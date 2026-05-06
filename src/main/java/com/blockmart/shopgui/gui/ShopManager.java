package com.blockmart.shopgui.gui;

import com.blockmart.shopgui.ShopGUI;
import com.blockmart.shopgui.utils.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ShopManager {

    private final ShopGUI plugin;
    private final Map<Integer, ShopItem> shopItems = new HashMap<>();

    public ShopManager(ShopGUI plugin) {
        this.plugin = plugin;
        loadShopItemsFromDatabase();
    }

    public void loadShopItemsFromDatabase() {
        CompletableFuture.runAsync(() -> {
            shopItems.clear();
            String selectSql = "SELECT id, item_serialized, buy_price, sell_price, stock, is_buyable, is_sellable FROM shop_items;";
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(selectSql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String serializedItem = rs.getString("item_serialized");
                    double buyPrice = rs.getDouble("buy_price");
                    double sellPrice = rs.getDouble("sell_price");
                    int stock = rs.getInt("stock");
                    boolean isBuyable = rs.getBoolean("is_buyable");
                    boolean isSellable = rs.getBoolean("is_sellable");

                    try {
                        ItemStack itemStack = ShopItem.itemStackFromBase64(serializedItem);
                        shopItems.put(id, new ShopItem(id, itemStack, buyPrice, sellPrice, stock, isBuyable, isSellable));
                    } catch (IOException e) {
                        plugin.getLogger().severe("Failed to deserialize item for shop item ID " + id + ": " + e.getMessage());
                    }
                }
                plugin.getLogger().info("Loaded " + shopItems.size() + " shop items from the database.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load shop items from database: " + e.getMessage());
            }
        });
    }

    public void saveShopItem(ShopItem shopItem) {
        CompletableFuture.runAsync(() -> {
            String serializedItem;
            try {
                serializedItem = ShopItem.itemStackToBase64(shopItem.getItemStack());
            } catch (IllegalStateException e) {
                plugin.getLogger().severe("Failed to serialize item for saving: " + e.getMessage());
                return;
            }

            String insertSql = "INSERT INTO shop_items (item_serialized, buy_price, sell_price, stock, is_buyable, is_sellable) VALUES (?, ?, ?, ?, ?, ?)";
            String updateSql = "UPDATE shop_items SET item_serialized = ?, buy_price = ?, sell_price = ?, stock = ?, is_buyable = ?, is_sellable = ? WHERE id = ?";

            try (Connection conn = plugin.getDatabaseManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(shopItem.getId() == 0 ? insertSql : updateSql)) {

                stmt.setString(1, serializedItem);
                stmt.setDouble(2, shopItem.getBuyPrice());
                stmt.setDouble(3, shopItem.getSellPrice());
                stmt.setInt(4, shopItem.getStock());
                stmt.setBoolean(5, shopItem.isBuyable());
                stmt.setBoolean(6, shopItem.isSellable());

                if (shopItem.getId() != 0) {
                    stmt.setInt(7, shopItem.getId());
                }
                stmt.executeUpdate();
                if (shopItem.getId() == 0) {
                    // If it was an insert, get the generated ID
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            shopItem = new ShopItem(generatedKeys.getInt(1), shopItem.getItemStack(), shopItem.getBuyPrice(), shopItem.getSellPrice(), shopItem.getStock(), shopItem.isBuyable(), shopItem.isSellable());
                        }
                    }
                }
                shopItems.put(shopItem.getId(), shopItem);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save shop item: " + e.getMessage());
            }
        });
    }

    public ShopItem getShopItemById(int id) {
        return shopItems.get(id);
    }

    public void openShopGUI(Player player) {
        ConfigurationSection guiConfig = plugin.getConfig().getConfigurationSection("shop.gui");
        if (guiConfig == null) {
            player.sendMessage("§cShop configuration not found. Please contact an administrator.");
            return;
        }

        String title = guiConfig.getString("title", "Shop");
        int size = guiConfig.getInt("size", 54);
        Inventory shopInventory = Bukkit.createInventory(new ShopGUIHolder(), size, title);

        // Load static items (e.g., border, pagination buttons) from config
        ConfigurationSection staticItemsSection = guiConfig.getConfigurationSection("static-items");
        if (staticItemsSection != null) {
            for (String key : staticItemsSection.getKeys(false)) {
                ConfigurationSection itemSection = staticItemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    try {
                        ItemStack item = createConfiguredItemStack(itemSection);
                        int slot = itemSection.getInt("slot", -1);
                        if (slot != -1 && slot < size) {
                            shopInventory.setItem(slot, item);
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid item configuration for static item '" + key + "': " + e.getMessage());
                    }
                }
            }
        }

        // Add shop items to the GUI (pagination logic would go here for larger shops)
        int currentSlot = 0;
        for (ShopItem shopItem : shopItems.values()) {
            if (currentSlot >= size) break; // Simple overflow protection

            ItemStack displayItem = new ItemStack(shopItem.getItemStack());
            ItemMeta meta = displayItem.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

            String buyStatus = shopItem.isBuyable() ? "§aBuy Price: §f" + plugin.getEconomyHook().format(shopItem.getBuyPrice()) : "§cCannot Buy";
            String sellStatus = shopItem.isSellable() ? "§eSell Price: §f" + plugin.getEconomyHook().format(shopItem.getSellPrice()) : "§cCannot Sell";
            String stockStatus = shopItem.getStock() == -1 ? "§bStock: §fUNLIMITED" : "§bStock: §f" + shopItem.getStock();

            lore.add("");
            lore.add(buyStatus);
            lore.add(sellStatus);
            lore.add(stockStatus);
            lore.add("");
            lore.add("§aLeft-Click to Buy");
            lore.add("§eRight-Click to Sell");

            meta.setLore(lore);
            displayItem.setItemMeta(meta);
            displayItem = NBTUtil.addShopItemId(displayItem, shopItem.getId());
            shopInventory.setItem(currentSlot, displayItem);
            currentSlot++;
        }

        player.openInventory(shopInventory);
    }

    private ItemStack createConfiguredItemStack(ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material", "BARRIER"));
        if (material == null) {
            throw new IllegalArgumentException("Invalid material for item: " + section.getString("material"));
        }
        ItemStack item = new ItemStack(material, section.getInt("amount", 1));
        ItemMeta meta = item.getItemMeta();
        if (section.getString("name") != null) {
            meta.setDisplayName(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(section.getString("name")));
        }
        if (section.isSet("lore")) {
            List<String> lore = new ArrayList<>();
            for (String line : section.getStringList("lore")) {
                lore.add(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(line).toString());
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        // Add NBT to identify static items if needed
        return NBTUtil.addShopItemStatic(item, section.getString("id", "unknown"));
    }
}
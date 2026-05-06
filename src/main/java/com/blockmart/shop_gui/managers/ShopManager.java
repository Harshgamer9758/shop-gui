package com.blockmart.shop_gui.managers;

import com.blockmart.shop_gui.ShopGUI;
import com.blockmart.shop_gui.models.ShopItem;
import com.blockmart.shop_gui.utils.NBTUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ShopManager {

    private final ShopGUI plugin;
    private final DatabaseManager databaseManager;
    private final List<ShopItem> shopItems;
    private static final int ITEMS_PER_PAGE = 27; // Max items per page, excluding control buttons
    private final Gson gson = new GsonBuilder().create();
    
    private ItemStack prevPageItem;
    private ItemStack nextPageItem;

    public ShopManager(ShopGUI plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.shopItems = new ArrayList<>();
        setupPageControlItems();
    }

    private void setupPageControlItems() {
        prevPageItem = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevPageItem.getItemMeta();
        prevMeta.setDisplayName("§aPrevious Page");
        prevPageItem.setItemMeta(NBTUtils.setNBTTag(prevMeta, "shopgui_page_control", "prev_page"));

        nextPageItem = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextPageItem.getItemMeta();
        nextMeta.setDisplayName("§aNext Page");
        nextPageItem.setItemMeta(NBTUtils.setNBTTag(nextMeta, "shopgui_page_control", "next_page"));
    }

    public void loadShopItems() {
        shopItems.clear();
        CompletableFuture.runAsync(() -> {
            // Load from database first
            String selectSql = "SELECT id, item_json, buy_price, sell_price FROM shop_items;";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(selectSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String itemJson = rs.getString("item_json");
                    double buyPrice = rs.getDouble("buy_price");
                    double sellPrice = rs.getDouble("sell_price");

                    ItemStack itemStack = NBTUtils.itemStackFromJson(itemJson);
                    if (itemStack != null) {
                        ItemMeta meta = itemStack.getItemMeta();
                        meta = NBTUtils.setNBTTag(meta, "shopgui_item_id", id);
                        itemStack.setItemMeta(meta);
                        shopItems.add(new ShopItem(id, itemStack, buyPrice, sellPrice));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load shop items from database!", e);
            }

            // Load from config for initial setup or new items
            ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("shop.items");
            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    String itemId = key;
                    Material material = Material.valueOf(itemsSection.getString(key + ".material", "STONE").toUpperCase());
                    String name = itemsSection.getString(key + ".name", null);
                    int amount = itemsSection.getInt(key + ".amount", 1);
                    List<String> lore = itemsSection.getStringList(key + ".lore");
                    double buyPrice = itemsSection.getDouble(key + ".buy-price", 0.0);
                    double sellPrice = itemsSection.getDouble(key + ".sell-price", 0.0);

                    ItemStack itemStack = new ItemStack(material, amount);
                    ItemMeta meta = itemStack.getItemMeta();
                    if (name != null) {
                        meta.setDisplayName(name.replace("&", "§"));
                    }
                    if (!lore.isEmpty()) {
                        meta.setLore(lore.stream().map(line -> line.replace("&", "§")).collect(java.util.List.of()));
                    }
                    meta = NBTUtils.setNBTTag(meta, "shopgui_item_id", itemId);
                    itemStack.setItemMeta(meta);

                    // Check if item already exists in database, if not, add it
                    if (shopItems.stream().noneMatch(si -> si.getId().equals(itemId))) {
                        ShopItem newShopItem = new ShopItem(itemId, itemStack, buyPrice, sellPrice);
                        shopItems.add(newShopItem);
                        saveShopItem(newShopItem); // Persist to database
                    }
                }
            }
            plugin.getLogger().log(Level.INFO, "Loaded " + shopItems.size() + " shop items.");
        });
    }

    public CompletableFuture<Void> saveShopItem(ShopItem item) {
        return CompletableFuture.runAsync(() -> {
            String itemJson = NBTUtils.itemStackToJson(item.getItemStack());
            String insertSql = "INSERT OR REPLACE INTO shop_items (id, item_json, buy_price, sell_price) VALUES (?, ?, ?, ?);";
            databaseManager.executeUpdate(insertSql, item.getId(), itemJson, item.getBuyPrice(), item.getSellPrice());
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to save shop item " + item.getId() + " to database!", ex);
            return null;
        });
    }

    public Optional<ShopItem> getShopItemById(String id) {
        return shopItems.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public boolean isValidPage(int page) {
        int maxPages = (int) Math.ceil((double) shopItems.size() / ITEMS_PER_PAGE);
        return page >= 1 && page <= Math.max(1, maxPages);
    }

    public void openShopGUI(Player player, int page) {
        int maxPages = (int) Math.ceil((double) shopItems.size() / ITEMS_PER_PAGE);
        int actualPage = Math.max(1, Math.min(page, Math.max(1, maxPages)));

        Inventory gui = Bukkit.createInventory(null, 54, "Shop - Page " + actualPage);

        int startIndex = (actualPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, shopItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem shopItem = shopItems.get(i);
            ItemStack displayItem = shopItem.getItemStack().clone();

            ItemMeta meta = displayItem.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add("§7Buy Price: §a" + shopItem.getBuyPrice());
            lore.add("§7Sell Price: §e" + shopItem.getSellPrice());
            lore.add("");
            lore.add("§eLeft-click to buy");
            lore.add("§eRight-click to sell");
            meta.setLore(lore);
            displayItem.setItemMeta(meta);

            // Add NBT tag to identify this custom shop item
            displayItem = NBTUtils.addNBTTag(displayItem, "shopgui_item_id", shopItem.getId());

            gui.setItem(i - startIndex, displayItem);
        }

        // Pagination controls
        if (actualPage > 1) {
            gui.setItem(45, prevPageItem); // Bottom left corner
        }
        if (actualPage < maxPages) {
            gui.setItem(53, nextPageItem); // Bottom right corner
        }

        player.openInventory(gui);
    }
}

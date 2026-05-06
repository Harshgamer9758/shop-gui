package com.blockmart.shopgui.managers;

import com.blockmart.shopgui.ShopGUI;
import com.blockmart.shopgui.models.ShopItem;
import com.blockmart.shopgui.utils.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;

public class ShopManager {

    private final ShopGUI plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, ShopItem> shopItems = new HashMap<>();
    public static final String GUI_TITLE = ConfigManager.getGUITitle();

    public ShopManager(ShopGUI plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void loadShopItems() {
        shopItems.clear();
        Map<String, ShopItem> itemsFromConfig = ConfigManager.getLoadedShopItems();
        shopItems.putAll(itemsFromConfig);
    }

    public ShopItem getShopItem(String id) {
        return shopItems.get(id);
    }

    public void openShopGUI(Player player) {
        openShopGUI(player, 1);
    }

    public void openShopGUI(Player player, int page) {
        int guiSize = ConfigManager.getGUISize();
        Inventory shopInventory = Bukkit.createInventory(null, guiSize, GUI_TITLE);

        List<ShopItem> itemsOnPage = shopItems.values().stream()
                .filter(item -> item.getPage() == page)
                .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);

        for (ShopItem item : itemsOnPage) {
            ItemStack displayItem = NBTUtil.setNBTString(item.getItemStack().clone(), "shop_item_id", item.getId());
            shopInventory.setItem(item.getSlot(), displayItem);
        }

        addNavigationButtons(shopInventory, page);

        player.openInventory(shopInventory);
    }

    private void addNavigationButtons(Inventory inventory, int currentPage) {
        int guiSize = inventory.getSize();

        // Previous Page button
        if (currentPage > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta = prevPage.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§aPrevious Page");
                prevPage.setItemMeta(meta);
            }
            prevPage = NBTUtil.setNBTInt(prevPage, "shop_prev_page", 1);
            prevPage = NBTUtil.setNBTInt(prevPage, "shop_current_page", currentPage);
            inventory.setItem(guiSize - 9, prevPage); // Example slot
        }

        // Next Page button
        boolean hasNextPage = shopItems.values().stream().anyMatch(item -> item.getPage() == currentPage + 1);
        if (hasNextPage) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta = nextPage.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§aNext Page");
                nextPage.setItemMeta(meta);
            }
            nextPage = NBTUtil.setNBTInt(nextPage, "shop_next_page", 1);
            nextPage = NBTUtil.setNBTInt(nextPage, "shop_current_page", currentPage);
            inventory.setItem(guiSize - 1, nextPage); // Example slot
        }
    }

    public void buyItem(Player player, ShopItem shopItem) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            double price = shopItem.getBuyPrice();
            ItemStack itemToGive = shopItem.getItemStack().clone();

            if (!plugin.getEconomyHook().has(player, price)) {
                player.sendMessage("§cYou don't have enough money to buy this item!");
                return;
            }

            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage("§cYour inventory is full!");
                return;
            }

            // Escrow logic: Deduct money first, then give item. If item fails, return money.
            if (plugin.getEconomyHook().withdrawPlayer(player, price).transactionSuccess()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.getInventory().addItem(itemToGive);
                    player.sendMessage("§aYou bought " + itemToGive.getAmount() + "x " + itemToGive.getType().name() + " for " + plugin.getEconomyHook().format(price) + ".");
                    databaseManager.recordTransaction(player.getUniqueId().toString(), player.getName(),
                            shopItem.getId(), shopItem.getItemStack().getType().name(),
                            itemToGive.getAmount(), price, "BUY");
                });
            } else {
                player.sendMessage("§cFailed to process transaction. Please try again.");
            }
        });
    }

    public void sellItem(Player player, ShopItem shopItem) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            double price = shopItem.getSellPrice();
            ItemStack itemToSell = shopItem.getItemStack().clone();
            itemToSell.setAmount(1); // We sell one item at a time based on its stack NBT

            // Check for NBT equality
            if (!player.getInventory().containsAtLeast(NBTUtil.stripNBT(itemToSell), 1)) {
                player.sendMessage("§cYou don't have this exact item to sell in your inventory.");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                // Find the exact item using NBT comparison and remove it
                ItemStack[] contents = player.getInventory().getContents();
                boolean removed = false;
                for (int i = 0; i < contents.length; i++) {
                    ItemStack currentItem = contents[i];
                    if (currentItem != null && !currentItem.getType().isAir() && NBTUtil.areSimilarIgnoreAmount(currentItem, itemToSell)) {
                        if (currentItem.getAmount() > 1) {
                            currentItem.setAmount(currentItem.getAmount() - 1);
                        } else {
                            player.getInventory().setItem(i, null);
                        }
                        removed = true;
                        break;
                    }
                }

                if (removed) {
                    // Escrow logic: Give money after removing item. If money fails, return item.
                    if (plugin.getEconomyHook().depositPlayer(player, price).transactionSuccess()) {
                        player.updateInventory();
                        player.sendMessage("§aYou sold 1x " + itemToSell.getType().name() + " for " + plugin.getEconomyHook().format(price) + ".");
                        databaseManager.recordTransaction(player.getUniqueId().toString(), player.getName(),
                                shopItem.getId(), shopItem.getItemStack().getType().name(),
                                1, price, "SELL");
                    } else {
                        // If money deposit fails, try to return item to player
                        player.getInventory().addItem(itemToSell);
                        player.sendMessage("§cFailed to process transaction. Item returned to inventory.");
                    }
                } else {
                    player.sendMessage("§cYou don't have this exact item to sell in your inventory.");
                }
            });
        });
    }
}

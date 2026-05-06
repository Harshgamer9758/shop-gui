package com.blockmart.shopgui.listeners;

import com.blockmart.shopgui.ShopGUI;
import com.blockmart.shopgui.gui.ShopGUIHolder;
import com.blockmart.shopgui.gui.ShopItem;
import com.blockmart.shopgui.utils.EconomyHook;
import com.blockmart.shopgui.utils.NBTUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class ShopListener implements Listener {

    private final ShopGUI plugin;

    public ShopListener(ShopGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getInventory();
        if (!(clickedInventory.getHolder() instanceof ShopGUIHolder)) {
            return; // Not our shop GUI
        }

        event.setCancelled(true); // Always cancel clicks in our shop GUI by default

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        // Check for static NBT (e.g., border items, navigation buttons)
        String staticItemId = NBTUtil.getShopItemStatic(clickedItem);
        if (staticItemId != null) {
            // Handle static item clicks (e.g., close, next page, category change)
            plugin.getLogger().info(player.getName() + " clicked static item: " + staticItemId);
            // Example: if (staticItemId.equals("close_button")) player.closeInventory();
            return;
        }

        Integer id = NBTUtil.getShopItemId(clickedItem);
        if (id == null) {
            return;
        }

        ShopItem shopItem = plugin.getShopManager().getShopItemById(id);
        if (shopItem == null) {
            player.sendMessage("§cThis shop item is no longer available. Please reopen the shop.");
            player.closeInventory();
            return;
        }

        EconomyHook economy = plugin.getEconomyHook();

        if (event.isLeftClick() && shopItem.isBuyable()) {
            // Buy item
            if (shopItem.getStock() != -1 && shopItem.getStock() < 1) {
                player.sendMessage("§cThis item is currently out of stock!");
                return;
            }
            if (!economy.has(player, shopItem.getBuyPrice())) {
                player.sendMessage("§cYou don't have enough money to buy this item!");
                return;
            }

            ItemStack itemToGive = new ItemStack(shopItem.getItemStack());
            if (player.getInventory().addItem(itemToGive).isEmpty()) {
                economy.withdrawPlayer(player, shopItem.getBuyPrice());
                if (shopItem.getStock() != -1) {
                    shopItem.setStock(shopItem.getStock() - 1);
                    plugin.getShopManager().saveShopItem(shopItem);
                    plugin.getShopManager().openShopGUI(player); // Reopen to refresh stock
                }
                player.sendMessage("§aYou bought " + itemToGive.getAmount() + "x " + itemToGive.getType().name() + " for " + economy.format(shopItem.getBuyPrice()) + ".");
            } else {
                player.sendMessage("§cYour inventory is full!");
            }

        } else if (event.isRightClick() && shopItem.isSellable()) {
            // Sell item
            ItemStack itemToSell = new ItemStack(shopItem.getItemStack().getType(), 1); // Sell one at a time
            if (!player.getInventory().containsAtLeast(itemToSell, 1)) {
                player.sendMessage("§cYou don't have this item to sell!");
                return;
            }

            player.getInventory().removeItem(itemToSell);
            economy.depositPlayer(player, shopItem.getSellPrice());
            if (shopItem.getStock() != -1) {
                shopItem.setStock(shopItem.getStock() + 1);
                plugin.getShopManager().saveShopItem(shopItem);
                plugin.getShopManager().openShopGUI(player); // Reopen to refresh stock
            }
            player.sendMessage("§aYou sold " + itemToSell.getAmount() + "x " + itemToSell.getType().name() + " for " + economy.format(shopItem.getSellPrice()) + ".");
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // If we had unique inventories per player or pagination state, we'd clean up here
    }
}
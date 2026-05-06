package com.blockmart.shop_gui.listeners;

import com.blockmart.shop_gui.managers.EconomyManager;
import com.blockmart.shop_gui.managers.ShopManager;
import com.blockmart.shop_gui.models.ShopItem;
import com.blockmart.shop_gui.utils.NBTUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ShopGUIListener implements Listener {

    private final ShopManager shopManager;
    private final EconomyManager economyManager;

    public ShopGUIListener(ShopManager shopManager, EconomyManager economyManager) {
        this.shopManager = shopManager;
        this.economyManager = economyManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();

        if (inventoryTitle.startsWith("Shop - Page ")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            if (NBTUtils.hasNBTTag(clickedItem, "shopgui_page_control")) {
                String controlType = NBTUtils.getNBTString(clickedItem, "shopgui_page_control");
                if (controlType == null) return;
                
                int currentPage = Integer.parseInt(inventoryTitle.replace("Shop - Page ", ""));
                if (controlType.equals("next_page")) {
                    int nextPage = currentPage + 1;
                    if (shopManager.isValidPage(nextPage)) {
                        shopManager.openShopGUI(player, nextPage);
                    }
                } else if (controlType.equals("prev_page")) {
                    int prevPage = currentPage - 1;
                    if (shopManager.isValidPage(prevPage)) {
                        shopManager.openShopGUI(player, prevPage);
                    }
                }
                return;
            }
            
            String itemId = NBTUtils.getNBTString(clickedItem, "shopgui_item_id");
            if (itemId == null) {
                return;
            }

            Optional<ShopItem> optionalShopItem = shopManager.getShopItemById(itemId);
            if (optionalShopItem.isEmpty()) {
                player.sendMessage("§cShop item not found.");
                return;
            }

            ShopItem shopItem = optionalShopItem.get();

            if (event.isLeftClick()) { // Buy
                if (!economyManager.hasBalance(player, shopItem.getBuyPrice())) {
                    player.sendMessage("§cYou don't have enough money to buy this.");
                    return;
                }
                if (economyManager.withdraw(player, shopItem.getBuyPrice())) {
                    player.getInventory().addItem(shopItem.getItemStack().clone());
                    player.sendMessage("§aYou bought " + shopItem.getItemStack().getItemMeta().getDisplayName() + " for " + shopItem.getBuyPrice() + "!");
                } else {
                    player.sendMessage("§cCould not complete purchase.");
                }
            } else if (event.isRightClick()) { // Sell
                if (!player.getInventory().containsAtLeast(shopItem.getItemStack(), 1)) {
                    player.sendMessage("§cYou don't have this item to sell.");
                    return;
                }

                ItemStack itemToSell = shopItem.getItemStack().clone();
                itemToSell.setAmount(1);

                if (player.getInventory().containsAtLeast(itemToSell, 1)) {
                    if (economyManager.deposit(player, shopItem.getSellPrice())) {
                        player.getInventory().removeItem(itemToSell);
                        player.sendMessage("§aYou sold " + shopItem.getItemStack().getItemMeta().getDisplayName() + " for " + shopItem.getSellPrice() + "!");
                    } else {
                        player.sendMessage("§cCould not complete sale.");
                    }
                } else {
                    player.sendMessage("§cYou don't have this item to sell.");
                }
            }
        }
    }
}

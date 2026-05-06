package com.blockmart.shopgui.listeners;

import com.blockmart.shopgui.ShopGUI;
import com.blockmart.shopgui.managers.ShopManager;
import com.blockmart.shopgui.models.ShopItem;
import com.blockmart.shopgui.utils.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.Optional;

public class ShopInventoryListener implements Listener {

    private final ShopGUI plugin;
    private final ShopManager shopManager;

    public ShopInventoryListener(ShopGUI plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getInventory();

        if (event.getView().getTitle().equals(ShopManager.GUI_TITLE)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            Optional<String> shopItemNBT = NBTUtil.getNBTString(clickedItem, "shop_item_id");
            if (shopItemNBT.isPresent()) {
                String shopItemId = shopItemNBT.get();
                ShopItem shopItem = shopManager.getShopItem(shopItemId);

                if (shopItem == null) {
                    player.sendMessage("§cThis shop item is no longer available.");
                    return;
                }

                if (event.isLeftClick()) { // Buy
                    shopManager.buyItem(player, shopItem);
                } else if (event.isRightClick()) { // Sell
                    shopManager.sellItem(player, shopItem);
                }
            } else if (NBTUtil.hasNBTKey(clickedItem, "shop_next_page")) {
                int currentPage = NBTUtil.getNBTInt(clickedItem, "shop_current_page").orElse(1);
                shopManager.openShopGUI(player, currentPage + 1);
            } else if (NBTUtil.hasNBTKey(clickedItem, "shop_prev_page")) {
                int currentPage = NBTUtil.getNBTInt(clickedItem, "shop_current_page").orElse(1);
                if (currentPage > 1) {
                    shopManager.openShopGUI(player, currentPage - 1);
                }
            }
        }
    }
}

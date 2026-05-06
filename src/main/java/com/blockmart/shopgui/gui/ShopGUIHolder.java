package com.blockmart.shopgui.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ShopGUIHolder implements InventoryHolder {

    @Override
    public @NotNull Inventory getInventory() {
        return null; // The inventory is created by ShopManager, this is just a marker holder.
    }
}
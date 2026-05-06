package com.blockmart.shop_gui.models;

import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private final String id;
    private final ItemStack itemStack;
    private final double buyPrice;
    private final double sellPrice;

    public ShopItem(String id, ItemStack itemStack, double buyPrice, double sellPrice) {
        this.id = id;
        this.itemStack = itemStack;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public String getId() {
        return id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }
}

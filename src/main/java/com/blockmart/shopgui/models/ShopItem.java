package com.blockmart.shopgui.models;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ShopItem {
    private final String id;
    private final ItemStack itemStack;
    private final double buyPrice;
    private final double sellPrice;
    private final int slot;
    private final int page;

    public ShopItem(String id, ItemStack itemStack, double buyPrice, double sellPrice, int slot, int page) {
        this.id = id;
        this.itemStack = itemStack;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.slot = slot;
        this.page = page;
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

    public int getSlot() {
        return slot;
    }

    public int getPage() {
        return page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopItem shopItem = (ShopItem) o;
        return Double.compare(buyPrice, shopItem.buyPrice) == 0 && Double.compare(sellPrice, shopItem.sellPrice) == 0 && slot == shopItem.slot && page == shopItem.page && Objects.equals(id, shopItem.id) && Objects.equals(itemStack, shopItem.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, itemStack, buyPrice, sellPrice, slot, page);
    }
}

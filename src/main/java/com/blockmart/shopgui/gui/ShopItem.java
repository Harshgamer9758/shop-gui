package com.blockmart.shopgui.gui;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ShopItem {

    private int id;
    private ItemStack itemStack;
    private double buyPrice;
    private double sellPrice;
    private int stock;
    private boolean isBuyable;
    private boolean isSellable;

    public ShopItem(int id, ItemStack itemStack, double buyPrice, double sellPrice, int stock, boolean isBuyable, boolean isSellable) {
        this.id = id;
        this.itemStack = itemStack;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stock = stock;
        this.isBuyable = isBuyable;
        this.isSellable = isSellable;
    }

    public int getId() {
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

    public int getStock() {
        return stock;
    }

    public boolean isBuyable() {
        return isBuyable;
    }

    public boolean isSellable() {
        return isSellable;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // Serialize ItemStack to Base64 String
    public static String itemStackToBase64(ItemStack item) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save itemstack.", e);
        }
    }

    // Deserialize Base64 String to ItemStack
    public static ItemStack itemStackFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
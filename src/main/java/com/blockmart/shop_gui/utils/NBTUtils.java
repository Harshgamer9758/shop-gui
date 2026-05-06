package com.blockmart.shop_gui.utils;

import com.blockmart.shop_gui.ShopGUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftMetaItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.util.logging.Level;

public class NBTUtils {

    private static ShopGUI plugin;
    private static NamespacedKey NBT_KEY_PREFIX;
    private static final Gson GSON = new GsonBuilder().create();

    public static void load(ShopGUI pluginInstance) {
        plugin = pluginInstance;
        NBT_KEY_PREFIX = new NamespacedKey(plugin, "shopgui");
    }

    // Method to add NBT tag to an ItemStack
    public static ItemStack addNBTTag(ItemStack item, String key, String value) {
        if (item == null || item.getType().isAir() || plugin == null) return item;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        CompoundTag nbtTag = nmsItem.hasTag() ? nmsItem.getTag() : new CompoundTag();
        nbtTag.putString(key, value);
        nmsItem.setTag(nbtTag);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    // Method to get NBT tag from an ItemStack
    public static String getNBTString(ItemStack item, String key) {
        if (item == null || item.getType().isAir() || plugin == null) return null;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem.hasTag()) {
            return nmsItem.getTag().getString(key);
        }
        return null;
    }

    // Method to check if an ItemStack has a specific NBT tag
    public static boolean hasNBTTag(ItemStack item, String key) {
        if (item == null || item.getType().isAir() || plugin == null) return false;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        return nmsItem.hasTag() && nmsItem.getTag().contains(key);
    }

    // Method to add NBT tag to ItemMeta (for pagination controls specifically)
    public static ItemMeta setNBTTag(ItemMeta meta, String key, String value) {
        if (meta == null || plugin == null) return meta;
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
        return meta;
    }

    public static String getNBTString(ItemMeta meta, String key) {
        if (meta == null || plugin == null) return null;
        return meta.getPersistentDataContainer().get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }

    public static boolean hasNBTTag(ItemMeta meta, String key) {
        if (meta == null || plugin == null) return false;
        return meta.getPersistentDataContainer().has(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }

    // Serialize ItemStack to JSON string (including NBT data)
    public static String itemStackToJson(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }
        try {
            net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            CompoundTag compound = new CompoundTag();
            nmsStack.save(compound);
            return compound.toString();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to serialize ItemStack to JSON: " + itemStack.getType(), e);
            return null;
        }
    }

    // Deserialize ItemStack from JSON string (including NBT data)
    public static ItemStack itemStackFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            CompoundTag compound = TagParser.parseTag(json);
            net.minecraft.world.item.ItemStack nmsStack = net.minecraft.world.item.ItemStack.of(compound);
            return CraftItemStack.asBukkitCopy(nmsStack);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to deserialize ItemStack from JSON: " + json, e);
            return null;
        }
    }
}

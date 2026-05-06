package com.blockmart.shopgui.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.blockmart.shopgui.ShopGUI;
import org.bukkit.NamespacedKey;

import java.util.Objects;
import java.util.Optional;

public class NBTUtil {

    private static ShopGUI plugin = ShopGUI.getPlugin(ShopGUI.class);

    public static ItemStack setNBTString(ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static Optional<String> getNBTString(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(new NamespacedKey(plugin, key), PersistentDataType.STRING)) {
                return Optional.ofNullable(container.get(new NamespacedKey(plugin, key), PersistentDataType.STRING));
            }
        }
        return Optional.empty();
    }

    public static ItemStack setNBTInt(ItemStack item, String key, int value) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.INTEGER, value);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static Optional<Integer> getNBTInt(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(new NamespacedKey(plugin, key), PersistentDataType.INTEGER)) {
                return Optional.ofNullable(container.get(new NamespacedKey(plugin, key), PersistentDataType.INTEGER));
            }
        }
        return Optional.empty();
    }

    public static boolean hasNBTKey(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }

    public static ItemStack stripNBT(ItemStack item) {
        ItemStack stripped = item.clone();
        ItemMeta meta = stripped.getItemMeta();
        if (meta != null) {
            for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
                meta.getPersistentDataContainer().remove(key);
            }
            stripped.setItemMeta(meta);
        }
        return stripped;
    }

    public static boolean areSimilarIgnoreAmount(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        // Compare material first
        if (item1.getType() != item2.getType()) {
            return false;
        }
        // Compare ItemMeta including NBT tags using PersistentDataContainer
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if (meta1 == null && meta2 == null) {
            return true;
        }
        if (meta1 == null || meta2 == null) {
            return false;
        }

        // Get PersistentDataContainers
        PersistentDataContainer container1 = meta1.getPersistentDataContainer();
        PersistentDataContainer container2 = meta2.getPersistentDataContainer();

        // Compare NBT keys and values
        if (!container1.getKeys().equals(container2.getKeys())) {
            return false;
        }

        for (NamespacedKey key : container1.getKeys()) {
            // Use a common PersistentDataType, e.g., STRING, to compare values
            // This assumes values are compatible or can be converted to string for comparison
            // For more robust comparison, you might need to check specific types per key
            if (!Objects.equals(container1.get(key, PersistentDataType.STRING), container2.get(key, PersistentDataType.STRING))) {
                return false;
            }
        }

        // Compare other ItemMeta properties (display name, lore, enchantments, etc.)
        // Excluding NBT tags we manually manage and compare above
        ItemMeta cleanMeta1 = meta1.clone();
        ItemMeta cleanMeta2 = meta2.clone();

        // Clear PersistentDataContainer from cloned metas to compare other properties
        for (NamespacedKey key : cleanMeta1.getPersistentDataContainer().getKeys()) {
            cleanMeta1.getPersistentDataContainer().remove(key);
        }
        for (NamespacedKey key : cleanMeta2.getPersistentDataContainer().getKeys()) {
            cleanMeta2.getPersistentDataContainer().remove(key);
        }

        return cleanMeta1.equals(cleanMeta2);
    }
}

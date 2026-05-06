package com.blockmart.shopgui.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import com.blockmart.shopgui.ShopGUI;

import java.util.Objects;
import java.util.concurrent.Callable;

public class NBTUtil {

    private static NamespacedKey SHOP_ITEM_ID_KEY;
    private static NamespacedKey SHOP_ITEM_STATIC_KEY;

    public static void init(ShopGUI plugin) {
        SHOP_ITEM_ID_KEY = new NamespacedKey(plugin, "shop_item_id");
        SHOP_ITEM_STATIC_KEY = new NamespacedKey(plugin, "shop_item_static");
    }

    public static ItemStack addShopItemId(ItemStack item, int id) {
        if (item == null || item.getType().isAir()) return item;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(SHOP_ITEM_ID_KEY, PersistentDataType.INTEGER, id);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static Integer getShopItemId(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            return meta.getPersistentDataContainer().get(SHOP_ITEM_ID_KEY, PersistentDataType.INTEGER);
        }
        return null;
    }

    public static ItemStack addShopItemStatic(ItemStack item, String staticId) {
        if (item == null || item.getType().isAir()) return item;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(SHOP_ITEM_STATIC_KEY, PersistentDataType.STRING, staticId);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getShopItemStatic(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            return meta.getPersistentDataContainer().get(SHOP_ITEM_STATIC_KEY, PersistentDataType.STRING);
        }
        return null;
    }
}
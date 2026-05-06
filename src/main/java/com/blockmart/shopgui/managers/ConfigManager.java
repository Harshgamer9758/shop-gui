package com.blockmart.shopgui.managers;

import com.blockmart.shopgui.ShopGUI;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.blockmart.shopgui.models.ShopItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ConfigManager {

    private static ShopGUI plugin;
    private static Map<String, ShopItem> loadedShopItems = new HashMap<>();

    public static void reloadConfig(ShopGUI p) {
        plugin = p;
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        loadedShopItems.clear();

        config.getConfigurationSection("shop.items").getKeys(false).forEach(key -> {
            String path = "shop.items." + key;
            try {
                Material material = Material.valueOf(config.getString(path + ".material").toUpperCase());
                String name = config.getString(path + ".name", "");
                List<String> lore = config.getStringList(path + ".lore");
                double buyPrice = config.getDouble(path + ".buy-price");
                double sellPrice = config.getDouble(path + ".sell-price");
                int slot = config.getInt(path + ".slot");
                int page = config.getInt(path + ".page", 1);
                int amount = config.getInt(path + ".amount", 1);

                ItemStack itemStack = new ItemStack(material, amount);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    if (!name.isEmpty()) {
                        meta.setDisplayName(name.replace("&", "§"));
                    }
                    if (!lore.isEmpty()) {
                        meta.setLore(lore.stream().map(line -> line.replace("&", "§")).collect(Collectors.toList()));
                    }
                    itemStack.setItemMeta(meta);
                }

                ShopItem shopItem = new ShopItem(key, itemStack, buyPrice, sellPrice, slot, page);
                loadedShopItems.put(key, shopItem);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Invalid material for shop item '" + key + "' in config.yml: " + e.getMessage());
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error loading shop item '" + key + "' from config.yml: " + e.getMessage());
            }
        });

        plugin.getLogger().log(Level.INFO, "Loaded " + loadedShopItems.size() + " shop items from config.");
    }

    public static Map<String, ShopItem> getLoadedShopItems() {
        return new HashMap<>(loadedShopItems);
    }

    public static int getGUISize() {
        return plugin.getConfig().getInt("shop.gui-size", 54);
    }

    public static String getGUITitle() {
        return plugin.getConfig().getString("shop.gui-title", "Shop").replace("&", "§");
    }
}

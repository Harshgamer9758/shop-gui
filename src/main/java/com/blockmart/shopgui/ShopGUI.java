package com.blockmart.shopgui;

import com.blockmart.shopgui.commands.ShopCommand;
import com.blockmart.shopgui.listeners.ShopInventoryListener;
import com.blockmart.shopgui.managers.ConfigManager;
import com.blockmart.shopgui.managers.DatabaseManager;
import com.blockmart.shopgui.managers.ShopManager;
import com.blockmart.shopgui.utils.EconomyHook;
import com.blockmart.shopgui.utils.NBTUtil;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public final class ShopGUI extends JavaPlugin {

    private EconomyHook economyHook;
    private DatabaseManager databaseManager;
    private ShopManager shopManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager.reloadConfig(this);

        if (!setupEconomy()) {
            getLogger().log(Level.SEVERE, "Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseManager = new DatabaseManager(this);
        databaseManager.loadDatabase();

        shopManager = new ShopManager(this, databaseManager);
        shopManager.loadShopItems();

        getCommand("shop").setExecutor(new ShopCommand(this, shopManager));
        getServer().getPluginManager().registerEvents(new ShopInventoryListener(this, shopManager), this);

        getLogger().log(Level.INFO, "ShopGUI has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().log(Level.INFO, "ShopGUI has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        economyHook = new EconomyHook();
        return economyHook.setupEconomy(getServer().getServicesManager());
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }
}

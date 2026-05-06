package com.blockmart.shopgui;

import com.blockmart.shopgui.commands.ShopCommand;
import com.blockmart.shopgui.database.DatabaseManager;
import com.blockmart.shopgui.gui.ShopManager;
import com.blockmart.shopgui.listeners.ShopListener;
import com.blockmart.shopgui.utils.EconomyHook;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShopGUI extends JavaPlugin {

    private DatabaseManager databaseManager;
    private ShopManager shopManager;
    private EconomyHook economyHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.shopManager = new ShopManager(this);
        this.economyHook = new EconomyHook(this);

        if (!economyHook.setupEconomy()) {
            getLogger().severe("No Vault dependency found! ShopGUI will not function without an economy plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("shop").setExecutor(new ShopCommand(this));
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        getLogger().info("ShopGUI has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().info("ShopGUI has been disabled!");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }
}
package com.blockmart.shop_gui;

import com.blockmart.shop_gui.commands.ShopCommand;
import com.blockmart.shop_gui.listeners.ShopGUIListener;
import com.blockmart.shop_gui.managers.DatabaseManager;
import com.blockmart.shop_gui.managers.EconomyManager;
import com.blockmart.shop_gui.managers.ShopManager;
import com.blockmart.shop_gui.utils.NBTUtils;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

public final class ShopGUI extends JavaPlugin {

    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private ShopManager shopManager;
    private Economy vaultEconomy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        NBTUtils.load(this);

        if (!setupEconomy()) {
            getLogger().log(Level.SEVERE, "Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.databaseManager = new DatabaseManager(this);
        this.shopManager = new ShopManager(this, databaseManager);
        this.economyManager = new EconomyManager(vaultEconomy);

        databaseManager.connect();
        databaseManager.createTables();
        shopManager.loadShopItems();

        getCommand("shop").setExecutor(new ShopCommand(shopManager));
        getServer().getPluginManager().registerEvents(new ShopGUIListener(shopManager, economyManager), this);

        getLogger().info("ShopGUI has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("ShopGUI has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }
}

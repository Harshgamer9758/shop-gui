package com.blockmart.shopgui.utils;

import com.blockmart.shopgui.ShopGUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {

    private final ShopGUI plugin;
    private Economy econ = null;

    public EconomyHook(ShopGUI plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public Economy getEconomy() {
        return econ;
    }

    public String format(double amount) {
        if (econ != null) {
            return econ.format(amount);
        }
        return String.format("%.2f", amount);
    }

    public boolean has(OfflinePlayer player, double amount) {
        return econ != null && econ.has(player, amount);
    }

    public void withdrawPlayer(OfflinePlayer player, double amount) {
        if (econ != null) {
            econ.withdrawPlayer(player, amount);
        }
    }

    public void depositPlayer(OfflinePlayer player, double amount) {
        if (econ != null) {
            econ.depositPlayer(player, amount);
        }
    }
}
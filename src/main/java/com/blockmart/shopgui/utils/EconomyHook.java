package com.blockmart.shopgui.utils;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServiceManager;

public class EconomyHook {

    private Economy econ = null;

    public boolean setupEconomy(ServiceManager sm) {
        if (sm.getRegistration(Economy.class) == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = sm.getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public boolean isEnabled() {
        return econ != null;
    }

    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return econ.depositPlayer(player, amount);
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return econ.withdrawPlayer(player, amount);
    }

    public boolean has(OfflinePlayer player, double amount) {
        return econ.has(player, amount);
    }

    public String format(double amount) {
        return econ.format(amount);
    }
}

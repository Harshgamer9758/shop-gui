package com.blockmart.shop_gui.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class EconomyManager {

    private final Economy economy;

    public EconomyManager(Economy economy) {
        this.economy = economy;
    }

    public boolean hasBalance(Player player, double amount) {
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (hasBalance(player, amount)) {
            return economy.withdrawPlayer(player, amount).transactionSuccess();
        }
        return false;
    }

    public boolean deposit(Player player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
}

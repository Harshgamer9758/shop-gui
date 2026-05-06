package com.blockmart.shopgui.commands;

import com.blockmart.shopgui.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommand implements CommandExecutor {

    private final ShopGUI plugin;

    public ShopCommand(ShopGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("shopgui.open")) {
            player.sendMessage("§cYou don't have permission to open the shop.");
            return true;
        }

        plugin.getShopManager().openShopGUI(player);
        return true;
    }
}
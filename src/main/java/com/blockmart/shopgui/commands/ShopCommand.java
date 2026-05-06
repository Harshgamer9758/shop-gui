package com.blockmart.shopgui.commands;

import com.blockmart.shopgui.ShopGUI;
import com.blockmart.shopgui.managers.ShopManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommand implements CommandExecutor {

    private final ShopGUI plugin;
    private final ShopManager shopManager;

    public ShopCommand(ShopGUI plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            shopManager.openShopGUI(player);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload") && player.hasPermission("shopgui.admin")) {
            plugin.reloadConfig();
            shopManager.loadShopItems();
            player.sendMessage("§aShop configuration reloaded.");
            return true;
        }

        player.sendMessage("§cUsage: /shop [reload]");
        return true;
    }
}

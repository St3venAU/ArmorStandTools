package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

class Commands implements CommandExecutor {

    private final Main plugin;

    Commands(Main main) {
        plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            System.out.println(Config.notConsole);
            return false;
        }
        Player p = (Player) sender;
        if (!Utils.hasPermissionNode(p, "astools.command")) {
            p.sendMessage(ChatColor.RED + Config.noCommandPerm);
            return true;
        }
        if(args.length == 0) {
            UUID uuid = p.getUniqueId();
            if(plugin.savedInventories.containsKey(uuid)) {
                plugin.restoreInventory(p);
                return true;
            } else {
                plugin.saveInventoryAndClear(p);
                ArmorStandTool.give(p);
                p.sendMessage(ChatColor.GREEN + Config.giveMsg1);
                p.sendMessage(ChatColor.AQUA + Config.giveMsg2);
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("reload")) {
            if(Utils.hasPermissionNode(p, "astools.reload")) {
                Config.reload();
                p.sendMessage(ChatColor.GREEN + Config.conReload);
                return true;
            } else {
                p.sendMessage(ChatColor.RED + Config.noRelPerm);
                return true;
            }
        }
        return false;
    }
}

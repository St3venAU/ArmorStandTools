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
        if(args.length == 0) {
            UUID uuid = p.getUniqueId();
            if(plugin.savedInventories.containsKey(uuid)) {
                p.getInventory().setContents(plugin.savedInventories.get(uuid));
                plugin.savedInventories.remove(uuid);
                p.sendMessage(ChatColor.GREEN + Config.invReturned);
                return true;
            } else {
                plugin.savedInventories.put(uuid, p.getInventory().getContents());
                ArmorStandTool.give(p);
                p.sendMessage(ChatColor.GREEN + Config.giveMsg1);
                p.sendMessage(ChatColor.AQUA + Config.giveMsg2);
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("reload")) {
            if(p.hasPermission("astools.reload")) {
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

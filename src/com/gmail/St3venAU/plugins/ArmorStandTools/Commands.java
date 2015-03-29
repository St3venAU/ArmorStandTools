package com.gmail.St3venAU.plugins.ArmorStandTools;

import net.minecraft.server.v1_8_R1.Material;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
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
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command");
        }
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
        if(args[0].equalsIgnoreCase("head")) {
            // get the entity you are looking at
            if(!Utils.hasPermissionNode(p, "astools.head")) {
                p.sendMessage(ChatColor.RED + Config.noHeadPerm);
                return true;
            }
            List<Entity> nearby = p.getNearbyEntities(16, 16, 16);
            if (nearby.size() == 0) {
                p.sendMessage(ChatColor.RED + Config.notTarget);
                return true;
            }
            double min = 0.5;
            Entity closest = null;
            Vector loc = p.getLocation().toVector();
            for (Entity entity : nearby) {
                Vector eLoc = entity.getLocation().toVector().subtract(loc).normalize();
                Vector target = p.getLocation().getDirection().normalize();
                double diff = eLoc.subtract(target).length();
                if (diff < min) {
                    min = diff;
                    closest = entity;
                }
            }
            if (closest == null || !(closest instanceof ArmorStand)) {
                p.sendMessage(ChatColor.RED + Config.notTarget);
                return true;
            }
            ArmorStand as = (ArmorStand) closest;
            if (!MainListener.checkPermission(p, closest.getLocation().getBlock())) {
                p.sendMessage(ChatColor.RED + Config.wgNoPerm);
                return true;
            }
            ItemStack item = p.getItemInHand();
            if (item == null || item.getTypeId() == 0) {
                as.setHelmet(new ItemStack(0));
                p.sendMessage(ChatColor.GREEN + Config.setHead);
                return true;
            }
            if (!item.getType().isBlock()) {
                p.sendMessage(ChatColor.RED + Config.notBlock);
                return true;
            }
            as.setHelmet(item);
            p.sendMessage(ChatColor.GREEN + Config.setHead);
            return true;
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

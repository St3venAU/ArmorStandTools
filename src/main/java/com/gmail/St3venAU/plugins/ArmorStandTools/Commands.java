package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Commands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            AST.plugin.getLogger().warning(Config.notConsole);
            return false;
        }
        String cmd = command.getName().toLowerCase();
        if(cmd.equals("astools") || cmd.equals("ast")) {
            if (!Utils.hasPermissionNode(p, "astools.use")) {
                p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                return true;
            }
            if(args.length > 0 && args[0].equalsIgnoreCase("new")) {
                // astools new
                if (!Utils.hasPermissionNode(p, "astools.new")) {
                    p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                    return true;
                }
                Location l = Utils.getLocationFacing(p.getLocation());
                if(l.getWorld() == null) {
                    p.sendMessage(ChatColor.RED + Config.error);
                    return true;
                }
                ArmorStand as = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
                AST.pickUpArmorStand(as, p);
                Utils.title(p, Config.carrying);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (Utils.hasPermissionNode(p, "astools.reload")) {
                    Config.reload(null);
                    p.sendMessage(ChatColor.GREEN + Config.reloaded);
                } else {
                    p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                }
                return true;
            }
            p.sendMessage(ChatColor.AQUA + Config.instructions1 + ChatColor.GREEN + " /ast new " + ChatColor.AQUA + Config.instructions2);
            return true;
        } else if(cmd.equals("ascmd")) {
            ArmorStand as = getNearbyArmorStand(p);
            if(as == null) {
                p.sendMessage("\n" + Config.noASNearBy);
                return true;
            }
            String name = " ";
            if(as.getName().length() > 0 && !as.getName().equalsIgnoreCase("armor stand")) {
                name = " (" + ChatColor.AQUA + as.getName() + ChatColor.RESET + ") ";
            }
            if(args.length > 0 && args[0].equalsIgnoreCase("view")) {
                // ascmd view
                if (!Utils.hasPermissionNode(p, "astools.ascmd.view")) {
                    p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                    return true;
                }
                ArmorStandCmd asCmd = new ArmorStandCmd(as);
                if(asCmd.getCommand() == null) {
                    p.sendMessage("\n" + Config.closestAS + name + Config.hasNoCmd);
                } else {
                    p.sendMessage("\n" + Config.closestAS + name + Config.hasCmd);
                    p.sendMessage(Config.type + ": " + ChatColor.YELLOW + asCmd.getType());
                    p.sendMessage(Config.command + ": " + ChatColor.YELLOW + asCmd.getCommand());
                }
            } else if(args.length > 0 && args[0].equalsIgnoreCase("remove")) {
                // ascmd remove
                if (!Utils.hasPermissionNode(p, "astools.ascmd.remove")) {
                    p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                    return true;
                }
                if(ArmorStandCmd.removeAssignedCommand(as)) {
                    p.sendMessage("\n" + Config.unassignedCmd + name);
                } else {
                    p.sendMessage("\n" + Config.closestAS + name + Config.hasNoCmd);
                }
            } else if(args.length >= 3 && args[0].equalsIgnoreCase("assign")) {
                // ascmd assign <player/console> (command)
                ArmorStandCmd asCmd = new ArmorStandCmd(as);
                if(asCmd.getCommand() != null) {
                    p.sendMessage("\n" + Config.closestAS + name + Config.hasCmd);
                    p.sendMessage(Config.removeCmd + ": " + ChatColor.YELLOW + " /ascmd remove");
                    return true;
                }
                ArmorStandCmd.CmdType cmdtype = null;
                if(args[1].equalsIgnoreCase("console")) {
                    cmdtype = ArmorStandCmd.CmdType.Console;
                    if (!Utils.hasPermissionNode(p, "astools.ascmd.assign.console")) {
                        p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                        return true;
                    }
                } else if(args[1].equalsIgnoreCase("player")) {
                    cmdtype = ArmorStandCmd.CmdType.Player;
                    if (!Utils.hasPermissionNode(p, "astools.ascmd.assign.player")) {
                        p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                        return true;
                    }
                } else if(args[1].equalsIgnoreCase("bungeecord")) {
                    cmdtype = ArmorStandCmd.CmdType.Bungeecord;
                    if (!Utils.hasPermissionNode(p, "astools.ascmd.assign.bungeecord")) {
                        p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                        return true;
                    }
                }
                if(cmdtype == null) {
                    ascmdHelp(p);
                    return true;
                }
                StringBuilder sb = new StringBuilder();
                for(int i = 2; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                int startAt = sb.charAt(0) == '/' ? 1 : 0;
                String c = sb.substring(startAt, sb.length() - 1);
                if(c.length() == 0) {
                    ascmdHelp(p);
                    return true;
                }
                asCmd = new ArmorStandCmd(as, c, cmdtype);
                if(asCmd.save()) {
                    p.sendMessage("\n" + Config.assignedCmdToAS + name);
                    p.sendMessage(Config.type + ": " + ChatColor.YELLOW + asCmd.getType());
                    p.sendMessage(Config.command + ": " + ChatColor.YELLOW + asCmd.getCommand());
                } else {
                    p.sendMessage("\n" + Config.assignCmdError + name);
                }
            } else if(args.length >= 2 && args[0].equalsIgnoreCase("cooldown")) { //ascmd cooldown <ticks>/remove
                if (!Utils.hasPermissionNode(p, "astools.ascmd.cooldown")) {
                    p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                    return true;
                }
                ArmorStandCmd asCmd = new ArmorStandCmd(as);
                if(asCmd.getCommand() == null) {
                    p.sendMessage(Config.closestAS + name + Config.hasNoCmd);
                    return true;
                }
                if(args[1].equalsIgnoreCase("remove")) {
                    asCmd.setCooldownTime(-1);
                    p.sendMessage(Config.cooldownRemovedFrom + " " + Config.closestAS + name);
                } else {
                    int ticks;
                    try {
                        ticks = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        p.sendMessage(args[1] + " " + Config.isAnInvalidCooldown);
                        return true;
                    }
                    if(ticks < 0) {
                        p.sendMessage(args[1] + " " + Config.isAnInvalidCooldown);
                        return true;
                    }
                    asCmd.setCooldownTime(ticks);
                    p.sendMessage(Config.cooldownSetTo + " " + ticks + " " + Config.ticksFor + " " + Config.closestAS + name);
                }
                return true;
            } else {
                ascmdHelp(p);
                return true;
            }
            return true;
        }
        return false;
    }

    private void ascmdHelp(Player p) {
        p.sendMessage("\n" + ChatColor.AQUA + Config.ascmdHelp);
        p.sendMessage(Config.viewCmd + ": " + ChatColor.YELLOW + "/ascmd view");
        p.sendMessage(Config.removeCmd + ": " + ChatColor.YELLOW + "/ascmd remove");
        p.sendMessage(Config.assignConsole + ":");
        p.sendMessage(ChatColor.YELLOW + "/ascmd assign console <command>");
        p.sendMessage(Config.assignPlayer + ":");
        p.sendMessage(ChatColor.YELLOW + "/ascmd assign player <command>");
        p.sendMessage(Config.assignBungee + ":");
        p.sendMessage(ChatColor.YELLOW + "/ascmd assign bungeecord <server>");
        p.sendMessage(Config.setCooldown + ":");
        p.sendMessage(ChatColor.YELLOW + "/ascmd cooldown <ticks>");
        p.sendMessage(Config.removeCooldown + ":");
        p.sendMessage(ChatColor.YELLOW + "/ascmd cooldown remove");
    }

    private ArmorStand getNearbyArmorStand(Player p) {
        ArmorStand closest = null;
        double dist = 1000000;
        for(Entity e : p.getNearbyEntities(4, 4, 4)) {
            if(e instanceof ArmorStand && e.getLocation().distanceSquared(p.getLocation()) < dist) {
                closest = (ArmorStand) e;
            }
        }
        return closest;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        String cmd = command.getName().toLowerCase();
        String typed = "";
        if (args.length > 0) {
            typed = args[args.length - 1].toLowerCase();
        }
        if (cmd.equals("ascmd")) {
            if (args.length == 1) {
                for(String s : Arrays.asList("view", "remove", "assign", "cooldown")) {
                    if(s.startsWith(typed)) {
                        list.add(s);
                    }
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("assign")) {
                for(String s : Arrays.asList("player", "console", "bungeecord")) {
                    if(s.startsWith(typed)) {
                        list.add(s);
                    }
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("cooldown")) {
                String s = "remove";
                if(s.startsWith(typed)) {
                    list.add(s);
                }
            }
        }
        return list;
    }
}

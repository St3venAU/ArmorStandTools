package com.gmail.st3venau.plugins.armorstandtools;

import org.jetbrains.annotations.NotNull;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class Commands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            AST.plugin.getLogger().warning(Config.notConsole);
            return false;
        }

        String cmd = command.getName().toLowerCase();
        if (cmd.equals("astools") || cmd.equals("ast")) {
            if (Config.useCommandForTextInput && args.length > 0) {
                StringBuilder sb = new StringBuilder();
                boolean space = false;
                for (String s : args) {
                    if (space) {
                        sb.append(' ');
                    }

                    space = true;
                    sb.append(s);
                }

                if (AST.processInput(p, sb.toString())) {
                    return true;
                }
            }

            if (!Utils.hasPermissionNode(p, "astools.command")) {
                p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                return true;
            }

            if (args.length == 0) {
                UUID uuid = p.getUniqueId();
                if (AST.savedInventories.containsKey(uuid)) {
                    AST.restoreInventory(p);
                } else {
                    AST.plugin.saveInventoryAndClear(p);
                    ArmorStandTool.give(p);
                    p.sendMessage(ChatColor.GREEN + Config.giveMsg1);
                    p.sendMessage(ChatColor.AQUA + Config.giveMsg2);
                }

                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (Utils.hasPermissionNode(p, "astools.reload")) {
                    Config.reload();
                    p.sendMessage(ChatColor.GREEN + Config.conReload);
                } else {
                    p.sendMessage(ChatColor.RED + Config.noRelPerm);
                }

                return true;
            }
        } else if (cmd.equals("ascmd")) {
            ArmorStand as = getNearbyArmorStand(p);
            if (as == null) {
                p.sendMessage("\n" + Config.noASNearBy);
                return true;
            }

            String name = " ";
            if (!as.getName().isEmpty() && !as.getName().equalsIgnoreCase("armor stand")) {
                name = ChatColor.RESET + " (" + ChatColor.AQUA + as.getName() + ChatColor.RESET + ") ";
            }

            ArmorStandCmdManager asCmdManager = new ArmorStandCmdManager(as);
            if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
                //       [0]
                // ascmd list
                if (!Utils.hasPermissionNode(p, "astools.ascmd.list")) {
                    p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                    return true;
                }

                listAssignedCommands(asCmdManager, name, p);
                return true;
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
                //       [0]    [1]
                // ascmd remove <command_number>
                if (!Utils.hasPermissionNode(p, "astools.ascmd.remove")) {
                    p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                    return true;
                }

                int n;
                try {
                    n = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + args[1] + " " + Config.isNotValidNumber);
                    return true;
                }

                if (asCmdManager.removeCommand(n - 1)) {
                    p.sendMessage("\n" + Config.command + ChatColor.GREEN + " #" + n + ChatColor.RESET + " " + Config.removedFromAs + name);
                } else {
                    p.sendMessage(ChatColor.RED + args[1] + " " + Config.isNotValidNumber);
                }

                listAssignedCommands(asCmdManager, name, p);
                return true;
            } else if (args.length >= 5 && args[0].equalsIgnoreCase("add")) {
                //       [0] [1]        [2]     [3]                     [4 - ...]
                // ascmd add <priority> <delay> <player/console/bungee> <command/bungee_server_name>
                CommandType type = CommandType.fromName(args[3]);
                if (type == null) {
                    ascmdHelp(p);
                    return true;
                }

                if (!Utils.hasPermissionNode(p, type.getAddPermission())) {
                    p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                    return true;
                }

                int priority;
                try {
                    priority = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + args[1] + " " + Config.isNotValidNumber);
                    return true;
                }

                int delay;
                try {
                    delay = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + args[2] + " " + Config.isNotValidNumber);
                    return true;
                }

                if (delay < 0) {
                    p.sendMessage(ChatColor.RED + args[2] + " " + Config.isNotValidNumber);
                    return true;
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 4; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }

                int startAt = sb.charAt(0) == '/' ? 1 : 0;
                String c = sb.substring(startAt, sb.length() - 1);
                if (c.isEmpty()) {
                    ascmdHelp(p);
                    return true;
                }

                asCmdManager.addCommand(new ArmorStandCmd(c, type, priority, delay), true);
                listAssignedCommands(asCmdManager, name, p);
                return true;
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("cooldown")) { //ascmd cooldown <ticks>/remove
                if (!Utils.hasPermissionNode(p, "astools.ascmd.cooldown")) {
                    p.sendMessage(ChatColor.RED + Config.noCommandPerm);
                    return true;
                }

                if (!asCmdManager.hasCommands()) {
                    p.sendMessage(Config.closestAS + name + Config.hasNoCmds);
                    return true;
                }

                if (args[1].equalsIgnoreCase("remove")) {
                    asCmdManager.setCooldownTime(-1);
                    p.sendMessage(Config.cooldownRemovedFrom + " " + Config.closestAS + name);
                } else {
                    int ticks;
                    try {
                        ticks = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        p.sendMessage(args[1] + " " + Config.isAnInvalidCooldown);
                        return true;
                    }
                    if (ticks < 0) {
                        p.sendMessage(args[1] + " " + Config.isAnInvalidCooldown);
                        return true;
                    }

                    asCmdManager.setCooldownTime(ticks);
                    p.sendMessage(Config.cooldownSetTo + " " + ticks + " " + Config.ticksFor + " " + Config.closestAS + name);
                }

                return true;
            } else {
                ascmdHelp(p);
                return true;
            }
        }

        return false;
    }

    private void listAssignedCommands(ArmorStandCmdManager asCmdManager, String name, Player p) {
        if (asCmdManager.hasCommands()) {
            p.sendMessage("\n" + Config.closestAS + name + Config.hasTheseCmdsAssigned);
            List<ArmorStandCmd> list = asCmdManager.getCommands();
            for (int n = 0; n < list.size(); n++) {
                ArmorStandCmd asCmd = list.get(n);
                // #1 Priority:0 Delay:0 Type:Player Command:cmd
                p.sendMessage(
                ChatColor.GREEN + "#" + (n + 1) + " " +
                   ChatColor.LIGHT_PURPLE + Config.priority + ":" + ChatColor.RESET + asCmd.priority() + " " +
                   ChatColor.YELLOW + Config.delay + ":" + ChatColor.RESET + asCmd.delay() + " " +
                   ChatColor.GOLD + Config.type + ":" + ChatColor.RESET + asCmd.type().getName() + " " +
                   ChatColor.AQUA + Config.command + ":" + ChatColor.RESET + asCmd.command()
                );
            }
        } else {
            p.sendMessage("\n" + Config.closestAS + name + Config.hasNoCmds);
        }
    }

    private void ascmdHelp(Player p) {
        p.sendMessage("\n" + ChatColor.AQUA + Config.cmdHelp);
        p.sendMessage(Config.listAssignedCmds + ": " + ChatColor.YELLOW + "/ascmd list");
        p.sendMessage(Config.addACmd + ":" + ChatColor.YELLOW + "/ascmd add <priority> <delay> <player/console/bungee> <command/bungee_server_name>");
        p.sendMessage(Config.removeACmd + ": " + ChatColor.YELLOW + "/ascmd remove <command_number>");
        p.sendMessage(Config.setCooldown + ":");
        p.sendMessage(ChatColor.YELLOW + "/ascmd cooldown <ticks>");
        p.sendMessage(Config.removeCooldown + ":");
        p.sendMessage(ChatColor.YELLOW + "/ascmd cooldown remove");
    }

    private ArmorStand getNearbyArmorStand(Player p) {
        ArmorStand closestAs = null;
        double closestDist = 1000000;
        for (Entity e : p.getNearbyEntities(4, 4, 4)) {
            if (!(e instanceof ArmorStand)) {
                continue;
            }

            double dist = e.getLocation().distanceSquared(p.getLocation());
            if (dist < closestDist) {
                closestDist = dist;
                closestAs = (ArmorStand) e;
            }
        }

        return closestAs;
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
                for (String s : Arrays.asList("list", "remove", "add", "cooldown")) {
                    if (s.startsWith(typed)) {
                        list.add(s);
                    }
                }
            } else if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 2 && typed.isEmpty()) {
                    list.add("priority");
                } if (args.length == 3 && typed.isEmpty()) {
                    list.add("delay");
                } else if (args.length == 4) {
                    for (String s : Arrays.asList("player", "console", "bungee")) {
                        if (s.startsWith(typed)) {
                            list.add(s);
                        }
                    }
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("cooldown")) {
                String s = "remove";
                if (s.startsWith(typed)) {
                    list.add(s);
                }
            }
        }

        return list;
    }
}

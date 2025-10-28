package com.gmail.st3venau.plugins.armorstandtools;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public record ArmorStandCmd(String command, CommandType type, Integer priority, Integer delay) implements Comparable<ArmorStandCmd>, Serializable {

    String getTag() {
        return "ascmd::v2::" + priority + "::" + delay + "::" + type.getTag() + "::" + command;
    }

    void saveTo(ArmorStand as) {
        as.addScoreboardTag(getTag());
    }

    void removeFrom(ArmorStand as) {
        as.removeScoreboardTag(getTag());
    }

    boolean execute(Player p) {
        if (command == null) {
            return true;
        }

        String cmd = command.replaceAll("%player%", p.getName());
        switch (type) {
            case PLAYER -> {
                return p.performCommand(cmd);
            }
            case CONSOLE -> {
                // Folia: dispatch on the global next tick
                AST.scheduler().runNextTick(task -> {
                    try {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    } catch (CommandException ignored) {
                    }
                });
                return true;
            }
            case BUNGEE -> {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(cmd);
                p.sendPluginMessage(AST.plugin, "BungeeCord", out.toByteArray());
                return true;
            }
        }

        return true;
    }

    static ArmorStandCmd fromTag(String tag) {
        //[0]    [1] [2]       [3]    [4]   [5 - ...]
        //ascmd::v2::priority::delay::type::command
        String[] split = tag.split("::");
        if (split.length < 6) {
            return null;
        }

        if (!split[0].equals("ascmd")) {
            return null;
        }

        int priority;
        try {
            priority = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
            return null;
        }

        int delay;
        try {
            delay = Integer.parseInt(split[3]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (delay < 0) {
            return null;
        }

        CommandType type = CommandType.fromTag(split[4]);
        if (type == null) {
            return null;
        }

        String cmd;
        if (split.length == 6) {
            cmd = split[5];
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 5; i < split.length; i++) {
                sb.append(split[i]).append("::");
            }

            cmd = sb.substring(0, sb.length() - 2);
        }

        if (cmd.charAt(0) == '/') {
            cmd = cmd.substring(1);
        }

        if (cmd.isEmpty()) {
            return null;
        }

        return new ArmorStandCmd(cmd, type, priority, delay);
    }

    static ArmorStandCmd fromLegacyTag(String tag) {
        //ast-cmd-type-command
        if (!tag.startsWith("ast-cmd-")) {
            return null;
        }

        CommandType type = CommandType.fromTag(tag.substring(8, 11));
        if (type == null) {
            return null;
        }

        String cmd = tag.substring(12);
        if (cmd.charAt(0) == '/') {
            cmd = cmd.substring(1);
        }

        if (cmd.isEmpty()) {
            return null;
        }

        return new ArmorStandCmd(cmd, type, 0, 0);
    }

    @Override
    public int compareTo(@NotNull ArmorStandCmd o) {
        return Integer.compare(priority, o.priority);
    }
}

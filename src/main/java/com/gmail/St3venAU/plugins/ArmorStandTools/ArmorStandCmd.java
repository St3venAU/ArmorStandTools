package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import com.google.common.io.ByteStreams;
import com.google.common.io.ByteArrayDataOutput;

class ArmorStandCmd {

    private final ArmorStand armorStand;
    private String command;
    public static enum CmdType {
        Console,
        Player,
        Bungeecord;
    };
    private CmdType cmdtype;

    ArmorStandCmd(ArmorStand as, String command, CmdType cmdtype) {
        this.armorStand = as;
        this.command = command;
        this.cmdtype = cmdtype;
    }

    ArmorStandCmd(ArmorStand as) {
        this.armorStand = as;
        this.command = null;
        for(String tag : armorStand.getScoreboardTags()) {
            if(tag.startsWith("ast-cmd-")) {
                String cmd = tag.substring(8);
                if(cmd.startsWith("con:")) {
                    cmd = cmd.substring(4);
                    if(cmd.charAt(0) == '/') {
                        cmd = cmd.substring(1);
                    }
                    if(cmd.length() == 0) return;
                    this.command = cmd;
                    this.cmdtype = CmdType.Console;
                    return;
                } else if(cmd.startsWith("plr:")) {
                    cmd = cmd.substring(4);
                    if(cmd.charAt(0) == '/') {
                        cmd = cmd.substring(1);
                    }
                    if(cmd.length() == 0) return;
                    this.command = cmd;
                    this.cmdtype = CmdType.Player;
                    return;
                } else if(cmd.startsWith("bun:")) {
                    cmd = cmd.substring(4);
                    if(cmd.charAt(0) == '/') {
                        cmd = cmd.substring(1);
                    }
                    if(cmd.length() == 0) return;
                    this.command = cmd;
                    this.cmdtype = CmdType.Bungeecord;
                    return;
                }
            }
        }
    }

    private String getTag() {
        String typename;
        switch (cmdtype) {
            case Player:
                typename = "plr";
                break;
            case Bungeecord:
                typename = "bun";
                break;
            default:
                typename = "con";
        }
        return "ast-cmd-" + typename + ":" + command;
    }

    String getCommand() {
        return this.command;
    }

    boolean execute(Player p) {
        if(command == null) return true;
        if(isOnCooldown()) {
            p.sendMessage(ChatColor.RED + Config.cmdOnCooldown);
            return true;
        }
        setOnCooldown();
        String cmd = command.contains("%player%") ? command.replaceAll("%player%", p.getName()) : command;
        switch (cmdtype) {
            case Console:
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            case Player:
                return p.performCommand(cmd);
            case Bungeecord:
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(cmd);
                p.sendPluginMessage(AST.plugin, "BungeeCord", out.toByteArray());
                return true;
            default:
                return false;
        }
    }

    private void cleanUpCommand() {
        if(command.charAt(0) == '/') {
            command = command.substring(1);
        }
    }

    boolean save() {
        removeAssignedCommand(armorStand);
        if (command == null) return false;
        cleanUpCommand();
        return command.length() != 0 && armorStand.addScoreboardTag(getTag());
    }

    void cloneTo(ArmorStand clone) {
        if(command == null) return;
        ArmorStandCmd asCmd = new ArmorStandCmd(clone, command, cmdtype);
        asCmd.save();
    }

    String getType() {
        return cmdtype.name();
    }

    static boolean removeAssignedCommand(ArmorStand as) {
        List<String> tags = new ArrayList<>();
        for(String tag : as.getScoreboardTags()) {
            if(tag.startsWith("ast-cmd-")) {
                tags.add(tag);
            }
        }
        for(String tag : tags) {
            as.removeScoreboardTag(tag);
        }
        return tags.size() > 0;
    }

    private void setOnCooldown() {
        int cooldownTime = getCooldownTime();
        if(cooldownTime == -1) {
            cooldownTime = Config.defaultASCmdCooldownTicks;
        }
        if(cooldownTime < 1) return;
        armorStand.setMetadata("ast-cmd-cooldown", new FixedMetadataValue(AST.plugin, true));
        new BukkitRunnable() {
            @Override
            public void run() {
                armorStand.removeMetadata("ast-cmd-cooldown", AST.plugin);
            }
        }.runTaskLater(AST.plugin, cooldownTime);
    }

    private boolean isOnCooldown() {
        return armorStand.hasMetadata("ast-cmd-cooldown");
    }

    // Positive cooldown: Set cooldown time, Negative cooldown: Remove cooldown time
    void setCooldownTime(int cooldown) {
        if(armorStand == null) return;
        List<String> tags = new ArrayList<>();
        for(String tag : armorStand.getScoreboardTags()) {
            if(tag.startsWith("ast-cdn-")) {
                tags.add(tag);
            }
        }
        for(String tag : tags) {
            armorStand.removeScoreboardTag(tag);
        }
        if(cooldown < 0) return;
        armorStand.addScoreboardTag("ast-cdn-" + cooldown);
    }

    private int getCooldownTime() {
        if(armorStand == null) return -1;
        for(String tag : armorStand.getScoreboardTags()) {
            if(tag.startsWith("ast-cdn-")) {
                String[] split = tag.split("ast-cdn-");
                if(split.length < 2 || split[1].length() < 1) return -1;
                try {
                    return Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

}

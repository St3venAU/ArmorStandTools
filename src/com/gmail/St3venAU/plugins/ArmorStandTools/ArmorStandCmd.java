package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

class ArmorStandCmd {

    private String command;
    private final boolean console;

    ArmorStandCmd(String command, boolean console) {
        this.command = command;
        this.console = console;
    }

    static ArmorStandCmd fromAS(ArmorStand as) {
        for(String tag : as.getScoreboardTags()) {
            if(tag.startsWith("ast-cmd-")) {
                String cmd = tag.substring(8);
                if(cmd.startsWith("con:")) {
                    cmd = cmd.substring(4);
                    if(cmd.charAt(0) == '/') {
                        cmd = cmd.substring(1);
                    }
                    if(cmd.length() == 0) return null;
                    return new ArmorStandCmd(cmd, true);
                } else if(cmd.startsWith("plr:")) {
                    cmd = cmd.substring(4);
                    if(cmd.charAt(0) == '/') {
                        cmd = cmd.substring(1);
                    }
                    if(cmd.length() == 0) return null;
                    return new ArmorStandCmd(cmd, false);
                }
            }
        }
        return null;
    }

    private String getTag() {
        return "ast-cmd-" + (console ? "con" : "plr") + ":" + command;
    }

    String getCommand() {
        return this.command;
    }

    boolean execute(Player p) {
        String cmd = command.contains("%player%") ? command.replaceAll("%player%", p.getName()) : command;
        if(console) {
            return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } else {
            return p.performCommand(cmd);
        }
    }

    private void cleanUpCommand() {
        if(command.charAt(0) == '/') {
            command = command.substring(1);
        }
    }

    boolean assignTo(ArmorStand as) {
        removeAssignedCommand(as);
        if (command == null) return false;
        cleanUpCommand();
        return command.length() != 0 && as.addScoreboardTag(getTag());
    }

    static void cloneASCommand(ArmorStand original, ArmorStand clone) {
        ArmorStandCmd asCmd = fromAS(original);
        if(asCmd == null) return;
        asCmd.assignTo(clone);
    }

    String getType() {
        return console ? "Console" : "Player";
    }

    static boolean removeAssignedCommand(ArmorStand as) {
        List<String> tags = new ArrayList<String>();
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


}

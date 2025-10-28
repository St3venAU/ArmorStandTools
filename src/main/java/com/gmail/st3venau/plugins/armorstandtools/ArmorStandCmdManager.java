package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ArmorStandCmdManager {

    static private final String ON_COOLDOWN_TAG = "AstCmdCooldown";

    private final ArmorStand armorStand;
    private final List<ArmorStandCmd> commands = new ArrayList<>();

    ArmorStandCmdManager(ArmorStand as) {
        this.armorStand = as;
        getCommandsFromScoreboardTags();
        commands.sort(null);
    }

    private void getCommandsFromScoreboardTags() {
        Set<String> tags = new HashSet<>(armorStand.getScoreboardTags());
        for (String tag : tags) {
            if (tag.startsWith("ast-cmd-")) {
                ArmorStandCmd command = ArmorStandCmd.fromLegacyTag(tag);
                armorStand.removeScoreboardTag(tag);
                if (command != null) {
                    addCommand(command, true);
                }
            } else if (tag.startsWith("ascmd::v2::")) {
                ArmorStandCmd command = ArmorStandCmd.fromTag(tag);
                if (command != null) {
                    addCommand(command, false);
                }
            }
        }
    }

    public List<ArmorStandCmd> getCommands() {
        commands.sort(null);
        return commands;
    }

    boolean hasCommands() {
        return !commands.isEmpty();
    }

    void addCommand(ArmorStandCmd command, boolean saveToArmorStand) {
        commands.add(command);
        if (saveToArmorStand) {
            command.saveTo(armorStand);
        }
    }

    boolean removeCommand(int index) {
        ArmorStandCmd cmd;
        try {
            cmd = commands.get(index);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

        cmd.removeFrom(armorStand);
        commands.remove(index);
        return true;
    }

    void executeCommands(Player p) {
        if (isOnCooldown()) {
            p.sendMessage(ChatColor.RED + Config.cmdOnCooldown);
            return;
        }

        setOnCooldown();
        long cumulativeDelay = 0;
        for (int n = 0; n < commands.size(); n++) {
            ArmorStandCmd asCmd = commands.get(n);
            cumulativeDelay += asCmd.delay();
            final int num = n + 1;
            AST.scheduler().runLater(task -> {
                if (!p.isOnline()) {
                    return;
                }

                if (!asCmd.execute(p)) {
                    p.sendMessage(ChatColor.RED + Config.errorExecutingCmd + " #" + num);
                }
            }, cumulativeDelay);
        }
    }

    private void setOnCooldown() {
        int cooldownTime = getCooldownTime();
        if (cooldownTime == -1) {
            cooldownTime = Config.defaultASCmdCooldownTicks;
        }

        if (cooldownTime < 1) {
            return;
        }

        AST.scheduler().runLater(task -> {
            armorStand.setMetadata(ON_COOLDOWN_TAG, new FixedMetadataValue(AST.plugin, true));
            armorStand.removeMetadata(ON_COOLDOWN_TAG, AST.plugin);
        }, cooldownTime);
    }

    private boolean isOnCooldown() {
        return armorStand.hasMetadata(ON_COOLDOWN_TAG);
    }

    // Positive cooldown: Set cooldown time, Negative cooldown: Remove cooldown time
    void setCooldownTime(int cooldown) {
        if (armorStand == null) {
            return;
        }

        List<String> tags = new ArrayList<>();
        for (String tag : armorStand.getScoreboardTags()) {
            if (tag.startsWith("ast-cdn-")) {
                tags.add(tag);
            }
        }

        for (String tag : tags) {
            armorStand.removeScoreboardTag(tag);
        }

        if (cooldown < 0) {
            return;
        }

        armorStand.addScoreboardTag("ast-cdn-" + cooldown);
    }

    int getCooldownTime() {
        if (armorStand == null) {
            return -1;
        }

        for (String tag : armorStand.getScoreboardTags()) {
            if (tag.startsWith("ast-cdn-")) {
                String[] split = tag.split("ast-cdn-");
                if (split.length < 2 || split[1].isEmpty()) {
                    return -1;
                }

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

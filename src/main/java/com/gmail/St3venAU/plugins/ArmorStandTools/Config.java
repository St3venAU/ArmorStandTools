package com.gmail.st3venau.plugins.armorstandtools;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

class Config {

    private static AST plugin;
    private static File languageConfigFile;
    private static FileConfiguration languageConfig;

    static WorldGuardPlugin worldGuardPlugin;

    static boolean allowMoveWorld            = false;
    static boolean debug                     = false;
    static boolean requireCreative           = false;
    static boolean ignoreWGForASCmdExecution = false;
    static int defaultASCmdCooldownTicks     = 0;

    static String
            asDropped, asVisible, isTrue, isFalse, carrying,
            cbCreated, size, small, normal, basePlate, isOn,
            isOff, gravity, arms, invul, equip, locked,
            unLocked, notConsole, noAirError, invalidName,
            wgNoPerm, currently, noCommandPerm, generalNoPerm,
            armorStand, none, guiInUse, noASNearBy, closestAS,
            creativeRequired, hasNoCmd, hasCmd, type, command,
            unassignedCmd, assignedCmdToAS, assignCmdError,
            ascmdHelp, viewCmd, removeCmd, assignConsole,
            assignPlayer, executeCmdError, cmdOnCooldown,
            cooldownRemovedFrom, isAnInvalidCooldown,
            cooldownSetTo, ticksFor, setCooldown,
            removeCooldown, glow, instructions, crouch, click,
            finish;

    static void reload(AST main) {
        plugin = main;
        reloadMainConfig();
        saveDefaultLanguageConfig();
        reloadLanguageConfig();
        ArmorStandTool.updateTools(languageConfig);
    }

    private static void reloadMainConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        allowMoveWorld              = config.getBoolean("allowMovingStandsBetweenWorlds");
        requireCreative             = config.getBoolean("requireCreativeForSaveAsCmdBlock");
        defaultASCmdCooldownTicks   = config.getInt("defaultASCmdCooldownTicks");
        ignoreWGForASCmdExecution   = config.getBoolean("bypassWorldguardForASCmdExecution");
        debug                       = config.getBoolean("debug", false);

        AST.activeTool.clear();
        AST.selectedArmorStand.clear();

        for(ArmorStandTool tool : ArmorStandTool.values()) {
            tool.setEnabled(config);
        }
        
        Plugin plotSquared = plugin.getServer().getPluginManager().getPlugin("PlotSquared");
        if (plotSquared != null && plotSquared.isEnabled()) {
            try {
                PlotSquaredHook.init();
                plugin.getLogger().log(Level.INFO, "PlotSquared plugin was found. PlotSquared support enabled.");
            }
            catch (Throwable e) {
                e.printStackTrace();
                plugin.getLogger().log(Level.WARNING, "PlotSquared plugin was found, but there was an error initializing PlotSquared support.");
            }
        } else {
            plugin.getLogger().log(Level.INFO, "PlotSquared plugin not found. Continuing without PlotSquared support.");
        }
        
        Plugin wgp = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if(wgp instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) wgp;
        }
        if(config.getBoolean("integrateWithWorldGuard")) {
            plugin.getLogger().log(Level.INFO, worldGuardPlugin == null ? "WorldGuard plugin not found. Continuing without WorldGuard support." : "WorldGuard plugin found. WorldGuard support enabled.");
        } else if(worldGuardPlugin != null) {
            plugin.getLogger().log(Level.WARNING, "WorldGuard plugin was found, but integrateWithWorldGuard is set to false in config.yml. Continuing without WorldGuard support.");
            worldGuardPlugin = null;
        }
    }

    private static void saveDefaultLanguageConfig() {
        languageConfigFile = new File(plugin.getDataFolder(), "language.yml");
        if (!languageConfigFile.exists()) {
            plugin.saveResource("language.yml", false);
        }
    }

    private static void reloadLanguageConfig() {
        languageConfigFile = new File(plugin.getDataFolder(), "language.yml");
        languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
        InputStream defConfigStream = plugin.getResource("language.yml");
        if (defConfigStream != null) {
            languageConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
        }
        asDropped = languageConfig.getString("asDropped");
        asVisible = languageConfig.getString("asVisible");
        isTrue = languageConfig.getString("isTrue");
        isFalse = languageConfig.getString("isFalse");
        carrying = languageConfig.getString("carrying");
        cbCreated = languageConfig.getString("cbCreated");
        size = languageConfig.getString("size");
        small = languageConfig.getString("small");
        normal = languageConfig.getString("normal");
        basePlate = languageConfig.getString("basePlate");
        isOn = languageConfig.getString("isOn");
        isOff = languageConfig.getString("isOff");
        gravity = languageConfig.getString("gravity");
        arms = languageConfig.getString("arms");
        invul = languageConfig.getString("invul");
        equip = languageConfig.getString("equip");
        locked = languageConfig.getString("locked");
        unLocked = languageConfig.getString("unLocked");
        notConsole = languageConfig.getString("notConsole");
        noAirError = languageConfig.getString("noAirError");
        invalidName = languageConfig.getString("invalidName");
        wgNoPerm = languageConfig.getString("wgNoPerm");
        noCommandPerm = languageConfig.getString("noCommandPerm");
        currently = languageConfig.getString("currently");
        generalNoPerm = languageConfig.getString("generalNoPerm");
        armorStand = languageConfig.getString("armorStand");
        none = languageConfig.getString("none");
        guiInUse = languageConfig.getString("guiInUse");
        noASNearBy = languageConfig.getString("noASNearBy");
        closestAS = languageConfig.getString("closestAS");
        hasNoCmd = languageConfig.getString("hasNoCmd");
        hasCmd = languageConfig.getString("hasCmd");
        type = languageConfig.getString("type");
        command = languageConfig.getString("command");
        unassignedCmd = languageConfig.getString("unassignedCmd");
        assignedCmdToAS = languageConfig.getString("assignedCmdToAS");
        assignCmdError = languageConfig.getString("assignCmdError");
        ascmdHelp = languageConfig.getString("ascmdHelp");
        viewCmd = languageConfig.getString("viewCmd");
        removeCmd = languageConfig.getString("removeCmd");
        assignConsole = languageConfig.getString("assignConsole");
        assignPlayer = languageConfig.getString("assignPlayer");
        executeCmdError = languageConfig.getString("executeCmdError");
        creativeRequired = languageConfig.getString("creativeRequired");
        cmdOnCooldown = languageConfig.getString("cmdOnCooldown");
        cooldownRemovedFrom = languageConfig.getString("cooldownRemovedFrom");
        isAnInvalidCooldown = languageConfig.getString("isAnInvalidCooldown");
        cooldownSetTo = languageConfig.getString("cooldownSetTo");
        ticksFor = languageConfig.getString("ticksFor");
        setCooldown = languageConfig.getString("setCooldown");
        removeCooldown = languageConfig.getString("removeCooldown");
        ticksFor = languageConfig.getString("ticksFor");
        glow = languageConfig.getString("glow");
        instructions = languageConfig.getString("instructions");
        crouch = languageConfig.getString("crouch");
        click = languageConfig.getString("click");
        finish = languageConfig.getString("finish");
    }

}

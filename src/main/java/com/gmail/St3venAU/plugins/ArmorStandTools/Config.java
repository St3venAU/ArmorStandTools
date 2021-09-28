package com.gmail.St3venAU.plugins.ArmorStandTools;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

class Config {

    private static File languageConfigFile;
    private static FileConfiguration languageConfig;
    private static Path summonCommandsLogPath;

    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    static WorldGuardPlugin worldGuardPlugin;

    static ItemStack helmet, chest, pants, boots, itemInHand, itemInOffHand;

    static boolean isVisible                 = true;
    static boolean isSmall                   = false;
    static boolean hasArms                   = true;
    static boolean hasBasePlate              = false;
    static boolean hasGravity                = false;
    static String  defaultName               = "";
    static boolean invulnerable              = false;
    static boolean equipmentLock             = false;
    static boolean allowMoveWorld            = false;
    static boolean deactivateOnWorldChange   = true;
    static boolean showDebugMessages                     = false;
    static boolean requireCreative           = false;
    static int defaultASCmdCooldownTicks     = 0;
    static boolean ignoreWGForASCmdExecution = false;
    static boolean saveToolCreatesCommandBlock  = true;
    static boolean logGeneratedSummonCommands   = false;

    static final ArrayList<String> deniedCommands = new ArrayList<>();

    static String
            invReturned, asDropped, asVisible, isTrue, isFalse,
            carrying, cbCreated, size, small, normal, basePlate,
            isOn, isOff, gravity, arms, invul, equip, locked,
            unLocked, notConsole, giveMsg1, giveMsg2, conReload,
            noRelPerm, noAirError, invalidName, wgNoPerm, currently,
            noCommandPerm, generalNoPerm, armorStand, none,
            guiInUse, noASNearBy, closestAS, creativeRequired,
            hasNoCmd, hasCmd, type, command, unassignedCmd,
            assignedCmdToAS, assignCmdError, ascmdHelp, viewCmd,
            removeCmd, assignConsole, assignPlayer, executeCmdError,
            cmdOnCooldown, cooldownRemovedFrom, isAnInvalidCooldown,
            cooldownSetTo, ticksFor, setCooldown, removeCooldown,
            cmdNotAllowed, glow, crouch, click, finish;

    static void reload() {
        reloadMainConfig();
        saveDefaultLanguageConfig();
        reloadLanguageConfig();
        ArmorStandTool.updateTools(languageConfig);
    }

    private static void reloadMainConfig() {
        AST.plugin.saveDefaultConfig();
        AST.plugin.reloadConfig();
        FileConfiguration config = AST.plugin.getConfig();
        summonCommandsLogPath       = Paths.get("AST-generated-summon-commands.log");
        helmet                      = toItemStack(config.getString("helmet"));
        chest                       = toItemStack(config.getString("chest"));
        pants                       = toItemStack(config.getString("pants"));
        boots                       = toItemStack(config.getString("boots"));
        itemInHand                  = toItemStack(config.getString("inHand"));
        itemInOffHand               = toItemStack(config.getString("inOffHand"));
        isVisible                   = config.getBoolean("isVisible");
        isSmall                     = config.getBoolean("isSmall");
        hasArms                     = config.getBoolean("hasArms");
        hasBasePlate                = config.getBoolean("hasBasePlate");
        hasGravity                  = config.getBoolean("hasGravity");
        defaultName                 = config.getString("name");
        invulnerable                = config.getBoolean("invulnerable");
        equipmentLock               = config.getBoolean("equipmentLock");
        allowMoveWorld              = config.getBoolean("allowMovingStandsBetweenWorlds");
        deactivateOnWorldChange     = config.getBoolean("deactivateToolsOnWorldChange");
        requireCreative             = config.getBoolean("requireCreativeForSaveAsCmdBlock");
        defaultASCmdCooldownTicks   = config.getInt("defaultASCmdCooldownTicks");
        ignoreWGForASCmdExecution   = config.getBoolean("bypassWorldguardForASCmdExecution");
        showDebugMessages           = config.getBoolean("showDebugMessages", false);
        saveToolCreatesCommandBlock = config.getBoolean("saveToolCreatesCommandBlock", true);
        logGeneratedSummonCommands  = config.getBoolean("logGeneratedSummonCommands", false);

        AST.activeTool.clear();
        AST.selectedArmorStand.clear();

        deniedCommands.clear();
        for(String deniedCmd : config.getStringList("deniedCommandsWhileUsingTools")) {
            deniedCmd = deniedCmd.split(" ")[0].toLowerCase();
            while(deniedCmd.length() > 0 && deniedCmd.charAt(0) == '/') {
                deniedCmd = deniedCmd.substring(1);
            }
            if(deniedCmd.length() > 0) {
                deniedCommands.add(deniedCmd);
            }
        }

        for(ArmorStandTool tool : ArmorStandTool.values()) {
            tool.setEnabled(config);
        }

        Plugin plotSquared = AST.plugin.getServer().getPluginManager().getPlugin("PlotSquared");
        if (plotSquared != null && plotSquared.isEnabled()) {
            try {
                PlotSquaredHook.init();
                AST.plugin.getLogger().log(Level.INFO, "PlotSquared plugin was found. PlotSquared support enabled.");
            }
            catch (Throwable e) {
                e.printStackTrace();
                AST.plugin.getLogger().log(Level.WARNING, "PlotSquared plugin was found, but there was an error initializing PlotSquared support.");
            }
        } else {
            AST.plugin.getLogger().log(Level.INFO, "PlotSquared plugin not found. Continuing without PlotSquared support.");
        }

        Plugin wgp = AST.plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        if(wgp instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) wgp;
        }
        if(config.getBoolean("integrateWithWorldGuard")) {
            AST.plugin.getLogger().log(Level.INFO, worldGuardPlugin == null ? "WorldGuard plugin not found. Continuing without WorldGuard support." : "WorldGuard plugin found. WorldGuard support enabled.");
        } else if(worldGuardPlugin != null) {
            AST.plugin.getLogger().log(Level.WARNING, "WorldGuard plugin was found, but integrateWithWorldGuard is set to false in config.yml. Continuing without WorldGuard support.");
            worldGuardPlugin = null;
        }
    }

    static void logSummonCommand(String playerName, String command) {
        List<String> lines = Collections.singletonList("<" + timestampFormat.format(new Timestamp(System.currentTimeMillis())) + " " + playerName + "> " + command);
        try {
            Files.write(summonCommandsLogPath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveDefaultLanguageConfig() {
        languageConfigFile = new File(AST.plugin.getDataFolder(), "language.yml");
        if (!languageConfigFile.exists()) {
            AST.plugin.saveResource("language.yml", false);
        }
    }

    private static void reloadLanguageConfig() {
        languageConfigFile = new File(AST.plugin.getDataFolder(), "language.yml");
        languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
        InputStream defConfigStream = AST.plugin.getResource("language.yml");
        if (defConfigStream != null) {
            languageConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
        }
        invReturned = languageConfig.getString("invReturned");
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
        giveMsg1 = languageConfig.getString("giveMsg1");
        giveMsg2 = languageConfig.getString("giveMsg2");
        conReload = languageConfig.getString("conReload");
        noRelPerm = languageConfig.getString("noRelPerm");
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
        cmdNotAllowed = languageConfig.getString("cmdNotAllowed");
        glow = languageConfig.getString("glow");
        crouch = languageConfig.getString("crouch");
        click = languageConfig.getString("click");
        finish = languageConfig.getString("finish");
    }

    private static ItemStack toItemStack(String s) {
        if(s == null || s.length() == 0) {
            return new ItemStack(Material.AIR);
        }
        Material m;
        try {
            m = Material.valueOf(s.toUpperCase());
        } catch(IllegalArgumentException iae) {
            AST.plugin.getLogger().warning("Error in config.yml: Invalid material name specifed (" + s + "). Continuing using AIR instead.");
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(m, 1);
    }

}

package com.gmail.St3venAU.plugins.ArmorStandTools;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;

class Config {

    private static Main plugin;
    private static File languageConfigFile;
    private static FileConfiguration languageConfig;

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
    static boolean debug                     = false;
    static boolean requireCreative           = false;
    static int defaultASCmdCooldownTicks     = 0;
    static boolean ignoreWGForASCmdExecution = false;

    static final ArrayList<String> deniedCommands = new ArrayList<String>();

    static String
            invReturned, asDropped, asVisible, isTrue, isFalse,
            carrying, cbCreated, size, small, normal, basePlate,
            isOn, isOff, gravity, arms, invul, equip, locked,
            unLocked, notConsole, giveMsg1, giveMsg2, conReload,
            noRelPerm, noAirError, pleaseWait, appliedHead,
            invalidName, wgNoPerm, currently, headFailed,
            noCommandPerm, generalNoPerm, armorStand, none,
            guiInUse, noASNearBy, closestAS, creativeRequired,
            hasNoCmd, hasCmd, type, command, unassignedCmd,
            assignedCmdToAS, assignCmdError, ascmdHelp, viewCmd,
            removeCmd, assignConsole, assignPlayer, executeCmdError,
            cmdOnCooldown, cooldownRemovedFrom, isAnInvalidCooldown,
            cooldownSetTo, ticksFor, setCooldown, removeCooldown,
            cmdNotAllowed, glow;

    static void reload(Main main) {
        plugin = main;
        reload();
    }

    static void reload() {
        reloadMainConfig();
        saveDefaultLanguageConfig();
        reloadLanguageConfig();
        ArmorStandTool.updateTools(languageConfig);
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
        pleaseWait = languageConfig.getString("pleaseWait");
        appliedHead = languageConfig.getString("appliedHead");
        invalidName = languageConfig.getString("invalidName");
        wgNoPerm = languageConfig.getString("wgNoPerm");
        noCommandPerm = languageConfig.getString("noCommandPerm");
        currently = languageConfig.getString("currently");
        headFailed = languageConfig.getString("headFailed");
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
        ticksFor = languageConfig.getString("ticksFor");
        cmdNotAllowed = languageConfig.getString("cmdNotAllowed");
        glow = languageConfig.getString("glow");
    }

    private static void reloadMainConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
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
        debug                       = config.getBoolean("debug", false);
        plugin.carryingArmorStand.clear();

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
        
        Plugin plotSquared = plugin.getServer().getPluginManager().getPlugin("PlotSquared");
        if (plotSquared != null && plotSquared.isEnabled()) {
            try {
                new PlotSquaredHook(plugin);
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
        if(wgp != null && wgp instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) wgp;
        }
        if(config.getBoolean("integrateWithWorldGuard")) {
            plugin.getLogger().log(Level.INFO, worldGuardPlugin == null ? "WorldGuard plugin not found. Continuing without WorldGuard support." : "WorldGuard plugin found. WorldGuard support enabled.");
        } else if(worldGuardPlugin != null) {
            plugin.getLogger().log(Level.WARNING, "WorldGuard plugin was found, but integrateWithWorldGuard is set to false in config.yml. Continuing without WorldGuard support.");
            worldGuardPlugin = null;
        }
    }

    private static void reloadLanguageConfig() {
        languageConfigFile = new File(plugin.getDataFolder(), "language.yml");
        languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
        InputStream defConfigStream = plugin.getResource("language.yml");
        if (defConfigStream != null) {
            languageConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charset.forName("UTF-8"))));
        }
    }

    private static void saveDefaultLanguageConfig() {
        languageConfigFile = new File(plugin.getDataFolder(), "language.yml");
        if (!languageConfigFile.exists()) {
            plugin.saveResource("language.yml", false);
        }
    }

    private static ItemStack toItemStack(String s) {
        if(s == null || s.length() == 0) {
            return new ItemStack(Material.AIR);
        }
        String[] split = s.split(" ");
        if(split.length > 2) {
            plugin.getLogger().warning("Error in config.yml: Must use the format: MATERIAL_NAME dataValue. Continuing using AIR instead.");
            return new ItemStack(Material.AIR);
        }
        byte dataValue = (byte) 0;
        if(split.length == 2) {
            try {
                dataValue = Byte.parseByte(split[1]);
            } catch (NumberFormatException nfe) {
                plugin.getLogger().warning("Error in config.yml: Invalid data value specifed. Continuing using data value 0 instead.");
            }
        }
        Material m;
        try {
            m = Material.valueOf(split[0].toUpperCase());
        } catch(IllegalArgumentException iae) {
            plugin.getLogger().warning("Error in config.yml: Invalid material name specifed. Continuing using AIR instead.");
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(m, 1, dataValue);
    }

}

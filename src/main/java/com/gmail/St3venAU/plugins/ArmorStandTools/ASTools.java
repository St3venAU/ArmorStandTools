package com.gmail.St3venAU.plugins.ArmorStandTools;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class ASTools extends JavaPlugin {

    private static final String LATEST_VERSION = "v1_16_R3";

    private static Object WG_AST_FLAG;

    protected static NMS nms;

    protected final HashMap<UUID, ArmorStand> carryingArmorStand = new HashMap<UUID, ArmorStand>();
    protected final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<UUID, ItemStack[]>();

    protected static ASTools plugin;

    @SuppressWarnings("unchecked")
    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                // Need to do this with reflection for some reason, otherwise plugin load fails when worldguard is not present, even though this code block is not actually executed unless worldguard is present ???
                WG_AST_FLAG = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag").getConstructor(String.class, boolean.class).newInstance("ast", true);
                Class worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(worldGuardClass);
                Object flagRegistry = worldGuardClass.getMethod("getFlagRegistry").invoke(worldGuard);
                flagRegistry.getClass().getMethod("register", Class.forName("com.sk89q.worldguard.protection.flags.Flag")).invoke(flagRegistry, WG_AST_FLAG);
                getLogger().info("Registered custom WorldGuard flag: ast");
            } catch (Exception e) {
                getLogger().info("Failed to register custom WorldGuard flag");
            }
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        if(!loadSpigotVersionSupport()) {
            setEnabled(false);
            return;
        }
        getServer().getPluginManager().registerEvents(new ASToolsListener(this), this);
        Commands cmds = new Commands(this);
        getCommand("astools").setExecutor(cmds);
        getCommand("ascmd").setExecutor(cmds);
        getCommand("ascmd").setTabCompleter(cmds);
        Config.reload(this);
    }

    @Override
    public void onDisable() {
        for(ArmorStand as : carryingArmorStand.values()) {
            returnArmorStand(as);
        }
        carryingArmorStand.clear();
        Player p;
        for(UUID uuid : savedInventories.keySet()) {
            p = getServer().getPlayer(uuid);
            if(p != null && p.isOnline()) {
                revokeToolsSoft(p);
            }
        }
        savedInventories.clear();
    }

    /**
     * @return The current instance of ASTools for hooking into the
     * {@link #revokeToolsSoft(Player)} and {@link #revokeToolsHard(Player)} methods.
     */
    public static ASTools get() {
        return plugin;
    }

    private boolean loadSpigotVersionSupport() {
        String nmsVersion = getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        String usingVersion;
        if(nmsVersion.startsWith("v1_4")  || nmsVersion.startsWith("v1_5")  || nmsVersion.startsWith("v1_6") ||
           nmsVersion.startsWith("v1_7")  || nmsVersion.startsWith("v1_8")  || nmsVersion.startsWith("v1_9") ||
           nmsVersion.startsWith("v1_10") || nmsVersion.startsWith("v1_11") || nmsVersion.startsWith("v1_12")) {
            getLogger().warning("This Craftbukkit/Spigot version is not supported. Craftbukkit/Spigot 1.13+ required. Loading plugin failed.");
            return false;
        }
        try {
            if(NMS.class.isAssignableFrom(Class.forName("com.gmail.St3venAU.plugins.ArmorStandTools.NMS_" + nmsVersion))) {
                usingVersion = nmsVersion;
                getLogger().info("Loading support for " + usingVersion);
            } else {
                usingVersion = LATEST_VERSION;
                getLogger().warning("Support for " + nmsVersion + " not found, trying " + usingVersion + ". Please check for possible updates to the plugin.");
            }
        } catch (Exception e) {
            usingVersion = LATEST_VERSION;
            getLogger().warning("Support for " + nmsVersion + " not found, trying " + usingVersion + ". Please check for possible updates to the plugin.");
        }
        try {
            nms = (NMS) Class.forName("com.gmail.St3venAU.plugins.ArmorStandTools.NMS_" + usingVersion).getConstructor(String.class).newInstance(nmsVersion);
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("An error occurred while attempting to load support for this version of Craftbukkit/Spigot. Loading plugin failed.");
            return false;
        }
        return true;
    }

    protected void returnArmorStand(ArmorStand as) {
        if(as.hasMetadata("startLoc")) {
            for (MetadataValue value : as.getMetadata("startLoc")) {
                if (value.getOwningPlugin() == this) {
                    as.teleport((Location) value.value());
                    as.removeMetadata("startLoc", this);
                    return;
                }
            }
        }
        as.remove();
    }

    private void removeAllTools(Player p) {
        PlayerInventory i = p.getInventory();
        for(ArmorStandTool t : ArmorStandTool.values()) {
            i.remove(t.getItem());
        }
    }

    protected void saveInventoryAndClear(Player p) {
        ItemStack[] inv = p.getInventory().getContents().clone();
        savedInventories.put(p.getUniqueId(), inv);
        p.getInventory().clear();
    }

    /**
     * Toggles whether the player in question has tools active, and upon completion
     * returns the result of toggling. False being that the tools have been revoked,
     * and True being that the tools have been added.
     * @param p The player in question.
     * @param hard Whether or not to use the hard method if revoking tools.
     * @return The toggle result.
     */
    public boolean toggleTools(Player p, boolean hard) {
        if (savedInventories.containsKey(p.getUniqueId())) {
            if (hard) {
                revokeToolsHard(p);
            } else {
                revokeToolsSoft(p);
            }
            return false;
        } else {
            giveTools(p);
            return true;
        }
    }

    /**
     * Saves the given player's inventory, clears it, then gives them the ASTools.
     * If the player already has ASTools this will do nothing.
     * @param p The player in question.
     */
    public void giveTools(Player p) {
        if (!savedInventories.containsKey(p.getUniqueId())) {
            saveInventoryAndClear(p);
            ArmorStandTool.give(p);
            p.sendMessage(ChatColor.GREEN + Config.giveMsg1);
            p.sendMessage(ChatColor.AQUA + Config.giveMsg2);
        }
    }

    /**
     * Restores the inventory of the player in question while keeping any non-tool
     * items the player had in their inventory. If restoring creates too many then
     * it drops the extra on the ground.
     * @param p The player in question.
     */
    public void revokeToolsSoft(Player p) {
        removeAllTools(p);
        UUID uuid = p.getUniqueId();
        ItemStack[] savedInv = savedInventories.get(uuid);
        if(savedInv == null) return;
        PlayerInventory plrInv = p.getInventory();
        ItemStack[] newItems = plrInv.getContents().clone();
        plrInv.setContents(savedInv);
        savedInventories.remove(uuid);
        for(ItemStack i : newItems) {
            if(i == null) continue;
            HashMap<Integer, ItemStack> couldntFit = plrInv.addItem(i);
            for (ItemStack is : couldntFit.values()) {
                p.getWorld().dropItem(p.getLocation(), is);
            }
        }
        p.sendMessage(ChatColor.GREEN + Config.invReturned);
    }

    /**
     * Restores the inventory of the player in question, destroying all items
     * spawned or collected while in their ASTools inventory.
     * @param p The player in question.
     */
    public void revokeToolsHard(Player p) {
        UUID uuid = p.getUniqueId();
        ItemStack[] savedInv = savedInventories.get(uuid);
        if(savedInv != null) {
            p.getInventory().setContents(savedInv);
        } else {
            removeAllTools(p);
        }
        savedInventories.remove(uuid);
        p.sendMessage(ChatColor.GREEN + Config.invReturned);
    }

    protected void pickUpArmorStand(ArmorStand as, Player p, boolean newlySummoned) {
        carryingArmorStand.put(p.getUniqueId(), as);
        if(newlySummoned) return;
        as.setMetadata("startLoc", new FixedMetadataValue(this, as.getLocation()));
    }

    protected void setName(Player p, ArmorStand as) {
        Block b = Utils.findAnAirBlock(p.getLocation());
        if(b == null) {
            p.sendMessage(ChatColor.RED + Config.noAirError);
            return;
        }
        b.setType(Material.OAK_SIGN);
        nms.openSign(p, b);
        b.setMetadata("armorStand", new FixedMetadataValue(this, as.getUniqueId()));
        b.setMetadata("setName", new FixedMetadataValue(this, true));
    }

    protected void setPlayerSkull(Player p, ArmorStand as) {
        Block b = Utils.findAnAirBlock(p.getLocation());
        if(b == null) {
            p.sendMessage(ChatColor.RED + Config.noAirError);
            return;
        }
        b.setType(Material.OAK_SIGN);
        nms.openSign(p, b);
        b.setMetadata("armorStand", new FixedMetadataValue(this, as.getUniqueId()));
        b.setMetadata("setSkull", new FixedMetadataValue(this, true));
    }

    protected boolean checkBlockPermission(Player p, Block b) {
        if(b == null) return true;
        debug("PlotSquaredHook.api: " + PlotSquaredHook.api);
        if (PlotSquaredHook.api != null) {
            Location l = b.getLocation();
            debug("PlotSquaredHook.isPlotWorld(l): " + PlotSquaredHook.isPlotWorld(l));
            if(PlotSquaredHook.isPlotWorld(l)) {
                return PlotSquaredHook.checkPermission(p, l);
            }
        }
        if(Config.worldGuardPlugin != null) {
            if(!Utils.hasPermissionNode(p, "astools.bypass-wg-flag") && !getWorldGuardAstFlag(b.getLocation())) {
                return false;
            }
            return Config.worldGuardPlugin.createProtectionQuery().testBlockBreak(p, b);
        }
        BlockBreakEvent breakEvent = new BlockBreakEvent(b, p);
        Bukkit.getServer().getPluginManager().callEvent(breakEvent);
        return !breakEvent.isCancelled();
    }

    private boolean getWorldGuardAstFlag(Location l) {
        if (l != null) {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = regionContainer.get(BukkitAdapter.adapt(l.getWorld()));
            if (regions == null) return true;
            return regions.getApplicableRegions(BukkitAdapter.asBlockVector(l)).testState(null, (StateFlag) WG_AST_FLAG);
        } else {
            return false;
        }
    }

    protected boolean playerHasPermission(Player p, Block b, ArmorStandTool tool) {
        debug("tool: " + tool);
        if(tool != null) {
            debug("en: " + tool.isEnabled());
            debug("perm: " + Utils.hasPermissionNode(p, tool.getPermission()));
        }
        return (tool == null || (tool.isEnabled() && Utils.hasPermissionNode(p, tool.getPermission()))) && checkBlockPermission(p, b);
    }

    protected void debug(String msg) {
        if(Config.debug) {
            getLogger().log(Level.INFO, "[DEBUG] " + msg);
        }
    }
}
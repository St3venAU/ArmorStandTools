package com.gmail.st3venau.plugins.armorstandtools;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class AST extends JavaPlugin {

    private static Object WG_AST_FLAG;

    final static HashMap<UUID, ArmorStandTool> activeTool = new HashMap<>();
    final static HashMap<UUID, ArmorStand> selectedArmorStand = new HashMap<>();

    public static final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();

    static final HashMap<UUID, AbstractMap.SimpleEntry<UUID, WrappedTask>> waitingForName  = new HashMap<>(); // Player UUID, <ArmorStand UUID, Task>
    static final HashMap<UUID, AbstractMap.SimpleEntry<UUID, WrappedTask>> waitingForSkull = new HashMap<>(); // Player UUID, <ArmorStand UUID, Task>

    static AST plugin;

    private static PlatformScheduler scheduler;

    static final Pattern MC_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,16}$");

    public static PlatformScheduler scheduler() {
        return scheduler;
    }

    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                // Need to do this with reflection for some reason, otherwise plugin load fails when worldguard is not present, even though this code block is not actually executed unless worldguard is present ???
                WG_AST_FLAG = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag").getConstructor(String.class, boolean.class).newInstance("ast", true);
                Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(worldGuardClass);
                Object flagRegistry = worldGuardClass.getMethod("getFlagRegistry").invoke(worldGuard);
                flagRegistry.getClass().getMethod("register", Class.forName("com.sk89q.worldguard.protection.flags.Flag")).invoke(flagRegistry, WG_AST_FLAG);
                getLogger().info("Registered custom WorldGuard flag: ast");
            } catch (Exception e) {
                getLogger().warning("Failed to register custom WorldGuard flag");
            }
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        FoliaLib foliaLib = new FoliaLib(this);
        scheduler = foliaLib.getScheduler();
        getServer().getPluginManager().registerEvents(new  MainListener(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Commands cmds = new Commands();
        PluginCommand command = getCommand("astools");
        if (command != null) {
            command.setExecutor(cmds);
        }

        command = getCommand("ascmd");
        if (command != null) {
            command.setExecutor(cmds);
            command.setTabCompleter(cmds);
        }

        Config.reload();
        ArmorStandGUI.init();
        scheduler.runTimer(task -> {
            for (UUID uuid : activeTool.keySet()) {
                Player p = getServer().getPlayer(uuid);
                ArmorStandTool tool = activeTool.get(uuid);
                if (p != null && tool != null && p.isOnline() && selectedArmorStand.containsKey(uuid)) {
                    tool.use(p, selectedArmorStand.get(uuid));
                }
            }
        }, 3L, 3L);
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        for (UUID uuid : activeTool.keySet()) {
            if (ArmorStandTool.MOVE != activeTool.get(uuid)) {
                continue;
            }

            ArmorStand as = selectedArmorStand.get(uuid);
            if (as != null && !as.isDead()) {
                returnArmorStand(as);
                selectedArmorStand.remove(uuid);
                activeTool.remove(uuid);
            }
        }

        Player p;
        for (UUID uuid : savedInventories.keySet()) {
            p = getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) {
                restoreInventory(p);
            }
        }

        savedInventories.clear();
        waitingForName.clear();
        waitingForSkull.clear();
    }

    static void returnArmorStand(ArmorStand as) {
        if (as == null) {
            return;
        }

        if (as.hasMetadata("clone")) {
            scheduler.runAtEntity(as, task -> as.remove());
            return;
        }

        if (as.hasMetadata("startLoc")) {
            for (MetadataValue metaData : as.getMetadata("startLoc")) {
                if (metaData.getOwningPlugin() == plugin) {
                    Location l = (Location) metaData.value();
                    if (l != null) {
                        scheduler.runAtEntity(as, task -> as.teleportAsync(l));
                        as.removeMetadata("startLoc", plugin);
                        return;
                    }
                }
            }
        }

        scheduler.runAtEntity(as, task -> as.remove());
    }

    private static boolean matches(ItemStack one, ItemStack two) {
        if (one == null || two == null || one.getItemMeta() == null || two.getItemMeta() == null) {
            return false;
        }

        String NameOne = one.getItemMeta().getDisplayName();
        List<String> LoreOne = one.getItemMeta().getLore();
        if (LoreOne == null) {
            return false;
        }

        String NameTwo = two.getItemMeta().getDisplayName();
        List<String> LoreTwo = two.getItemMeta().getLore();
        if (LoreTwo == null) {
            return false;
        }

        return NameOne.equals(NameTwo) && LoreOne.equals(LoreTwo);
    }
    private static void removeAllTools(Player p) {
        PlayerInventory i = p.getInventory();
        for (ArmorStandTool t : ArmorStandTool.values()) {
            for (int slot = 0; slot < i.getSize(); slot++) {
                if (matches(t.getItem(), i.getItem(slot))) {
                    i.setItem(slot, null);
                }
            }
        }
    }

    void saveInventoryAndClear(Player p) {
        ItemStack[] inv = p.getInventory().getContents().clone();
        savedInventories.put(p.getUniqueId(), inv);
        p.getInventory().clear();
    }

    static void restoreInventory(Player p) {
        removeAllTools(p);
        UUID uuid = p.getUniqueId();
        ItemStack[] savedInv = savedInventories.get(uuid);
        if (savedInv == null) {
            return;
        }

        PlayerInventory plrInv = p.getInventory();
        ItemStack[] newItems = plrInv.getContents().clone();
        plrInv.setContents(savedInv);
        savedInventories.remove(uuid);
        for (ItemStack i : newItems) {
            if (i == null) {
                continue;
            }

            HashMap<Integer, ItemStack> couldntFit = plrInv.addItem(i);
            for (ItemStack is : couldntFit.values()) {
                scheduler.runAtEntity(p, task -> p.getWorld().dropItem(p.getLocation(), is));
            }
        }

        p.sendMessage(ChatColor.GREEN + Config.invReturned);
    }

    static ArmorStand getCarryingArmorStand(Player p) {
        UUID uuid = p.getUniqueId();
        return ArmorStandTool.MOVE == AST.activeTool.get(uuid) ? AST.selectedArmorStand.get(uuid) : null;
    }

    static void pickUpArmorStand(ArmorStand as, Player p, boolean newlySummoned) {
        UUID uuid = p.getUniqueId();
        ArmorStand carrying = getCarryingArmorStand(p);
        if (carrying != null && !carrying.isDead()) {
            returnArmorStand(carrying);
        }

        activeTool.put(uuid, ArmorStandTool.MOVE);
        selectedArmorStand.put(uuid, as);
        if (newlySummoned) {
            return;
        }

        as.setMetadata("startLoc", new FixedMetadataValue(AST.plugin, as.getLocation()));
    }

    static void setName(final Player p, ArmorStand as) {
        final UUID uuid = p.getUniqueId();
        if (waitingForSkull.containsKey(uuid)) {
            WrappedTask wrappedTask = waitingForSkull.get(uuid).getValue();
            scheduler.cancelTask(wrappedTask);
            waitingForSkull.remove(uuid);
        }

        if (Config.useCommandForTextInput) {
            String msg1 = ChatColor.GOLD + Config.enterNameC + ": " + ChatColor.GREEN + "/ast <Armor Stand Name>";
            p.sendTitle(" ", msg1, 0, 600, 0);
            p.sendMessage(msg1);
            p.sendMessage(ChatColor.GOLD + Config.enterNameC2 + ": " + ChatColor.GREEN + "/ast &");
        } else {
            p.sendTitle(" ", ChatColor.GOLD + Config.enterName, 0, 600, 0);
            p.sendMessage(ChatColor.GOLD + Config.enterName2 + " &");
        }

        WrappedTask wrappedTask = scheduler.runLater(() -> {
            if (!waitingForName.containsKey(uuid)) {
                return;
            }

            waitingForName.remove(uuid);
            p.sendMessage(ChatColor.RED + Config.inputTimeout);
        }, 600L);
        waitingForName.put(uuid, new AbstractMap.SimpleEntry<>(as.getUniqueId(), wrappedTask));
    }

    static void setPlayerSkull(final Player p, ArmorStand as) {
        final UUID uuid = p.getUniqueId();
        if (waitingForName.containsKey(uuid)) {
            WrappedTask wrappedTask = waitingForName.get(uuid).getValue();
            scheduler.cancelTask(wrappedTask);
            waitingForName.remove(uuid);
        }

        if (Config.useCommandForTextInput) {
            String msg1 = ChatColor.GOLD + Config.enterSkullC + ": " + ChatColor.GREEN + "/ast <MC Username For Skull>";
            p.sendTitle(" ", msg1, 0, 600, 0);
            p.sendMessage(msg1);
        } else {
            p.sendTitle(" ", ChatColor.GOLD + Config.enterSkull, 0, 600, 0);
            p.sendMessage(ChatColor.GOLD + Config.enterSkull);
        }

        WrappedTask wrappedTask = scheduler.runLater(() -> {
            if (!waitingForSkull.containsKey(uuid)) {
                return;
            }

            waitingForSkull.remove(uuid);
            p.sendMessage(ChatColor.RED + Config.inputTimeout);
        }, 600L);
        waitingForSkull.put(uuid, new AbstractMap.SimpleEntry<>(as.getUniqueId(), wrappedTask));
    }

    static boolean checkBlockPermission(Player p, Block b) {
        if (b == null) {
            return true;
        }

        if (PlotSquaredHook.api != null) {
            Location l = b.getLocation();
            if (PlotSquaredHook.isPlotWorld(l)) {
                Boolean hasPermission = PlotSquaredHook.checkPermission(p, l);
                if (hasPermission != null) {
                    return hasPermission;
                }
            }
        }

        if (Config.worldGuardPlugin != null) {
            if (!Utils.hasPermissionNode(p, "astools.bypass-wg-flag") && !getWorldGuardAstFlag(b.getLocation())) {
                return false;
            }

            if (!Config.worldGuardPlugin.createProtectionQuery().testBlockBreak(p, b)) {
                return false;
            }
        }

        BlockBreakEvent breakEvent = new BlockBreakEvent(b, p);
        Bukkit.getServer().getPluginManager().callEvent(breakEvent);
        return !breakEvent.isCancelled();
    }

    private static boolean getWorldGuardAstFlag(Location l) {
        if (l != null && l.getWorld() != null) {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = regionContainer.get(BukkitAdapter.adapt(l.getWorld()));
            if (regions == null) {
                return true;
            }

            return regions.getApplicableRegions(BukkitAdapter.asBlockVector(l)).testState(null, (StateFlag) WG_AST_FLAG);
        } else {
            return false;
        }
    }

    static boolean playerHasPermission(Player p, Block b, ArmorStandTool tool) {
        String permNode = tool == null ? "astools.use" : tool.getPermission();
        boolean enabled = tool == null || tool.isEnabled();
        boolean hasNode = Utils.hasPermissionNode(p, permNode);
        boolean blockPerm = checkBlockPermission(p, b);
        if (Config.showDebugMessages) {
            AST.debug("Plr: " + p.getName() + ", Tool: " + tool + ", Tool En: " + enabled + ", Perm: " + permNode + ", Has Perm: " + hasNode + ", Location Perm: " + blockPerm);
        }

        return enabled && hasNode && blockPerm;
    }

    static void debug(String msg) {
        if (!Config.showDebugMessages) {
            return;
        }

        plugin.getLogger().log(Level.INFO, "[AST DEBUG] " + msg);
    }

    static ArmorStand getArmorStand(UUID uuid, World w) {
        if (uuid != null && w != null) {
            for (org.bukkit.entity.Entity e : w.getEntities()) {
                if (e instanceof ArmorStand && e.getUniqueId().equals(uuid)) {
                    return (ArmorStand) e;
                }
            }
        }

        return null;
    }

    static ItemStack getPlayerHead(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getServer().getPlayer(playerName);
        if (offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        }

        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) {
            plugin.getLogger().warning("Skull item meta was null");
            return item;
        }

        meta.setOwningPlayer(offlinePlayer);
        item.setItemMeta(meta);
        return item;
    }

    static boolean processInput(Player p, final String in) {
        final UUID plrUuid = p.getUniqueId();
        final UUID uuid;
        boolean name;
        WrappedTask task;
        if (AST.waitingForName.containsKey(plrUuid)) {
            uuid = AST.waitingForName.get(plrUuid).getKey();
            task = AST.waitingForName.get(plrUuid).getValue();
            name = true;
        } else if (AST.waitingForSkull.containsKey(plrUuid)) {
            uuid = AST.waitingForSkull.get(plrUuid).getKey();
            task = AST.waitingForSkull.get(plrUuid).getValue();
            name = false;
        } else {
            return false;
        }

        scheduler.cancelTask(task);

        scheduler.runAtEntity(p, t -> {
            final ArmorStand as = getArmorStand(uuid, p.getWorld());
            if (as != null) {
                String input = ChatColor.translateAlternateColorCodes('&', in);
                if (input.equals("&")) {
                    input = "";
                }

                if (name) {
                    if (!input.isEmpty()) {
                        as.setCustomName(input);
                        as.setCustomNameVisible(true);
                        p.sendMessage(ChatColor.GREEN + Config.nameSet);
                    } else {
                        as.setCustomName("");
                        as.setCustomNameVisible(false);
                        as.setCustomNameVisible(false);
                        p.sendMessage(ChatColor.GREEN + Config.nameRemoved);
                    }
                } else {
                    if (MC_USERNAME_PATTERN.matcher(input).matches()) {
                        as.getEquipment();
                        as.getEquipment().setHelmet(getPlayerHead(input));
                        p.sendMessage(ChatColor.GREEN + Config.skullSet);
                    } else {
                        p.sendMessage(ChatColor.RED + input + " " + Config.invalidName);
                    }
                }
            }

            AST.waitingForName.remove(plrUuid);
            AST.waitingForSkull.remove(plrUuid);
            Utils.title(p, " ");
        });
        return true;
    }
}

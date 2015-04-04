package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin {

    public final HashMap<UUID, ArmorStand> carryingArmorStand = new HashMap<UUID, ArmorStand>();
    public final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<UUID, ItemStack[]>();
    private final EulerAngle zero = new EulerAngle(0D, 0D, 0D);
    static String NMS_VERSION;

    @Override
    public void onEnable() {
        NMS_VERSION = getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
        CommandExecutor ce = new Commands(this);
        getCommand("astools").setExecutor(ce);
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
                p.getInventory().setContents(savedInventories.get(uuid));
                p.sendMessage(ChatColor.GREEN + Config.invReturned);
            }
        }
        savedInventories.clear();
    }

    void returnArmorStand(ArmorStand as) {
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

    void pickUpArmorStand(ArmorStand as, Player p, boolean newlySummoned) {
        carryingArmorStand.put(p.getUniqueId(), as);
        if(newlySummoned) return;
        as.setMetadata("startLoc", new FixedMetadataValue(this, as.getLocation()));
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    void generateCmdBlock(Location l, ArmorStand as) {
        Location loc = as.getLocation();
        int dSlots = NBT.getDisabledSlots(as);
        int hand = as.getItemInHand() == null ? 0 : as.getItemInHand().getTypeId();
        int handDmg = as.getItemInHand() == null ? 0 : as.getItemInHand().getDurability();
        int boots = as.getBoots() == null ? 0 : as.getBoots().getTypeId();
        int bootsDmg = as.getBoots() == null ? 0 : as.getBoots().getDurability();
        int legs = as.getLeggings() == null ? 0 : as.getLeggings().getTypeId();
        int legsDmg = as.getLeggings() == null ? 0 : as.getLeggings().getDurability();
        int chest = as.getChestplate() == null ? 0 : as.getChestplate().getTypeId();
        int chestDmg = as.getChestplate() == null ? 0 : as.getChestplate().getDurability();
        int helm = as.getHelmet() == null ? 0 : as.getHelmet().getTypeId();
        int helmDmg = as.getHelmet() == null ? 0 : as.getHelmet().getDurability();
        EulerAngle he = as.getHeadPose();
        EulerAngle ll = as.getLeftLegPose();
        EulerAngle rl = as.getRightLegPose();
        EulerAngle la = as.getLeftArmPose();
        EulerAngle ra = as.getRightArmPose();
        EulerAngle bo = as.getBodyPose();
        String cmd =
                "summon ArmorStand " + Utils.twoDec(loc.getX()) + " " + Utils.twoDec(loc.getY()) + " " + Utils.twoDec(loc.getZ()) + " {"
                        + (as.getMaxHealth() != 20    ? "Attributes:[{Name:\"generic.maxHealth\", Base:" + as.getMaxHealth() + "}]," : "")
                        + (as.isVisible()             ? "" : "Invisible:1,")
                        + (as.hasBasePlate()          ? "" : "NoBasePlate:1,")
                        + (as.hasGravity()            ? "" : "NoGravity:1,")
                        + (as.hasArms()               ? "ShowArms:1," : "")
                        + (as.isSmall()               ? "Small:1," : "")
                        + (NBT.isInvulnerable(as)     ? "Invulnerable:1," : "")
                        + (dSlots == 0                ? "" : ("DisabledSlots:" + dSlots + ","))
                        + (as.isCustomNameVisible()   ? "CustomNameVisible:1," : "")
                        + (as.getCustomName() == null ? "" : ("CustomName:\"" + as.getCustomName() + "\","))
                        + (loc.getYaw() == 0F         ? "" : ("Rotation:[" + Utils.twoDec(loc.getYaw()) + "f],"))
                        + (hand == 0 && boots == 0 && legs == 0 && chest == 0 && helm == 0 ? "" : (
                        "Equipment:["
                                + "{id:" + hand  + ",Count:" + as.getItemInHand().getAmount() + ",Damage:" + handDmg  + NBT.getItemStackTags(as.getItemInHand()) + "},"
                                + "{id:" + boots + ",Count:" + as.getBoots().getAmount() + ",Damage:" + bootsDmg + NBT.getItemStackTags(as.getBoots()) + "},"
                                + "{id:" + legs  + ",Count:" + as.getLeggings().getAmount() + ",Damage:" + legsDmg  + NBT.getItemStackTags(as.getLeggings()) + "},"
                                + "{id:" + chest + ",Count:" + as.getChestplate().getAmount() + ",Damage:" + chestDmg + NBT.getItemStackTags(as.getChestplate()) + "},"
                                + "{id:" + helm  + ",Count:" + as.getHelmet().getAmount() + ",Damage:" + helmDmg  + NBT.getItemStackTags(as.getHelmet()) + NBT.skullOwner(as.getHelmet()) + "}],"))
                        + "Pose:{"
                        + (bo.equals(zero) ? "" : ("Body:["     + Utils.angle(bo.getX()) + "f," + Utils.angle(bo.getY()) + "f," + Utils.angle(bo.getZ()) + "f],"))
                        + (he.equals(zero) ? "" : ("Head:["     + Utils.angle(he.getX()) + "f," + Utils.angle(he.getY()) + "f," + Utils.angle(he.getZ()) + "f],"))
                        + (ll.equals(zero) ? "" : ("LeftLeg:["  + Utils.angle(ll.getX()) + "f," + Utils.angle(ll.getY()) + "f," + Utils.angle(ll.getZ()) + "f],"))
                        + (rl.equals(zero) ? "" : ("RightLeg:[" + Utils.angle(rl.getX()) + "f," + Utils.angle(rl.getY()) + "f," + Utils.angle(rl.getZ()) + "f],"))
                        + (la.equals(zero) ? "" : ("LeftArm:["  + Utils.angle(la.getX()) + "f," + Utils.angle(la.getY()) + "f," + Utils.angle(la.getZ()) + "f],"))
                        + "RightArm:[" + Utils.angle(ra.getX()) + "f," + Utils.angle(ra.getY()) + "f," + Utils.angle(ra.getZ()) + "f]}}";
        Block b = l.getBlock();
        b.setType(Material.COMMAND);
        b.setData((byte) 0);
        CommandBlock cb = (CommandBlock) b.getState();
        cb.setCommand(cmd);
        cb.update();
    }

    ArmorStand clone(ArmorStand as) {
        ArmorStand clone = (ArmorStand) as.getWorld().spawnEntity(as.getLocation().add(1, 0, 0), EntityType.ARMOR_STAND);
        clone.setGravity(as.hasGravity());
        clone.setHelmet(as.getHelmet());
        clone.setChestplate(as.getChestplate());
        clone.setLeggings(as.getLeggings());
        clone.setBoots(as.getBoots());
        clone.setItemInHand(as.getItemInHand());
        clone.setHeadPose(as.getHeadPose());
        clone.setBodyPose(as.getBodyPose());
        clone.setLeftArmPose(as.getLeftArmPose());
        clone.setRightArmPose(as.getRightArmPose());
        clone.setLeftLegPose(as.getLeftLegPose());
        clone.setRightLegPose(as.getRightLegPose());
        clone.setVisible(as.isVisible());
        clone.setBasePlate(as.hasBasePlate());
        clone.setArms(as.hasArms());
        clone.setCustomName(as.getCustomName());
        clone.setCustomNameVisible(as.isCustomNameVisible());
        clone.setSmall(as.isSmall());
        clone.setMaxHealth(as.getMaxHealth());
        NBT.setSlotsDisabled(clone, NBT.getDisabledSlots(as) == 2039583);
        NBT.setInvulnerable(clone, NBT.isInvulnerable(as));
        return clone;
    }

    @SuppressWarnings("deprecation")
    void setName(Player p, ArmorStand as) {
        Block b = Utils.findAnAirBlock(p.getLocation());
        if(b == null) {
            p.sendMessage(ChatColor.RED + Config.noAirError);
            return;
        }
        b.setData((byte) 0);
        b.setType(Material.SIGN_POST);
        Utils.openSign(p, b);
        b.setMetadata("armorStand", new FixedMetadataValue(this, as.getUniqueId()));
        b.setMetadata("setName", new FixedMetadataValue(this, true));
    }

    @SuppressWarnings("deprecation")
    void setPlayerSkull(Player p, ArmorStand as) {
        Block b = Utils.findAnAirBlock(p.getLocation());
        if(b == null) {
            p.sendMessage(ChatColor.RED + Config.noAirError);
            return;
        }
        b.setData((byte) 0);
        b.setType(Material.SIGN_POST);
        Utils.openSign(p, b);
        b.setMetadata("armorStand", new FixedMetadataValue(this, as.getUniqueId()));
        b.setMetadata("setSkull", new FixedMetadataValue(this, true));
    }

    boolean checkPermission(Player player, Block block) {

        if(block == null) return true;

        // Check PlotSquared
        Location loc = block.getLocation();
        if (PlotSquaredHook.api != null) {
            if (PlotSquaredHook.isPlotWorld(loc)) {
                return PlotSquaredHook.checkPermission(player, loc);
            }
        }

        // check WorldGuard
        if(Config.worldGuardPlugin != null) {
            return Config.worldGuardPlugin.canBuild(player, block);
        }

        // Use standard permission checking (will support basically any plugin)
        BlockBreakEvent myBreak = new BlockBreakEvent(block, player);
        Bukkit.getServer().getPluginManager().callEvent(myBreak);
        boolean hasPerm = !myBreak.isCancelled();
        BlockPlaceEvent place = new BlockPlaceEvent(block, block.getState(), block, null, player, true);
        Bukkit.getServer().getPluginManager().callEvent(place);
        if (place.isCancelled()) {
            hasPerm = false;
        }
        return hasPerm;
    }

    boolean playerHasPermission(Player p, Block b, ArmorStandTool tool) {
        return !(tool != null && !p.isOp()
                    && (!Utils.hasPermissionNode(p, "astools.use")
                        || (ArmorStandTool.SAVE == tool && !Utils.hasPermissionNode(p, "astools.cmdblock"))
                        || (ArmorStandTool.CLONE == tool && !Utils.hasPermissionNode(p, "astools.clone"))))
                    && checkPermission(p, b);
    }
}
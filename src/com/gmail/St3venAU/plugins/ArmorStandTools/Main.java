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
    static boolean oneNine, oneNineFour;

    @Override
    public void onEnable() {
        NMS_VERSION = getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        oneNine = NMS_VERSION.startsWith("v1_9");
        oneNineFour = NMS_VERSION.startsWith("v1_9_R2");
        getServer().getPluginManager().registerEvents(new  MainListener(this), this);
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
        String hand, boots, legs, chest, helm, offHand = "0";
        int handDmg, offHandDmg = 0;

        if(oneNine) {
            hand = as.getEquipment().getItemInMainHand() == null ? "air" : Utils.getNmsName(as.getEquipment().getItemInMainHand().getType());
            offHand = as.getEquipment().getItemInOffHand() == null ? "air" : Utils.getNmsName(as.getEquipment().getItemInOffHand().getType());
            boots = as.getBoots() == null ? "air" : Utils.getNmsName(as.getBoots().getType());
            legs = as.getLeggings() == null ? "air" : Utils.getNmsName(as.getLeggings().getType());
            chest = as.getChestplate() == null ? "air" : Utils.getNmsName(as.getChestplate().getType());
            helm = as.getHelmet() == null ? "air" : Utils.getNmsName(as.getHelmet().getType());
            handDmg = as.getEquipment().getItemInMainHand() == null ? 0 : as.getEquipment().getItemInMainHand().getDurability();
            offHandDmg = as.getEquipment().getItemInOffHand() == null ? 0 : as.getEquipment().getItemInOffHand().getDurability();
        } else {
            hand = as.getItemInHand() == null ? "0" : String.valueOf(as.getItemInHand().getTypeId());
            boots = as.getBoots() == null ? "0" : String.valueOf(as.getBoots().getTypeId());
            legs = as.getLeggings() == null ? "0" : String.valueOf(as.getLeggings().getTypeId());
            chest = as.getChestplate() == null ? "0" : String.valueOf(as.getChestplate().getTypeId());
            helm = as.getHelmet() == null ? "0" : String.valueOf(as.getHelmet().getTypeId());
            handDmg = as.getItemInHand() == null ? 0 : as.getItemInHand().getDurability();
        }

        int bootsDmg = as.getBoots() == null ? 0 : as.getBoots().getDurability();
        int legsDmg = as.getLeggings() == null ? 0 : as.getLeggings().getDurability();
        int chestDmg = as.getChestplate() == null ? 0 : as.getChestplate().getDurability();
        int helmDmg = as.getHelmet() == null ? 0 : as.getHelmet().getDurability();
        EulerAngle he = as.getHeadPose();
        EulerAngle ll = as.getLeftLegPose();
        EulerAngle rl = as.getRightLegPose();
        EulerAngle la = as.getLeftArmPose();
        EulerAngle ra = as.getRightArmPose();
        EulerAngle bo = as.getBodyPose();
        String cmd;
        if(oneNine) {
            cmd = "summon ArmorStand " + Utils.twoDec(loc.getX()) + " " + Utils.twoDec(loc.getY()) + " " + Utils.twoDec(loc.getZ()) + " {"
                    + (as.getMaxHealth() != 20 ? "Attributes:[{Name:\"generic.maxHealth\", Base:" + as.getMaxHealth() + "}]," : "")
                    + (as.isVisible() ? "" : "Invisible:1,")
                    + (as.hasBasePlate() ? "" : "NoBasePlate:1,")
                    + (as.hasGravity() ? "" : "NoGravity:1,")
                    + (as.hasArms() ? "ShowArms:1," : "")
                    + (as.isSmall() ? "Small:1," : "")
                    + (NBT.isInvulnerable(as) ? "Invulnerable:1," : "")
                    + (dSlots == 0 ? "" : ("DisabledSlots:" + dSlots + ","))
                    + (as.isCustomNameVisible() ? "CustomNameVisible:1," : "")
                    + (as.getCustomName() == null ? "" : ("CustomName:\"" + as.getCustomName() + "\","))
                    + (loc.getYaw() == 0F ? "" : ("Rotation:[" + Utils.twoDec(loc.getYaw()) + "f],"))
                    + (as.getBoots() == null && as.getLeggings() == null && as.getChestplate() == null && as.getHelmet() == null ? "" : (
                    "ArmorItems:["
                            + "{id:" + boots + ",Count:" + as.getBoots().getAmount() + ",Damage:" + bootsDmg + NBT.getItemStackTags(as.getBoots()) + "},"
                            + "{id:" + legs + ",Count:" + as.getLeggings().getAmount() + ",Damage:" + legsDmg + NBT.getItemStackTags(as.getLeggings()) + "},"
                            + "{id:" + chest + ",Count:" + as.getChestplate().getAmount() + ",Damage:" + chestDmg + NBT.getItemStackTags(as.getChestplate()) + "},"
                            + "{id:" + helm + ",Count:" + as.getHelmet().getAmount() + ",Damage:" + helmDmg + NBT.getItemStackTags(as.getHelmet()) + NBT.skullOwner(as.getHelmet()) + "}],"))
                    + (as.getEquipment().getItemInMainHand() == null && as.getEquipment().getItemInOffHand() == null ? "" : (
                    "HandItems:["
                            + "{id:" + hand + ",Count:" + as.getEquipment().getItemInMainHand().getAmount() + ",Damage:" + handDmg + NBT.getItemStackTags(as.getEquipment().getItemInMainHand()) + "},"
                            + "{id:" + offHand + ",Count:" + as.getEquipment().getItemInOffHand().getAmount() + ",Damage:" + offHandDmg + NBT.getItemStackTags(as.getEquipment().getItemInOffHand()) + "}],"))
                    + "Pose:{"
                    + (bo.equals(zero) ? "" : ("Body:[" + Utils.angle(bo.getX()) + "f," + Utils.angle(bo.getY()) + "f," + Utils.angle(bo.getZ()) + "f],"))
                    + (he.equals(zero) ? "" : ("Head:[" + Utils.angle(he.getX()) + "f," + Utils.angle(he.getY()) + "f," + Utils.angle(he.getZ()) + "f],"))
                    + (ll.equals(zero) ? "" : ("LeftLeg:[" + Utils.angle(ll.getX()) + "f," + Utils.angle(ll.getY()) + "f," + Utils.angle(ll.getZ()) + "f],"))
                    + (rl.equals(zero) ? "" : ("RightLeg:[" + Utils.angle(rl.getX()) + "f," + Utils.angle(rl.getY()) + "f," + Utils.angle(rl.getZ()) + "f],"))
                    + (la.equals(zero) ? "" : ("LeftArm:[" + Utils.angle(la.getX()) + "f," + Utils.angle(la.getY()) + "f," + Utils.angle(la.getZ()) + "f],"))
                    + "RightArm:[" + Utils.angle(ra.getX()) + "f," + Utils.angle(ra.getY()) + "f," + Utils.angle(ra.getZ()) + "f]}}";
        } else {
            cmd = "summon ArmorStand " + Utils.twoDec(loc.getX()) + " " + Utils.twoDec(loc.getY()) + " " + Utils.twoDec(loc.getZ()) + " {"
                    + (as.getMaxHealth() != 20 ? "Attributes:[{Name:\"generic.maxHealth\", Base:" + as.getMaxHealth() + "}]," : "")
                    + (as.isVisible() ? "" : "Invisible:1,")
                    + (as.hasBasePlate() ? "" : "NoBasePlate:1,")
                    + (as.hasGravity() ? "" : "NoGravity:1,")
                    + (as.hasArms() ? "ShowArms:1," : "")
                    + (as.isSmall() ? "Small:1," : "")
                    + (NBT.isInvulnerable(as) ? "Invulnerable:1," : "")
                    + (dSlots == 0 ? "" : ("DisabledSlots:" + dSlots + ","))
                    + (as.isCustomNameVisible() ? "CustomNameVisible:1," : "")
                    + (as.getCustomName() == null ? "" : ("CustomName:\"" + as.getCustomName() + "\","))
                    + (loc.getYaw() == 0F ? "" : ("Rotation:[" + Utils.twoDec(loc.getYaw()) + "f],"))
                    + (as.getItemInHand() == null && as.getBoots() == null && as.getLeggings() == null && as.getChestplate() == null && as.getHelmet() == null ? "" : (
                    "Equipment:["
                            + "{id:" + hand + ",Count:" + as.getItemInHand().getAmount() + ",Damage:" + handDmg + NBT.getItemStackTags(as.getItemInHand()) + "},"
                            + "{id:" + boots + ",Count:" + as.getBoots().getAmount() + ",Damage:" + bootsDmg + NBT.getItemStackTags(as.getBoots()) + "},"
                            + "{id:" + legs + ",Count:" + as.getLeggings().getAmount() + ",Damage:" + legsDmg + NBT.getItemStackTags(as.getLeggings()) + "},"
                            + "{id:" + chest + ",Count:" + as.getChestplate().getAmount() + ",Damage:" + chestDmg + NBT.getItemStackTags(as.getChestplate()) + "},"
                            + "{id:" + helm + ",Count:" + as.getHelmet().getAmount() + ",Damage:" + helmDmg + NBT.getItemStackTags(as.getHelmet()) + NBT.skullOwner(as.getHelmet()) + "}],"))
                    + "Pose:{"
                    + (bo.equals(zero) ? "" : ("Body:[" + Utils.angle(bo.getX()) + "f," + Utils.angle(bo.getY()) + "f," + Utils.angle(bo.getZ()) + "f],"))
                    + (he.equals(zero) ? "" : ("Head:[" + Utils.angle(he.getX()) + "f," + Utils.angle(he.getY()) + "f," + Utils.angle(he.getZ()) + "f],"))
                    + (ll.equals(zero) ? "" : ("LeftLeg:[" + Utils.angle(ll.getX()) + "f," + Utils.angle(ll.getY()) + "f," + Utils.angle(ll.getZ()) + "f],"))
                    + (rl.equals(zero) ? "" : ("RightLeg:[" + Utils.angle(rl.getX()) + "f," + Utils.angle(rl.getY()) + "f," + Utils.angle(rl.getZ()) + "f],"))
                    + (la.equals(zero) ? "" : ("LeftArm:[" + Utils.angle(la.getX()) + "f," + Utils.angle(la.getY()) + "f," + Utils.angle(la.getZ()) + "f],"))
                    + "RightArm:[" + Utils.angle(ra.getX()) + "f," + Utils.angle(ra.getY()) + "f," + Utils.angle(ra.getZ()) + "f]}}";
        }
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
        if(oneNine) {
            clone.getEquipment().setItemInMainHand(as.getEquipment().getItemInMainHand());
            clone.getEquipment().setItemInOffHand(as.getEquipment().getItemInOffHand());
        } else {
            clone.setItemInHand(as.getItemInHand());
        }
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

    boolean checkBlockPermission(Player p, Block b) {
        if(b == null) return true;
        if (PlotSquaredHook.api != null) {
            Location l = b.getLocation();
            if(PlotSquaredHook.isPlotWorld(l)) {
                return PlotSquaredHook.checkPermission(p, l);
            }
        }
        if(Config.worldGuardPlugin != null) {
            return Config.worldGuardPlugin.canBuild(p, b);
        }
        BlockBreakEvent breakEvent = new BlockBreakEvent(b, p);
        Bukkit.getServer().getPluginManager().callEvent(breakEvent);
        return !breakEvent.isCancelled();
    }

    boolean playerHasPermission(Player p, Block b, ArmorStandTool tool) {
        return (tool == null || tool.isEnabled() && Utils.hasPermissionNode(p, tool.getPermission())) && checkBlockPermission(p, b);
    }
}
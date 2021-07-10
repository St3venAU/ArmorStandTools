package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;

class Utils {

    private static DecimalFormat twoDec;

    static void openSign(final Player p, final Block b) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Object world = b.getWorld().getClass().getMethod("getHandle").invoke(b.getWorld());
                    Object position = Class.forName("net.minecraft.core.BlockPosition").getConstructor(int.class, int.class, int.class).newInstance(b.getX(), b.getY(), b.getZ());
                    Object sign = world.getClass().getMethod("getTileEntity", Class.forName("net.minecraft.core.BlockPosition")).invoke(world, position);
                    Object player = p.getClass().getMethod("getHandle").invoke(p);
                    player.getClass().getMethod("openSign", Class.forName("net.minecraft.world.level.block.entity.TileEntitySign")).invoke(player, sign);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(AST.plugin, 2L);
    }

    static final EquipmentSlot[] equipmentSlots = {
            EquipmentSlot.HAND,
            EquipmentSlot.OFF_HAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    static boolean hasDisabledSlots(ArmorStand as) {
        for(EquipmentSlot slot : equipmentSlots) {
            if(as.hasEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING)) {
                return true;
            }
        }
        return false;
    }

    static void setSlotsDisabled(ArmorStand as, boolean slotsDisabled) {
        if(slotsDisabled) {
            for(EquipmentSlot slot : equipmentSlots) {
                as.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
            }
        } else {
            for(EquipmentSlot slot : equipmentSlots) {
                as.removeEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
            }
        }
    }

    static boolean toggleSlotsDisabled(ArmorStand as) {
        boolean slotsDisabled = !hasDisabledSlots(as);
        setSlotsDisabled(as, slotsDisabled);
        return slotsDisabled;
    }

    static ArmorStand cloneArmorStand(ArmorStand as) {
        ArmorStand clone = (ArmorStand) as.getWorld().spawnEntity(as.getLocation().add(1, 0, 0), EntityType.ARMOR_STAND);
        EntityEquipment asEquipment = as.getEquipment();
        EntityEquipment cloneEquipment = clone.getEquipment();
        if(asEquipment != null && cloneEquipment != null) {
            cloneEquipment.setHelmet(asEquipment.getHelmet());
            cloneEquipment.setChestplate(asEquipment.getChestplate());
            cloneEquipment.setLeggings(asEquipment.getLeggings());
            cloneEquipment.setBoots(asEquipment.getBoots());
            cloneEquipment.setItemInMainHand(asEquipment.getItemInMainHand());
            cloneEquipment.setItemInOffHand(asEquipment.getItemInOffHand());
        }
        clone.setHeadPose(as.getHeadPose());
        clone.setBodyPose(as.getBodyPose());
        clone.setLeftArmPose(as.getLeftArmPose());
        clone.setRightArmPose(as.getRightArmPose());
        clone.setLeftLegPose(as.getLeftLegPose());
        clone.setRightLegPose(as.getRightLegPose());
        clone.setGravity(as.hasGravity());
        clone.setVisible(as.isVisible());
        clone.setBasePlate(as.hasBasePlate());
        clone.setArms(as.hasArms());
        clone.setCustomName(as.getCustomName());
        clone.setCustomNameVisible(as.isCustomNameVisible());
        clone.setSmall(as.isSmall());
        clone.setInvulnerable(as.isInvulnerable());
        clone.setGlowing(as.isGlowing());
        Utils.setSlotsDisabled(clone, Utils.hasDisabledSlots(as));
        ArmorStandCmd asCmd = new ArmorStandCmd(as);
        if(asCmd.getCommand() != null) {
            asCmd.cloneTo(clone);
        }
        clone.setMetadata("clone", new FixedMetadataValue(AST.plugin, true));
        return clone;
    }

    static boolean hasPermissionNode(Player p, String perm) {
        if ((p == null) || p.isOp()) {
            return true;
        }
        if (p.hasPermission(perm)) {
            return true;
        }
        final String[] nodes = perm.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i]).append(".");
            if (p.hasPermission(n + "*")) {
                return true;
            }
        }
        return false;
    }

    static void title(Player p, String msg) {
        p.sendTitle(" ", msg, 0, 70, 0);
    }

    static Location getLocationFacing(Location loc) {
        Location l = loc.clone();
        Vector v = l.getDirection();
        v.setY(0);
        v.multiply(3);
        l.add(v);
        l.setYaw(l.getYaw() + 180);
        int n;
        boolean ok = false;
        for (n = 0; n < 5; n++) {
            if (l.getBlock().getType().isSolid()) {
                l.add(0, 1, 0);
            } else {
                ok = true;
                break;
            }
        }
        if (!ok) {
            l.subtract(0, 5, 0);
        }
        return l;
    }


    static private String getItemStackTags(ItemStack is) {
        if(is == null) {
            return "";
        }
        StringBuilder tags = new StringBuilder();
        if(is.getItemMeta() != null && is.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta armorMeta = (LeatherArmorMeta) is.getItemMeta();
            tags.append("display:{color:");
            tags.append(armorMeta.getColor().asRGB());
            tags.append("}");
        }
        Map<Enchantment, Integer> enchants = is.getEnchantments();
        if(enchants.size() > 0) {
            if(tags.length() > 0) {
                tags.append(",");
            }
            tags.append("Enchantments:[");

            for(Enchantment e : enchants.keySet()) {
                tags.append("{id:");
                tags.append(e.getKey().getKey());
                tags.append(",lvl:");
                tags.append(enchants.get(e));
                tags.append("},");
            }

            tags.setCharAt(tags.length() - 1, ']');
        }
        return tags.length() == 0 ? "" : ("," + tags.toString());
    }

    static private String skullOwner(ItemStack is) {
        if(is == null || is.getItemMeta() == null || !(is.getItemMeta() instanceof SkullMeta)) return "";
        SkullMeta skull = (SkullMeta) is.getItemMeta();
        return  skull.getOwningPlayer() == null ? "" : ",SkullOwner:\"" + skull.getOwningPlayer().getName() + "\"";
    }

    static void generateCmdBlock(Location l, ArmorStand as) {
        Block b = l.getBlock();
        b.setType(Material.COMMAND_BLOCK);
        CommandBlock cb = (CommandBlock) b.getState();
        EntityEquipment e = as.getEquipment();
        cb.setCommand("summon minecraft:armor_stand " + twoDec(as.getLocation().getX()) + " " + twoDec(as.getLocation().getY()) + " " + twoDec(as.getLocation().getZ()) + " "
                + "{"
                + (as.isVisible()                  ? ""                     : "Invisible:1,"                                                  )
                + (as.hasBasePlate()               ? ""                     : "NoBasePlate:1,"                                                )
                + (as.hasGravity()                 ? ""                     : "NoGravity:1,"                                                  )
                + (as.hasArms()                    ? "ShowArms:1,"          : ""                                                              )
                + (as.isSmall()                    ? "Small:1,"             : ""                                                              )
                + (as.isInvulnerable()             ? "Invulnerable:1,"      : ""                                                              )
                + (as.isGlowing()                  ? "Glowing:1,"           : ""                                                              )
                + (as.isCustomNameVisible()        ? "CustomNameVisible:1," : ""                                                              )
                + (as.getCustomName() == null      ? ""                     : ("CustomName:\"\\\"" + as.getCustomName() + "\\\"\",")          )
                + (as.getLocation().getYaw() == 0F ? ""                     : ("Rotation:[" + twoDec(as.getLocation().getYaw()) + "f],"))
                + "ArmorItems:["
                + (e == null || e.getBoots()      == null ? "{}," : ("{id:" + e.getBoots().getType().getKey().getKey()      + ",Count:" + e.getBoots().getAmount()      + ",tag:{Damage:" + e.getBoots().getDurability()      + getItemStackTags(e.getBoots())                              + "}},"))
                + (e == null || e.getLeggings()   == null ? "{}," : ("{id:" + e.getLeggings().getType().getKey().getKey()   + ",Count:" + e.getLeggings().getAmount()   + ",tag:{Damage:" + e.getLeggings().getDurability()   + getItemStackTags(e.getLeggings())                           + "}},"))
                + (e == null || e.getChestplate() == null ? "{}," : ("{id:" + e.getChestplate().getType().getKey().getKey() + ",Count:" + e.getChestplate().getAmount() + ",tag:{Damage:" + e.getChestplate().getDurability() + getItemStackTags(e.getChestplate())                         + "}},"))
                + (e == null || e.getHelmet()     == null ? "{}"  : ("{id:" + e.getHelmet().getType().getKey().getKey()     + ",Count:" + e.getHelmet().getAmount()     + ",tag:{Damage:" + e.getHelmet().getDurability()     + getItemStackTags(e.getHelmet()) + skullOwner(e.getHelmet()) + "}}" ))
                + "],"
                + "HandItems:["
                + (e == null ? "{}," : ("{id:" + e.getItemInMainHand().getType().getKey().getKey() + ",Count:" + e.getItemInMainHand().getAmount() + ",tag:{Damage:" + e.getItemInMainHand().getDurability() + getItemStackTags(e.getItemInMainHand()) + "}},"))
                + (e == null ? "{}"  : ("{id:" + e.getItemInOffHand().getType().getKey().getKey()  + ",Count:" + e.getItemInOffHand().getAmount()  + ",tag:{Damage:" + e.getItemInOffHand().getDurability()  + getItemStackTags(e.getItemInOffHand())  + "}}" ))
                + "],"
                + "Pose:{"
                + "Body:["     + degrees(as.getBodyPose().getX())     + "f," + degrees(as.getBodyPose().getY())     + "f," + degrees(as.getBodyPose().getZ())     + "f],"
                + "Head:["     + degrees(as.getHeadPose().getX())     + "f," + degrees(as.getHeadPose().getY())     + "f," + degrees(as.getHeadPose().getZ())     + "f],"
                + "LeftLeg:["  + degrees(as.getLeftLegPose().getX())  + "f," + degrees(as.getLeftLegPose().getY())  + "f," + degrees(as.getLeftLegPose().getZ())  + "f],"
                + "RightLeg:[" + degrees(as.getRightLegPose().getX()) + "f," + degrees(as.getRightLegPose().getY()) + "f," + degrees(as.getRightLegPose().getZ()) + "f],"
                + "LeftArm:["  + degrees(as.getLeftArmPose().getX())  + "f," + degrees(as.getLeftArmPose().getY())  + "f," + degrees(as.getLeftArmPose().getZ())  + "f],"
                + "RightArm:[" + degrees(as.getRightArmPose().getX()) + "f," + degrees(as.getRightArmPose().getY()) + "f," + degrees(as.getRightArmPose().getZ()) + "f]"
                + "}"
                + "}"
        );
        cb.update();
    }

    private static String twoDec(double d) {
        if(twoDec == null) {
            twoDec = new DecimalFormat("0.0#");
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            twoDec.setDecimalFormatSymbols(symbols);
        }
        return twoDec.format(d);
    }

    private static String degrees(double d) {
        return twoDec(d * 180.0 / Math.PI);
    }

}

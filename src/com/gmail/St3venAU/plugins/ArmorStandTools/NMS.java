package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.Map;

abstract class NMS {

    private final String
            nmsVersion,
            disabledSlotsFieldName;

    NMS(String nmsVersion, String disabledSlotsFieldName) {
        this.nmsVersion = nmsVersion;
        this.disabledSlotsFieldName = disabledSlotsFieldName;
    }

    private Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + nmsVersion + "." + nmsClassString);
    }

    private Object getNmsEntity(org.bukkit.entity.Entity entity) {
        try {
            return entity.getClass().getMethod("getHandle").invoke(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void openSign(final Player p, final Block b) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Object world = b.getWorld().getClass().getMethod("getHandle").invoke(b.getWorld());
                    Object blockPos = getNMSClass("BlockPosition").getConstructor(int.class, int.class, int.class).newInstance(b.getX(), b.getY(), b.getZ());
                    Object sign = world.getClass().getMethod("getTileEntity", getNMSClass("BlockPosition")).invoke(world, blockPos);
                    Object player = p.getClass().getMethod("getHandle").invoke(p);
                    player.getClass().getMethod("openSign", getNMSClass("TileEntitySign")).invoke(player, sign);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(Main.plugin, 2L);
    }

    boolean toggleSlotsDisabled(ArmorStand as) {
        boolean slotsDisabled = getDisabledSlots(as) == 0;
        setSlotsDisabled(as, slotsDisabled);
        return slotsDisabled;
    }

    private int getDisabledSlots(ArmorStand as) {
        Object nmsEntity = getNmsEntity(as);
        if(nmsEntity == null) return 0;
        Field f;
        try {
            f = nmsEntity.getClass().getDeclaredField(disabledSlotsFieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return 0;
        }
        f.setAccessible(true);
        try {
            return (Integer) f.get(nmsEntity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    void setSlotsDisabled(ArmorStand as, boolean slotsDisabled) {
        Object nmsEntity = getNmsEntity(as);
        if (nmsEntity == null) return;
        Field f;
        try {
            f = nmsEntity.getClass().getDeclaredField(disabledSlotsFieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }
        f.setAccessible(true);
        try {
            f.set(nmsEntity, slotsDisabled ? 0xFFFFFF : 0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    boolean equipmentLocked(ArmorStand as) {
        return getDisabledSlots(as) == 0xFFFFFF;
    }

    private String getItemStackTags(ItemStack is) {
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

    private String skullOwner(ItemStack is) {
        if(is == null || is.getItemMeta() == null || !(is.getItemMeta() instanceof SkullMeta)) {
            return "";
        }
        SkullMeta skull = (SkullMeta) is.getItemMeta();
        if(skull.hasOwner()) {
            return ",SkullOwner:\"" + skull.getOwningPlayer().getName() + "\"";
        } else {
            return "";
        }
    }

    void generateCmdBlock(Location l, ArmorStand as) {
        Block b = l.getBlock();
        b.setType(Material.COMMAND_BLOCK);
        CommandBlock cb = (CommandBlock) b.getState();
        cb.setCommand("summon minecraft:armor_stand " + Utils.twoDec(as.getLocation().getX()) + " " + Utils.twoDec(as.getLocation().getY()) + " " + Utils.twoDec(as.getLocation().getZ()) + " "
            + "{"
                + (as.isVisible()                  ? ""                     : "Invisible:1,"                                                  )
                + (as.hasBasePlate()               ? ""                     : "NoBasePlate:1,"                                                )
                + (as.hasGravity()                 ? ""                     : "NoGravity:1,"                                                  )
                + (as.hasArms()                    ? "ShowArms:1,"          : ""                                                              )
                + (as.isSmall()                    ? "Small:1,"             : ""                                                              )
                + (as.isInvulnerable()             ? "Invulnerable:1,"      : ""                                                              )
                + (getDisabledSlots(as) == 0       ? ""                     : ("DisabledSlots:" + getDisabledSlots(as) + ",")                 )
                + (as.isCustomNameVisible()        ? "CustomNameVisible:1," : ""                                                              )
                + (as.getCustomName() == null      ? ""                     : ("CustomName:\"\\\"" + as.getCustomName() + "\\\"\",")          )
                + (as.getLocation().getYaw() == 0F ? ""                     : ("Rotation:[" + Utils.twoDec(as.getLocation().getYaw()) + "f],"))
                + "ArmorItems:["
                    + (as.getBoots()      == null ? "{}," : ("{id:" + as.getBoots().getType().getKey().getKey()      + ",Count:" + as.getBoots().getAmount()      + ",tag:{Damage:" + as.getBoots().getDurability()      + getItemStackTags(as.getBoots())                               + "}},"))
                    + (as.getLeggings()   == null ? "{}," : ("{id:" + as.getLeggings().getType().getKey().getKey()   + ",Count:" + as.getLeggings().getAmount()   + ",tag:{Damage:" + as.getLeggings().getDurability()   + getItemStackTags(as.getLeggings())                            + "}},"))
                    + (as.getChestplate() == null ? "{}," : ("{id:" + as.getChestplate().getType().getKey().getKey() + ",Count:" + as.getChestplate().getAmount() + ",tag:{Damage:" + as.getChestplate().getDurability() + getItemStackTags(as.getChestplate())                          + "}},"))
                    + (as.getHelmet()     == null ? "{}"  : ("{id:" + as.getHelmet().getType().getKey().getKey()     + ",Count:" + as.getHelmet().getAmount()     + ",tag:{Damage:" + as.getHelmet().getDurability()     + getItemStackTags(as.getHelmet()) + skullOwner(as.getHelmet()) + "}}" ))
                + "],"
                + "HandItems:["
                    + (as.getEquipment().getItemInMainHand() == null ? "{}," : ("{id:" + as.getEquipment().getItemInMainHand().getType().getKey().getKey() + ",Count:" + as.getEquipment().getItemInMainHand().getAmount() + ",tag:{Damage:" + as.getEquipment().getItemInMainHand().getDurability() + getItemStackTags(as.getEquipment().getItemInMainHand()) + "}},"))
                    + (as.getEquipment().getItemInOffHand()  == null ? "{}"  : ("{id:" + as.getEquipment().getItemInOffHand().getType().getKey().getKey()  + ",Count:" + as.getEquipment().getItemInOffHand().getAmount()  + ",tag:{Damage:" + as.getEquipment().getItemInOffHand().getDurability()  + getItemStackTags(as.getEquipment().getItemInOffHand())  + "}}" ))
                + "],"
                + "Pose:{"
                    + "Body:["     + Utils.angle(as.getBodyPose().getX())     + "f," + Utils.angle(as.getBodyPose().getY())     + "f," + Utils.angle(as.getBodyPose().getZ())     + "f],"
                    + "Head:["     + Utils.angle(as.getHeadPose().getX())     + "f," + Utils.angle(as.getHeadPose().getY())     + "f," + Utils.angle(as.getHeadPose().getZ())     + "f],"
                    + "LeftLeg:["  + Utils.angle(as.getLeftLegPose().getX())  + "f," + Utils.angle(as.getLeftLegPose().getY())  + "f," + Utils.angle(as.getLeftLegPose().getZ())  + "f],"
                    + "RightLeg:[" + Utils.angle(as.getRightLegPose().getX()) + "f," + Utils.angle(as.getRightLegPose().getY()) + "f," + Utils.angle(as.getRightLegPose().getZ()) + "f],"
                    + "LeftArm:["  + Utils.angle(as.getLeftArmPose().getX())  + "f," + Utils.angle(as.getLeftArmPose().getY())  + "f," + Utils.angle(as.getLeftArmPose().getZ())  + "f],"
                    + "RightArm:[" + Utils.angle(as.getRightArmPose().getX()) + "f," + Utils.angle(as.getRightArmPose().getY()) + "f," + Utils.angle(as.getRightArmPose().getZ()) + "f]"
                + "}"
            + "}"
        );
        cb.update();
    }

    ArmorStand clone(ArmorStand as) {
        ArmorStand clone = (ArmorStand) as.getWorld().spawnEntity(as.getLocation().add(1, 0, 0), EntityType.ARMOR_STAND);
        clone.setGravity(as.hasGravity());
        clone.setHelmet(as.getHelmet());
        clone.setChestplate(as.getChestplate());
        clone.setLeggings(as.getLeggings());
        clone.setBoots(as.getBoots());
        clone.getEquipment().setItemInMainHand(as.getEquipment().getItemInMainHand());
        clone.getEquipment().setItemInOffHand(as.getEquipment().getItemInOffHand());
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
        clone.setInvulnerable(as.isInvulnerable());
        setSlotsDisabled(clone, getDisabledSlots(as) == 0xFFFFFF);
        ArmorStandCmd asCmd = new ArmorStandCmd(as);
        if(asCmd.getCommand() != null) {
            asCmd.cloneTo(clone);
        }
        return clone;
    }

}

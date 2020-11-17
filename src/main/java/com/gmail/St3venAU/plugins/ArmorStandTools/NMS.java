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

    private final String nmsVersion;
    private final String[] disabledSlotsFieldNames;

    NMS(String nmsVersion, String... disabledSlotsFieldNames) {
        this.nmsVersion = nmsVersion;
        this.disabledSlotsFieldNames = disabledSlotsFieldNames;
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

    private Field getDisabledSlotsField(Object nmsEntity) {
        if(nmsEntity == null) return null;
        for(String field : disabledSlotsFieldNames) {
            try {
                Field f = nmsEntity.getClass().getDeclaredField(field);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
            }
        }
        return null;
    }

    private int getDisabledSlots(ArmorStand as) {
        Object nmsEntity = getNmsEntity(as);
        if(nmsEntity == null) return 0;
        Field f = getDisabledSlotsField(nmsEntity);
        if(f == null) return 0;
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
        Field f = getDisabledSlotsField(nmsEntity);
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
                + (as.isGlowing()                  ? "Glowing:1,"           : ""                                                              )
                + (getDisabledSlots(as) == 0       ? ""                     : ("DisabledSlots:" + getDisabledSlots(as) + ",")                 )
                + (as.isCustomNameVisible()        ? "CustomNameVisible:1," : ""                                                              )
                + (as.getCustomName() == null      ? ""                     : ("CustomName:\"\\\"" + as.getCustomName() + "\\\"\",")          )
                + (as.getLocation().getYaw() == 0F ? ""                     : ("Rotation:[" + Utils.twoDec(as.getLocation().getYaw()) + "f],"))
                + "ArmorItems:["
                    + (as.getEquipment() != null && as.getEquipment().getBoots()      == null ? "{}," : ("{id:" + as.getEquipment().getBoots().getType().getKey().getKey()      + ",Count:" + as.getEquipment().getBoots().getAmount()      + ",tag:{Damage:" + as.getEquipment().getBoots().getDurability()      + getItemStackTags(as.getEquipment().getBoots())                                              + "}},"))
                    + (as.getEquipment() != null && as.getEquipment().getLeggings()   == null ? "{}," : ("{id:" + as.getEquipment().getLeggings().getType().getKey().getKey()   + ",Count:" + as.getEquipment().getLeggings().getAmount()   + ",tag:{Damage:" + as.getEquipment().getLeggings().getDurability()   + getItemStackTags(as.getEquipment().getLeggings())                                           + "}},"))
                    + (as.getEquipment() != null && as.getEquipment().getChestplate() == null ? "{}," : ("{id:" + as.getEquipment().getChestplate().getType().getKey().getKey() + ",Count:" + as.getEquipment().getChestplate().getAmount() + ",tag:{Damage:" + as.getEquipment().getChestplate().getDurability() + getItemStackTags(as.getEquipment().getChestplate())                                         + "}},"))
                    + (as.getEquipment() != null && as.getEquipment().getHelmet()     == null ? "{}"  : ("{id:" + as.getEquipment().getHelmet().getType().getKey().getKey()     + ",Count:" + as.getEquipment().getHelmet().getAmount()     + ",tag:{Damage:" + as.getEquipment().getHelmet().getDurability()     + getItemStackTags(as.getEquipment().getHelmet()) + skullOwner(as.getEquipment().getHelmet()) + "}}" ))
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
        clone.getEquipment().setHelmet(as.getEquipment().getHelmet());
        clone.getEquipment().setChestplate(as.getEquipment().getChestplate());
        clone.getEquipment().setLeggings(as.getEquipment().getLeggings());
        clone.getEquipment().setBoots(as.getEquipment().getBoots());
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
        clone.setGlowing(as.isGlowing());
        setSlotsDisabled(clone, getDisabledSlots(as) == 0xFFFFFF);
        ArmorStandCmd asCmd = new ArmorStandCmd(as);
        if(asCmd.getCommand() != null) {
            asCmd.cloneTo(clone);
        }
        return clone;
    }

}

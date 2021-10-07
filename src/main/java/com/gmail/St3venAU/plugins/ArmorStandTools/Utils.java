package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
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

    static boolean hasDisabledSlots(ArmorStand as) {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            for(ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                if(!as.hasEquipmentLock(slot, lockType)) {
                    return false;
                }
            }
        }
        return true;
    }

    static void setSlotsDisabled(ArmorStand as, boolean slotsDisabled) {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            for(ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                if(slotsDisabled) {
                    as.addEquipmentLock(slot, lockType);
                } else {
                    as.removeEquipmentLock(slot, lockType);
                }
            }
        }
    }

    static int disabledSlotsAsInteger(ArmorStand as) {
        return    (as.hasEquipmentLock(EquipmentSlot.HAND,     ArmorStand.LockType.ADDING_OR_CHANGING) ? 1 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.FEET,     ArmorStand.LockType.ADDING_OR_CHANGING) ? 2 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.LEGS,     ArmorStand.LockType.ADDING_OR_CHANGING) ? 4 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.CHEST,    ArmorStand.LockType.ADDING_OR_CHANGING) ? 8 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.HEAD,     ArmorStand.LockType.ADDING_OR_CHANGING) ? 16 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING) ? 32 : 0)

                + (as.hasEquipmentLock(EquipmentSlot.HAND,     ArmorStand.LockType.REMOVING_OR_CHANGING) ? 256 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.FEET,     ArmorStand.LockType.REMOVING_OR_CHANGING) ? 512 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.LEGS,     ArmorStand.LockType.REMOVING_OR_CHANGING) ? 1024 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.CHEST,    ArmorStand.LockType.REMOVING_OR_CHANGING) ? 2048 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.HEAD,     ArmorStand.LockType.REMOVING_OR_CHANGING) ? 4096 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING) ? 8192 : 0)

                + (as.hasEquipmentLock(EquipmentSlot.HAND,     ArmorStand.LockType.ADDING) ? 65536 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.FEET,     ArmorStand.LockType.ADDING) ? 131072 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.LEGS,     ArmorStand.LockType.ADDING) ? 262144 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.CHEST,    ArmorStand.LockType.ADDING) ? 524288 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.HEAD,     ArmorStand.LockType.ADDING) ? 1048576 : 0)
                + (as.hasEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING) ? 2097152 : 0);
    }

    static boolean toggleSlotsDisabled(ArmorStand as) {
        boolean slotsDisabled = !hasDisabledSlots(as);
        setSlotsDisabled(as, slotsDisabled);
        return slotsDisabled;
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

    static boolean containsItems(Collection<ItemStack> items) {
        for(ItemStack i : items) {
            if(ArmorStandTool.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    static private String getItemStackTags(ItemStack is) {
        if(is == null) {
            return "";
        }
        StringBuilder tags = new StringBuilder();
        if(is.getItemMeta() != null && is.getItemMeta() instanceof LeatherArmorMeta armorMeta) {
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
        return tags.length() == 0 ? "" : tags.toString();
    }

    static private String skullOwner(ItemStack is) {
        if(is == null || is.getItemMeta() == null || !(is.getItemMeta() instanceof SkullMeta skull)) return "";
        return skull.getOwningPlayer() == null ? "" : "SkullOwner:\"" + skull.getOwningPlayer().getName() + "\"";
    }

    static private boolean isEmpty(ItemStack is) {
        return is == null || is.getType() == Material.AIR;
    }

    static private String itemInfo(ItemStack is) {
        if(isEmpty(is)) return "{}";
        StringBuilder sb = new StringBuilder("{id:");
        sb.append(is.getType().getKey().getKey());
        if(is.getAmount() > 0) {
            sb.append(",Count:").append(is.getAmount());
        }
        String itemStackTags = getItemStackTags(is);
        @SuppressWarnings("deprecation")
        short durability = is.getDurability();
        String skullOwner = skullOwner(is);
        int n = 0;
        if(itemStackTags.length() > 0 || durability > 0 || skullOwner.length() > 0) {
            sb.append(",tag:{");
            if(durability > 0) {
                sb.append("Damage:").append(durability);
                n++;
            }
            if(itemStackTags.length() > 0) {
                if(n > 0) sb.append(",");
                sb.append(itemStackTags);
                n++;
            }
            if(skullOwner.length() > 0) {
                if(n > 0) sb.append(",");
                sb.append(skullOwner);
            }
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    static private String armorItems(EntityEquipment e) {
        if(e == null || (isEmpty(e.getBoots()) && isEmpty(e.getLeggings()) && isEmpty(e.getChestplate()) && isEmpty(e.getHelmet()))) {
            return "";
        }
        return "ArmorItems:["
                + itemInfo(e.getBoots()) + ","
                + itemInfo(e.getLeggings()) + ","
                + itemInfo(e.getChestplate()) + ","
                + itemInfo(e.getHelmet())
                + "],";
    }

    static private String handItems(EntityEquipment e) {
        if(e == null || (isEmpty(e.getItemInMainHand()) && isEmpty(e.getItemInOffHand()))) {
            return "";
        }
        return "HandItems:["
                + itemInfo(e.getItemInMainHand()) + ","
                + itemInfo(e.getItemInOffHand())
                + "],";
    }

    static private String angleInfo(EulerAngle ea) {
        return "[" + degrees(ea.getX()) + "f," + degrees(ea.getY()) + "f," + degrees(ea.getZ()) + "f]";
    }

    static private String pose(ArmorStand as) {
        return "Pose:{"
                + "Body:"     + angleInfo(as.getBodyPose())     + ","
                + "Head:"     + angleInfo(as.getHeadPose())     + ","
                + "LeftLeg:"  + angleInfo(as.getLeftLegPose())  + ","
                + "RightLeg:" + angleInfo(as.getRightLegPose()) + ","
                + "LeftArm:"  + angleInfo(as.getLeftArmPose())  + ","
                + "RightArm:" + angleInfo(as.getRightArmPose())
                + "}";
    }

    static String createSummonCommand(ArmorStand as) {
        Location asLocation = as.getLocation();
        EntityEquipment e = as.getEquipment();
        StringBuilder sb = new StringBuilder("summon minecraft:armor_stand ");
        sb.append(twoDec(asLocation.getX())).append(" ");
        sb.append(twoDec(asLocation.getY())).append(" ");
        sb.append(twoDec(asLocation.getZ())).append(" ");
        sb.append("{");
        if(!as.isVisible())                 sb.append("Invisible:1,");
        if(!as.hasBasePlate())              sb.append("NoBasePlate:1,");
        if(!as.hasGravity())                sb.append("NoGravity:1,");
        if(as.hasArms())                    sb.append("ShowArms:1,");
        if(as.isSmall())                    sb.append("Small:1,");
        if(as.isInvulnerable())             sb.append("Invulnerable:1,");
        if(as.isGlowing())                  sb.append("Glowing:1,");
        if(hasDisabledSlots(as))            sb.append("DisabledSlots:").append(disabledSlotsAsInteger(as)).append(",");
        if(as.isCustomNameVisible())        sb.append("CustomNameVisible:1,");
        if(as.getCustomName() != null)      sb.append("CustomName:\"\\\"").append(as.getCustomName()).append("\\\"\",");
        if(as.getLocation().getYaw() != 0F) sb.append("Rotation:[").append(twoDec(as.getLocation().getYaw())).append("f],");
        sb.append(armorItems(e));
        sb.append(handItems(e));
        sb.append(pose(as));
        sb.append("}");
        return sb.toString();
    }

    static void generateCmdBlock(Location l, String command) {
        Block b = l.getBlock();
        b.setType(Material.COMMAND_BLOCK);
        CommandBlock cb = (CommandBlock) b.getState();
        cb.setCommand(command);
        cb.update();
    }

    static String twoDec(double d) {
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

    static boolean hasAnyTools(Player p) {
        for(ItemStack i : p.getInventory()) {
            if(ArmorStandTool.isTool(i)) {
                return true;
            }
        }
        return false;
    }

    private static boolean onCooldown(Entity e) {
        for(MetadataValue meta : e.getMetadata("lastEvent")) {
            if(AST.plugin.equals(meta.getOwningPlugin())) {
                return System.currentTimeMillis() - meta.asLong() < 100;
            }
        }
        return false;
    }

    static void cycleInventory(Player p) {
        if(onCooldown(p)) return;
        Inventory i = p.getInventory();
        ItemStack temp;
        for (int n = 0; n < 9; n++) {
            temp = i.getItem(n);
            i.setItem(n, i.getItem(27 + n));
            i.setItem(27 + n, i.getItem(18 + n));
            i.setItem(18 + n, i.getItem(9 + n));
            i.setItem(9 + n, temp);
        }
        p.updateInventory();
        p.setMetadata("lastEvent", new FixedMetadataValue(AST.plugin, System.currentTimeMillis()));
    }



}

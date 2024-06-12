package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

class Utils {

    private static DecimalFormat twoDec;

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
        String itemStackTags = ItemStackReflections.itemNBTToString(is);
        if(itemStackTags != null && !itemStackTags.isEmpty()) {
            sb.append(",tag:");
            sb.append(itemStackTags);
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
        return  "summon minecraft:armor_stand " +
                twoDec(asLocation.getX()) + " " +
                twoDec(asLocation.getY()) + " " +
                twoDec(asLocation.getZ()) + " " +
                createEntityTag(as);
    }

    static String quote(String s) {
        return "\"\\\"" +
                s.replace("\\", "\\\\\\\\").replace("\"", "\\\\\\\"") // escape " and \
                + "\\\"\"";
    }

    static ItemStack createArmorStandItem(ArmorStand as) {
        EntityEquipment equipment = as.getEquipment();
        if(equipment != null){
            for(EquipmentSlot slot : EquipmentSlot.values()){
                if(!canArmorStandItemContain(equipment.getItem(slot))) return null;
            }
        }
        ItemStack armorStand = new ItemStack(Material.ARMOR_STAND);
        ItemStackReflections.setItemNBTFromString(armorStand, "{EntityTag:" + createEntityTag(as) + "}");
        ItemMeta meta = armorStand.getItemMeta();
        if(meta != null){
            meta.setLore(createItemLore(as));
            meta.setDisplayName(Config.configuredArmorStand);
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        armorStand.setItemMeta(meta);
        return armorStand;
    }

    static String createEntityTag(ArmorStand as) {
        EntityEquipment e = as.getEquipment();
        StringBuilder sb = new StringBuilder("{");
        if(!as.isVisible())                 sb.append("Invisible:1,");
        if(!as.hasBasePlate())              sb.append("NoBasePlate:1,");
        if(!as.hasGravity())                sb.append("NoGravity:1,");
        if(as.hasArms())                    sb.append("ShowArms:1,");
        if(as.isSmall())                    sb.append("Small:1,");
        if(as.isInvulnerable())             sb.append("Invulnerable:1,");
        if(as.isGlowing())                  sb.append("Glowing:1,");
        if(hasDisabledSlots(as))            sb.append("DisabledSlots:").append(disabledSlotsAsInteger(as)).append(",");
        if(as.isCustomNameVisible())        sb.append("CustomNameVisible:1,");
        if(as.getCustomName() != null)      sb.append("CustomName:").append(quote(as.getCustomName())).append(",");
        if(as.getLocation().getYaw() != 0F) sb.append("Rotation:[").append(twoDec(as.getLocation().getYaw())).append("f],");
        sb.append(armorItems(e));
        sb.append(handItems(e));
        sb.append(pose(as));
        sb.append("}");
        return sb.toString();
    }

    static List<String> createItemLore(ArmorStand as) {
        EntityEquipment e = as.getEquipment();
        List<String> lore = new ArrayList<>();
        String name = as.getCustomName();
        if(name != null && name.length() > 0) {
            lore.add(Config.name + ": " + name);
        }
        if (e != null) {
            int stacks = 0;
            int items = 0;
            for(EquipmentSlot slot : EquipmentSlot.values()){
                ItemStack item = e.getItem(slot);
                if(item.getType() == Material.AIR) continue;
                stacks++;
                items += item.getAmount();
            }
            if(stacks > 0) {
                lore.add(Config.inventory + ": " + items + " " + Config.items + " (" + stacks + " " + Config.stacks + ")");
            }
        }
        List<String> attribs = new ArrayList<>();
        if(hasDisabledSlots(as))    attribs.add(Config.equip + " " + Config.locked);
        if(!as.hasGravity())        attribs.add(Config.gravity + " " + Config.isOff);
        if(!as.isVisible())         attribs.add(Config.invisible);
        if(as.hasArms())            attribs.add(Config.arms);
        if(as.isSmall())            attribs.add(Config.small);
        if(as.isInvulnerable())     attribs.add(Config.invuln);
        if(as.isGlowing())          attribs.add(Config.glowing);
        if(attribs.size() > 0) {
            StringBuilder sb = new StringBuilder(Config.attributes + ": ");
            for (String attrib : attribs) {
                sb.append(attrib).append(", ");
                if (sb.length() >= 40) {
                    lore.add(sb.toString());
                    sb = new StringBuilder();
                }
            }
            if (sb.length() > 1) {
                lore.add(sb.substring(0, sb.length() - 2));
            } else {
                String last = lore.get(lore.size() - 1);
                lore.remove(lore.size() - 1);
                lore.add(last.substring(0, last.length() - 2));
            }
        }
        return lore;
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
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            for(ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                if(as.hasEquipmentLock(slot, lockType)) {
                    clone.addEquipmentLock(slot, lockType);
                }
            }
        }
        ArmorStandCmdManager cmdMgrAs = new ArmorStandCmdManager(as);
        ArmorStandCmdManager cmdMgrClone = new ArmorStandCmdManager(clone);
        for(ArmorStandCmd asCmd : cmdMgrAs.getCommands()) {
            cmdMgrClone.addCommand(asCmd, true);
        }
        int cooldown = cmdMgrAs.getCooldownTime();
        if(cooldown > 0) {
            cmdMgrClone.setCooldownTime(cooldown);
        }
        clone.setMetadata("clone", new FixedMetadataValue(AST.plugin, true));
        return clone;
    }

    static boolean isConfiguredArmorStandItem(ItemStack item){
        return item.getType() == Material.ARMOR_STAND && ItemStackReflections.containsEntityTag(item);
    }

    static boolean canArmorStandItemContain(ItemStack item){
        return !isConfiguredArmorStandItem (item) &&
                !(item.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof ShulkerBox);
    }
}

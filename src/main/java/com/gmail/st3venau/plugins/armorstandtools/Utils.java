package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class Utils {

    private static DecimalFormat twoDec;

    static boolean hasDisabledSlots(ArmorStand as) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                if (!as.hasEquipmentLock(slot, lockType)) {
                    return false;
                }
            }
        }

        return true;
    }

    static void setSlotsDisabled(ArmorStand as, boolean slotsDisabled) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                if (slotsDisabled) {
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
        for (ItemStack i : items) {
            if (ArmorStandTool.get(i) != null) {
                return true;
            }
        }

        return false;
    }

    static private String getItemStackTags(ItemStack is) {
        if (is == null) {
            return "";
        }

        StringBuilder tags = new StringBuilder();
        if (is.getItemMeta() != null && is.getItemMeta() instanceof LeatherArmorMeta armorMeta) {
            tags.append("display:{color:");
            tags.append(armorMeta.getColor().asRGB());
            tags.append("}");
        }

        Map<Enchantment, Integer> enchants = is.getEnchantments();
        if (!enchants.isEmpty()) {
            if (!tags.isEmpty()) {
                tags.append(",");
            }

            tags.append("Enchantments:[");

            for (Enchantment e : enchants.keySet()) {
                tags.append("{id:");
                tags.append(e.getKey().getKey());
                tags.append(",lvl:");
                tags.append(enchants.get(e));
                tags.append("},");
            }

            tags.setCharAt(tags.length() - 1, ']');
        }

        return tags.isEmpty() ? "" : tags.toString();
    }
    
    static private int getItemCustomModelData(ItemStack is) {
        if (is == null || is.getItemMeta() == null || !is.getItemMeta().hasCustomModelData()) {
            return 0;
        }

        return is.getItemMeta().getCustomModelData();
    }

    static private String skullOwner(ItemStack is) {
        if (is == null || is.getItemMeta() == null || !(is.getItemMeta() instanceof SkullMeta skull)) {
            return "";
        }

        return skull.getOwningPlayer() == null ? "" : "SkullOwner:\"" + skull.getOwner() + "\"";
    }

    static private boolean isEmpty(ItemStack is) {
        return is == null || is.getType() == Material.AIR;
    }

    static private String itemInfo(ItemStack is) {
        if (isEmpty(is)) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{id:");
        sb.append(is.getType().getKey().getKey());
        if (is.getAmount() > 0) {
            sb.append(",Count:").append(is.getAmount());
        }

        String itemStackTags = getItemStackTags(is);
        @SuppressWarnings("deprecation")
        short durability = is.getDurability();
        String skullOwner = skullOwner(is);
        int customModelData = getItemCustomModelData(is);
        int n = 0;
        if (!itemStackTags.isEmpty() || durability > 0 || !skullOwner.isEmpty()) {
            sb.append(",tag:{");
            if (durability > 0) {
                sb.append("Damage:").append(durability);
                n++;
            }

            if (customModelData > 0) {
                if (n > 0) sb.append(",");
                sb.append("CustomModelData:").append(customModelData);
                n++;
            }

            if (!itemStackTags.isEmpty()) {
                if (n > 0) sb.append(",");
                sb.append(itemStackTags);
                n++;
            }

            if (!skullOwner.isEmpty()) {
                if (n > 0) sb.append(",");
                sb.append(skullOwner);
            }

            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    static private String armorItems(EntityEquipment e) {
        if (e == null || (isEmpty(e.getBoots()) && isEmpty(e.getLeggings()) && isEmpty(e.getChestplate()) && isEmpty(e.getHelmet()))) {
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
        if (e == null || (isEmpty(e.getItemInMainHand()) && isEmpty(e.getItemInOffHand()))) {
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
        return "summon minecraft:armor_stand " +
               twoDec(asLocation.getX()) + " " +
               twoDec(asLocation.getY()) + " " +
               twoDec(asLocation.getZ()) + " " +
               createEntityTag(as);
    }

    static String quote(String s) {
        return "\"\\\"" + s + "\\\"\"";
    }

    static String createGiveCommand(ArmorStand as, Player p) {
        StringBuilder sb = new StringBuilder("minecraft:give ");
        sb.append(p.getName()).append(" minecraft:armor_stand{Enchantments:[{id:unbreaking,lvl:1}],HideFlags:1,display:{Name:");
        sb.append(quote(Config.configuredArmorStand));
        sb.append(",Lore:[");
        boolean comma = false;
        for (String s : createItemLore(as)) {
            if (comma) {
                sb.append(",");
            }

            comma = true;
            sb.append(quote(s));
        }

        sb.append("]},EntityTag:").append(createEntityTag(as)).append("}");
        return sb.toString();
    }

    static String createEntityTag(ArmorStand as) {
        EntityEquipment e = as.getEquipment();
        StringBuilder sb = new StringBuilder("{");
        if (!as.isVisible()) {
            sb.append("Invisible:1,");
        }

        if (!as.hasBasePlate()) {
            sb.append("NoBasePlate:1,");
        }

        if (!as.hasGravity()) {
            sb.append("NoGravity:1,");
        }

        if (as.hasArms()) {
            sb.append("ShowArms:1,");
        }

        if (as.isSmall()) {
            sb.append("Small:1,");
        }

        if (as.isInvulnerable()) {
            sb.append("Invulnerable:1,");
        }

        if (as.isGlowing()) {
            sb.append("Glowing:1,");
        }

        if (hasDisabledSlots(as)) {
            sb.append("DisabledSlots:").append(disabledSlotsAsInteger(as)).append(",");
        }

        if (as.isCustomNameVisible()) {
            sb.append("CustomNameVisible:1,");
        }

        if (as.getCustomName() != null) {
            sb.append("CustomName:").append(quote(as.getCustomName())).append(",");
        }

        if (as.getLocation().getYaw() != 0F) {
            sb.append("Rotation:[").append(twoDec(as.getLocation().getYaw())).append("f],");
        }

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
        if (name != null && !name.isEmpty()) {
            lore.add(Config.name + ": " + name);
        }

        int stacks = 0;
        int items = 0;
        if (e.getHelmet() != null) {
            { stacks++; items += e.getHelmet().getAmount(); }
        }

        if (e.getChestplate() != null) {
            { stacks++; items += e.getChestplate().getAmount(); }
        }

        if (e.getLeggings() != null) {
            { stacks++; items += e.getLeggings().getAmount(); }
        }

        if (e.getBoots() != null) {
            { stacks++; items += e.getBoots().getAmount(); }
        }

        if (Material.AIR != e.getItemInMainHand().getType()) {
            { stacks++; items += e.getItemInMainHand().getAmount(); }
        }

        if (Material.AIR != e.getItemInOffHand().getType()) {
            { stacks++; items += e.getItemInOffHand().getAmount(); }
        }

        if (stacks > 0) {
            lore.add(Config.inventory + ": " + items + " " + Config.items + " (" + stacks + " " + Config.stacks + ")");
        }

        List<String> attribs = new ArrayList<>();
        if (hasDisabledSlots(as)) {
            attribs.add(Config.equip + " " + Config.locked);
        }

        if (!as.hasGravity()) {
            attribs.add(Config.gravity + " " + Config.isOff);
        }

        if (!as.isVisible()) {
            attribs.add(Config.invisible);
        }

        if (as.hasArms()) {
            attribs.add(Config.arms);
        }

        if (as.isSmall()) {
            attribs.add(Config.small);
        }

        if (as.isInvulnerable()) {
            attribs.add(Config.invuln);
        }

        if (as.isGlowing()) {
            attribs.add(Config.glowing);
        }

        if (!attribs.isEmpty()) {
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
        if (twoDec == null) {
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
        for (ItemStack i : p.getInventory()) {
            if (ArmorStandTool.isTool(i)) {
                return true;
            }
        }

        return false;
    }

    private static boolean onCooldown(Entity e) {
        for (MetadataValue meta : e.getMetadata("lastEvent")) {
            if (AST.plugin.equals(meta.getOwningPlugin())) {
                return System.currentTimeMillis() - meta.asLong() < 100;
            }
        }

        return false;
    }

    static void cycleInventory(Player p) {
        if (onCooldown(p)) {
            return;
        }

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
        cloneEquipment.setHelmet(asEquipment.getHelmet());
        cloneEquipment.setChestplate(asEquipment.getChestplate());
        cloneEquipment.setLeggings(asEquipment.getLeggings());
        cloneEquipment.setBoots(asEquipment.getBoots());
        cloneEquipment.setItemInMainHand(asEquipment.getItemInMainHand());
        cloneEquipment.setItemInOffHand(asEquipment.getItemInOffHand());
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
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                if (as.hasEquipmentLock(slot, lockType)) {
                    clone.addEquipmentLock(slot, lockType);
                }
            }
        }

        ArmorStandCmdManager cmdMgrAs = new ArmorStandCmdManager(as);
        ArmorStandCmdManager cmdMgrClone = new ArmorStandCmdManager(clone);
        for (ArmorStandCmd asCmd : cmdMgrAs.getCommands()) {
            cmdMgrClone.addCommand(asCmd, true);
        }

        int cooldown = cmdMgrAs.getCooldownTime();
        if (cooldown > 0) {
            cmdMgrClone.setCooldownTime(cooldown);
        }

        clone.setMetadata("clone", new FixedMetadataValue(AST.plugin, true));
        return clone;
    }
}

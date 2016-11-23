package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Map;

class NBT {

    private static String disabledSlotsFieldName;
    private static String getDisabledSlotsFieldName() {
        if(disabledSlotsFieldName != null) return disabledSlotsFieldName;
        if(Main.NMS_VERSION.startsWith("v1_9_R2")) {
            disabledSlotsFieldName = "bA";
        } else if(Main.NMS_VERSION.startsWith("v1_10")) {
            disabledSlotsFieldName = "bB";
        } else if(Main.NMS_VERSION.startsWith("v1_11")) {
            disabledSlotsFieldName = "bA";
        } else {
            disabledSlotsFieldName = "bA"; // If ver greater than 1.11 then assume the same as 1.11 and hope that it didn't change :)
        }
        return disabledSlotsFieldName;
    }

    static boolean toggleSlotsDisabled(ArmorStand as) {
        boolean slotsDisabled = getDisabledSlots(as) == 0;
        setSlotsDisabled(as, slotsDisabled);
        return slotsDisabled;
    }

    static int getDisabledSlots(ArmorStand as) {
        Object nmsEntity = getNmsEntity(as);
        if(nmsEntity == null) return 0;
        Field f;
        try {
            f = nmsEntity.getClass().getDeclaredField(getDisabledSlotsFieldName());
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

    static void setSlotsDisabled(ArmorStand as, boolean slotsDisabled) {
        Object nmsEntity = getNmsEntity(as);
        if (nmsEntity == null) return;
        Field f;
        try {
            f = nmsEntity.getClass().getDeclaredField(getDisabledSlotsFieldName());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }
        f.setAccessible(true);
        try {
            f.set(nmsEntity, slotsDisabled ? 2039583 : 0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static boolean toggleInvulnerability(ArmorStand as) {
        boolean isInvulnerable = !as.isInvulnerable();
        as.setInvulnerable(isInvulnerable);
        return isInvulnerable;
    }

    private static Object getNmsEntity(org.bukkit.entity.Entity entity) {
        try {
            return entity.getClass().getMethod("getHandle").invoke(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    static String getItemStackTags(ItemStack is) {
        if(is == null) {
            return "";
        }
        StringBuilder tags = new StringBuilder("");
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
            tags.append("ench:[");

            for(Enchantment e : enchants.keySet()) {
                tags.append("{id:");
                tags.append(e.getId());
                tags.append(",lvl:");
                tags.append(enchants.get(e));
                tags.append("},");
            }

            tags.setCharAt(tags.length() - 1, ']');
        }
        return tags.length() == 0 ? "" : (",tag:{" + tags.toString() + "}");
    }

    static String skullOwner(ItemStack is) {
        if(is == null || is.getItemMeta() == null || !(is.getItemMeta() instanceof SkullMeta)) {
            return "";
        }
        SkullMeta skull = (SkullMeta) is.getItemMeta();
        if(skull.hasOwner()) {
            return ",tag:{SkullOwner:\"" + skull.getOwner() + "\"}";
        } else {
            return "";
        }
    }
}

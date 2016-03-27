package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

class NBT {

    static boolean toggleSlotsDisabled(ArmorStand as) {
        boolean slotsDisabled = getDisabledSlots(as) == 0;
        setSlotsDisabled(as, slotsDisabled);
        return slotsDisabled;
    }

    static int getDisabledSlots(ArmorStand as) {
        Object nmsEntity = getNmsEntity(as);
        if(nmsEntity == null) return 0;
        if(Main.one_nine) {
            Field f;
            try {
                f = nmsEntity.getClass().getDeclaredField("bz");
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
        } else {
            Object tag = getTag(nmsEntity);
            if (tag == null) return 0;
            return getInt(tag, "DisabledSlots");
        }
    }

    static void setSlotsDisabled(ArmorStand as, boolean slotsDisabled) {
        Object nmsEntity = getNmsEntity(as);
        if (nmsEntity == null) return;
        if(Main.one_nine) {
            Field f;
            try {
                f = nmsEntity.getClass().getDeclaredField("bz");
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
        } else {
            Object tag = getTag(nmsEntity);
            if (tag == null) return;
            setInt(tag, "DisabledSlots", slotsDisabled ? 2039583 : 0);
            saveTagA(nmsEntity, tag);
        }
    }

    static boolean toggleInvulnerability(ArmorStand as) {
        boolean isInvulnerable = !isInvulnerable(as);
        setInvulnerable(as, isInvulnerable);
        return isInvulnerable;
    }

    static boolean isInvulnerable(ArmorStand as) {
        Object nmsEntity = getNmsEntity(as);
        if (nmsEntity == null) return false;
        if(Main.one_nine) {
            Field f;
            try {
                f = Utils.getNMSClass("Entity").getDeclaredField("invulnerable");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            f.setAccessible(true);
            try {
                return (Boolean) f.get(nmsEntity);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Object tag = getTag(nmsEntity);
            return tag != null && getBoolean(tag, "Invulnerable");
        }
    }

    static void setInvulnerable(ArmorStand as, boolean invulnerable) {
        Object nmsEntity = getNmsEntity(as);
        if (nmsEntity == null) return;
        if(Main.one_nine) {
            Field f;
            try {
                f = Utils.getNMSClass("Entity").getDeclaredField("invulnerable");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            f.setAccessible(true);
            try {
                f.set(nmsEntity, invulnerable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Object tag = getTag(nmsEntity);
            if(tag == null) return;
            setBoolean(tag, "Invulnerable", invulnerable);
            saveTagF(nmsEntity, tag);
        }
    }


    private static Object getNmsEntity(org.bukkit.entity.Entity entity) {
        try {
            return entity.getClass().getMethod("getHandle").invoke(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object getTag(Object nmsEntity) {

        try {
            Method method = nmsEntity.getClass().getMethod("getNBTTag");
            Object tag = method.invoke(nmsEntity);
            if(tag == null) {
                tag = Utils.getNMSClass("NBTTagCompound").newInstance();
            }
            method = nmsEntity.getClass().getMethod("c", Utils.getNMSClass("NBTTagCompound"));
            method.invoke(nmsEntity, tag);
            return tag;
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

    private static int getInt(Object tag, String name) {
        try {
            return (Integer) tag.getClass().getMethod("getInt", String.class).invoke(tag, name);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static boolean getBoolean(Object tag, String name) {
        try {
            return (Boolean) tag.getClass().getMethod("getBoolean", String.class).invoke(tag, name);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void setInt(Object tag, String name, int value) {
        try {
            tag.getClass().getMethod("setInt", String.class, int.class).invoke(tag, name, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setBoolean(Object tag, String name, boolean value) {
        try {
            tag.getClass().getMethod("setBoolean", String.class, boolean.class).invoke(tag, name, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveTagA(Object nmsEntity, Object tag) {
        try {
            nmsEntity.getClass().getMethod("a", Utils.getNMSClass("NBTTagCompound")).invoke(nmsEntity, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveTagF(Object nmsEntity, Object tag) {
        try {
            nmsEntity.getClass().getMethod("f", Utils.getNMSClass("NBTTagCompound")).invoke(nmsEntity, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

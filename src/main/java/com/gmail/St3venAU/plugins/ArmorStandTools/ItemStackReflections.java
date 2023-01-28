package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ItemStackReflections {
    private final static Method AS_NMS_COPY;
    private final static Method GET_TAG;
    private final static Method MODIFY_ITEM_STACK;
    private final static Object CRAFT_MAGIC_NUMBERS;
    private static final String SERVER_VERSION;

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String[] split = name.split("\\.");
        name = split[split.length - 1];
        SERVER_VERSION = name;

        try {
            Class<?> mcItemStack = Class.forName("net.minecraft.world.item.ItemStack");
            Class<?> bukkitItemStack = Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + ".inventory.CraftItemStack");
            Class<?> craftMagicNumbers = Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + ".util.CraftMagicNumbers");
            AS_NMS_COPY = bukkitItemStack.getMethod("asNMSCopy", ItemStack.class);
            GET_TAG = mcItemStack.getDeclaredMethod("getTagClone");
            GET_TAG.setAccessible(true);
            MODIFY_ITEM_STACK = craftMagicNumbers.getDeclaredMethod("modifyItemStack", ItemStack.class, String.class);
            CRAFT_MAGIC_NUMBERS = craftMagicNumbers.getField("INSTANCE").get(null);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    // CREDIT: This function is based on the MiniNBT library: https://github.com/I-Al-Istannen/MiniNBT
    public static String itemNBTToString(ItemStack item){
        Object nmsItem;
        try {
            nmsItem = AS_NMS_COPY.invoke(null, item);

            if (nmsItem == null) {
                throw new NullPointerException("Unable to find a nms item clone for " + item);
            }

            Object tag = GET_TAG.invoke(nmsItem);

            if (tag == null) {
                return null;
            }

            return tag.toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setItemNBTFromString(ItemStack itemStack, String nbt){
        try {
            MODIFY_ITEM_STACK.invoke(CRAFT_MAGIC_NUMBERS, itemStack, nbt);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

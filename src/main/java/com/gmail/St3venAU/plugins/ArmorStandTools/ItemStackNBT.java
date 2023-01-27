package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ItemStackNBT {
    private final static Method AS_NMS_COPY;
    private final static Method GET_TAG;
    private final static Method TAG_TO_STRING;
    private static final String SERVER_VERSION;

    static {
        boolean runningUnderTest = Bukkit.getServer() == null
                || Bukkit.getServer().getClass().getName().contains("Mockito");

        String name = runningUnderTest
                ? "org.bukkit.craftbukkit.v1_14_R1"
                : Bukkit.getServer().getClass().getPackage().getName();
        String[] split = name.split("\\.");
        name = split[split.length - 1];

        SERVER_VERSION = name;
        Class<?> craftItemStackClass;
        Class<?> itemStackClass;
        Class<?> compoundTagClass;
        try {
            craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + ".inventory.CraftItemStack");
            itemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
            compoundTagClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            AS_NMS_COPY = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            GET_TAG = itemStackClass.getDeclaredMethod("getTagClone");
            GET_TAG.setAccessible(true);
            TAG_TO_STRING = compoundTagClass.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static String itemToString(ItemStack item){
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

            return (String) TAG_TO_STRING.invoke(tag);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    //
}

package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Reflections {
    private final static Method AS_NMS_COPY;
    private final static Method GET_TAG;
    private static final String SERVER_VERSION;
    private static final ProxiedCommandSender PROXIED_COMMAND_SENDER;

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String[] split = name.split("\\.");
        name = split[split.length - 1];
        SERVER_VERSION = name;

        try {
            AS_NMS_COPY = Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + ".inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
            GET_TAG = Class.forName("net.minecraft.world.item.ItemStack").getDeclaredMethod("getTagClone");
            GET_TAG.setAccessible(true);

            Object commandSourceStack = Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + ".command.VanillaCommandWrapper").getMethod("getListener", CommandSender.class).invoke(null, Bukkit.getConsoleSender());
            Method suppressOutput = Arrays.stream(commandSourceStack.getClass().getDeclaredMethods()).filter(m -> m.getParameters().length == 0 && m.getReturnType() == commandSourceStack.getClass()).findAny().orElse(null);
            if(suppressOutput == null){
                throw new RuntimeException("Cant find method withSuppressedOutput() of net.minecraft.commands.CommandListenerWrapper");
            }
            PROXIED_COMMAND_SENDER = (ProxiedCommandSender) Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + ".command.ProxiedNativeCommandSender")
                    .getConstructor(commandSourceStack.getClass(), CommandSender.class, CommandSender.class)
                    .newInstance(suppressOutput.invoke(commandSourceStack), Bukkit.getConsoleSender(), Bukkit.getConsoleSender());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    // CREDIT: This function is based on the MiniNBT library: https://github.com/I-Al-Istannen/MiniNBT
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

            return tag.toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    // Run a command with no output to the console
    public static void callCommandSilently(String command){
        Bukkit.dispatchCommand(PROXIED_COMMAND_SENDER, command);
    }
}

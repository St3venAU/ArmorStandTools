package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collection;

class Utils {

    private static DecimalFormat twoDec;

    static boolean containsItems(Collection<ItemStack> items) {
        for(ItemStack i : items) {
            if(ArmorStandTool.get(i) != null) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasPermissionNode(Player player, String perm) {
        if ((player == null) || player.isOp()) {
            return true;
        }
        if (player.hasPermission(perm)) {
            return true;
        }
        final String[] nodes = perm.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i]).append(".");
            if (player.hasPermission(n + "*")) {
                return true;
            }
        }
        return false;
    }

    static boolean hasItems(Player p) {
        for(ItemStack i : p.getInventory()) {
            if(ArmorStandTool.isTool(i)) {
                return true;
            }
        }
        return false;
    }

    static Location getLocationFacingPlayer(Player p) {
        Vector v = p.getLocation().getDirection();
        v.setY(0);
        v.multiply(3);
        Location l = p.getLocation().add(v);
        l.setYaw(p.getLocation().getYaw() + 180);
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

    @SuppressWarnings("deprecation")
    static void cycleInventory(Player p) {
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
    }

    static void actionBarMsg(Player p, String msg) {
        try {
            Object chat = getNMSClass("ChatSerializer").getMethod("a", String.class).invoke(null, "{text:\"" + msg + "\"}");
            Object packet = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent"), byte.class).newInstance(chat, (byte) 2);
            sendPacket(p, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void openSign(Player p, Block b) {
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

    static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        if(nmsClassString.equals("ChatSerializer") && !Main.NMS_VERSION.equals("v1_8_R1")) {
            nmsClassString = "IChatBaseComponent$ChatSerializer";
        }
        return Class.forName("net.minecraft.server." + Main.NMS_VERSION + "." + nmsClassString);
    }

    static boolean loadProfile(String name) {
        try {
            Object server = getNMSClass("MinecraftServer").getMethod("getServer").invoke(null);
            Object cache = server.getClass().getMethod("getUserCache").invoke(server);
            return cache.getClass().getMethod("getProfile", String.class).invoke(cache, name) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void sendPacket(Player p, Object packet) {
        try {
            Object player = p.getClass().getMethod("getHandle").invoke(p);
            Object connection = player.getClass().getField("playerConnection").get(player);
            connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String angle(double d) {
        return twoDec(d * 180.0 / Math.PI);
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

    static Block findAnAirBlock(Location l) {
        while(l.getY() < 255 && l.getBlock().getType() != Material.AIR) {
            l.add(0, 1, 0);
        }
        return l.getY() < 255 && l.getBlock().getType() == Material.AIR ? l.getBlock() : null;
    }

    static ItemStack setLore(ItemStack is, String... lore) {
        ItemMeta meta = is.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        is.setItemMeta(meta);
        return is;
    }

}

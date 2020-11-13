package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
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

    static boolean hasPermissionNode(Player player, String perm, boolean ignoreOp) {
        if (!ignoreOp && ((player == null) || player.isOp())) {
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

    static boolean hasPermissionNode(Player player, String perm) {
        return hasPermissionNode(player, perm, false);
    }

    static boolean hasAnyTools(Player p) {
        for(ItemStack i : p.getInventory()) {
            if(ArmorStandTool.isTool(i)) {
                return true;
            }
        }
        return false;
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
        //noinspection deprecation
        p.updateInventory();
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

    static boolean toggleInvulnerability(ArmorStand as) {
        boolean inv = !as.isInvulnerable();
        as.setInvulnerable(inv);
        return inv;
    }

    static void actionBarMsg(Player p, String msg) {
        p.sendTitle("", msg, 0, 70, 0);
    }

    static boolean toggleGlow(ArmorStand as) {
        boolean glowing = !as.isGlowing();
        as.setGlowing(glowing);
        return glowing;
    }
}

package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public enum ArmorStandTool {
    HEADX   ("headX",   Material.JACK_O_LANTERN,    (short) 0, 27, true),
    HEADY   ("headY",   Material.JACK_O_LANTERN,    (short) 0, 28, true),
    HEADZ   ("headZ",   Material.JACK_O_LANTERN,    (short) 0, 29, true),
    LARMX   ("lArmX",   Material.TORCH,             (short) 0, 30, true),
    LARMY   ("lArmY",   Material.TORCH,             (short) 0, 31, true),
    LARMZ   ("lArmZ",   Material.TORCH,             (short) 0, 32, true),
    RARMX   ("rArmX",   Material.REDSTONE_TORCH_ON, (short) 0, 33, true),
    RARMY   ("rArmY",   Material.REDSTONE_TORCH_ON, (short) 0, 34, true),
    RARMZ   ("rArmZ",   Material.REDSTONE_TORCH_ON, (short) 0, 35, true),
    MOVEX   ("moveX",   Material.SHEARS,            (short) 0, 24, true),
    MOVEY   ("moveY",   Material.SHEARS,            (short) 0, 25, true),
    MOVEZ   ("moveZ",   Material.SHEARS,            (short) 0, 26, true),
    ROTAT   ("rotat",   Material.MAGMA_CREAM,       (short) 0,  6, true),
    INVIS   ("invis",   Material.GOLD_NUGGET,       (short) 0,  5, true),
    SUMMON  ("summon",  Material.ARMOR_STAND,       (short) 0,  0, true),
    CLONE   ("clone",   Material.GLOWSTONE_DUST,    (short) 0, 16, true),
    SAVE    ("save",    Material.DIAMOND,           (short) 0, 17, true),
    LLEGX   ("lLegX",   Material.BONE,              (short) 0, 18, true),
    LLEGY   ("lLegY",   Material.BONE,              (short) 0, 19, true),
    LLEGZ   ("lLegZ",   Material.BONE,              (short) 0, 20, true),
    RLEGX   ("rLegX",   Material.BLAZE_ROD,         (short) 0, 21, true),
    RLEGY   ("rLegY",   Material.BLAZE_ROD,         (short) 0, 22, true),
    RLEGZ   ("rLegZ",   Material.BLAZE_ROD,         (short) 0, 23, true),
    BODYX   ("bodyX",   Material.NETHER_BRICK_ITEM, (short) 0,  9, true),
    BODYY   ("bodyY",   Material.NETHER_BRICK_ITEM, (short) 0, 10, true),
    BODYZ   ("bodyZ",   Material.NETHER_BRICK_ITEM, (short) 0, 11, true),
    SIZE    ("size",    Material.EMERALD,           (short) 0,  3, true),
    BASE    ("base",    Material.BOOK,              (short) 0,  4, true),
    GRAV    ("grav",    Material.GHAST_TEAR,        (short) 0,  1, true),
    ARMS    ("arms",    Material.ARROW,             (short) 0,  2, true),
    NAME    ("name",    Material.NAME_TAG,          (short) 0,  7, true),
    SLOTS   ("slots",   Material.IRON_HOE,          (short) 0, 13, true),
    PHEAD   ("pHead",   Material.SKULL_ITEM,        (short) 3,  8, true),
    INVUL   ("invul",   Material.GOLDEN_CARROT,     (short) 0, 12, true),
    MOVE    ("move",    Material.FEATHER,           (short) 0, 14, true),
    NODEL   ("noDel",   Material.WOOD_SPADE,        (short) 0, 15, false); // Developer tool, disabled by default

    private final ItemStack item;
    private final String id;
    private final int slot;
    private final boolean enabled;
    
    ArmorStandTool(String id, Material m, short data, int slot, boolean enabled) {
        item = new ItemStack(m, 1, data);
        this.id = id;
        this.slot = slot;
        this.enabled = enabled;
    }

    ItemStack getItem() {
        return item;
    }

    boolean is(ItemStack is) {
        return is != null && is.getType() == item.getType() && is.hasItemMeta() &&
               is.getItemMeta().hasDisplayName() &&
               is.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName());
    }

    static void updateTools(FileConfiguration config) {
        for(ArmorStandTool t : values()) {
            ItemMeta im = t.item.getItemMeta();
            im.setDisplayName(ChatColor.GREEN + config.getString("tool." + t.id + ".name"));
            im.setLore(config.getStringList("tool." + t.id + ".lore"));
            t.item.setItemMeta(im);
        }
    }

    static void give(Player p) {
        PlayerInventory i = p.getInventory();
        for(ArmorStandTool t : values()) {
            if(t.enabled) {
                i.setItem(t.slot, t.item);
            }
        }
    }

    static ArmorStandTool get(ItemStack is) {
        if(is == null || !is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) return null;
        for(ArmorStandTool t : values()) {
            if(t.is(is)) return t;
        }
        return null;
    }

    static boolean isTool(ItemStack is) {
        return get(is) != null;
    }
}

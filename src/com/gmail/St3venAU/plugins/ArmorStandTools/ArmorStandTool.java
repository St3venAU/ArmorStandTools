package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public enum ArmorStandTool {
    HEADX   ("headX",       Material.JACK_O_LANTERN,    (short) 0, 12, false, true),
    HEADY   ("headY",       Material.JACK_O_LANTERN,    (short) 0, 13, false, true),
    HEADZ   ("headZ",       Material.JACK_O_LANTERN,    (short) 0, 14, false, true),
    LARMX   ("lArmX",       Material.TORCH,             (short) 0, 27, false, true),
    LARMY   ("lArmY",       Material.TORCH,             (short) 0, 28, false, true),
    LARMZ   ("lArmZ",       Material.TORCH,             (short) 0, 29, false, true),
    RARMX   ("rArmX",       Material.REDSTONE_TORCH_ON, (short) 0, 30, false, true),
    RARMY   ("rArmY",       Material.REDSTONE_TORCH_ON, (short) 0, 31, false, true),
    RARMZ   ("rArmZ",       Material.REDSTONE_TORCH_ON, (short) 0, 32, false, true),
    MOVEX   ("moveX",       Material.SHEARS,            (short) 0,  3, false, true),
    MOVEY   ("moveY",       Material.SHEARS,            (short) 0,  4, false, true),
    MOVEZ   ("moveZ",       Material.SHEARS,            (short) 0,  5, false, true),
    LLEGX   ("lLegX",       Material.BONE,              (short) 0, 18, false, true),
    LLEGY   ("lLegY",       Material.BONE,              (short) 0, 19, false, true),
    LLEGZ   ("lLegZ",       Material.BONE,              (short) 0, 20, false, true),
    RLEGX   ("rLegX",       Material.BLAZE_ROD,         (short) 0, 21, false, true),
    RLEGY   ("rLegY",       Material.BLAZE_ROD,         (short) 0, 22, false, true),
    RLEGZ   ("rLegZ",       Material.BLAZE_ROD,         (short) 0, 23, false, true),
    BODYX   ("bodyX",       Material.NETHER_BRICK_ITEM, (short) 0,  9, false, true),
    BODYY   ("bodyY",       Material.NETHER_BRICK_ITEM, (short) 0, 10, false, true),
    BODYZ   ("bodyZ",       Material.NETHER_BRICK_ITEM, (short) 0, 11, false, true),
    SUMMON  ("summon",      Material.ARMOR_STAND,       (short) 0,  0, false, true),
    GUI     ("gui",         Material.NETHER_STAR,       (short) 0,  1, false, true),
    ROTAT   ("rotat",       Material.MAGMA_CREAM,       (short) 0,  2, false, true),
    CLONE   ("gui_clone",   Material.GLOWSTONE_DUST,    (short) 0, 16, true,  true),
    SAVE    ("gui_save",    Material.DIAMOND,           (short) 0, 17, true,  true),
    INVIS   ("gui_invis",   Material.GOLD_NUGGET,       (short) 0, 14, true,  true),
    SIZE    ("gui_size",    Material.EMERALD,           (short) 0, 23, true,  true),
    BASE    ("gui_base",    Material.BOOK,              (short) 0, 22, true,  true),
    GRAV    ("gui_grav",    Material.GHAST_TEAR,        (short) 0, 24, true,  true),
    ARMS    ("gui_arms",    Material.ARROW,             (short) 0, 21, true,  true),
    NAME    ("gui_name",    Material.NAME_TAG,          (short) 0, 12, true,  true),
    SLOTS   ("gui_slots",   Material.IRON_HOE,          (short) 0, 26, true,  true),
    PHEAD   ("gui_pHead",   Material.SKULL_ITEM,        (short) 3, 13, true,  true),
    INVUL   ("gui_invul",   Material.GOLDEN_CARROT,     (short) 0, 25, true,  true),
    MOVE    ("gui_move",    Material.FEATHER,           (short) 0, 15, true,  true),
    NODEL   ("gui_noDel",   Material.WOOD_SPADE,        (short) 0, 35, true, false); // Developer tool, disabled by default

    private final ItemStack item;
    private final String id;
    private final int slot;
    private final boolean enabled;
    private final boolean forGui;

    ArmorStandTool(String id, Material m, short data, int slot, boolean forGui, boolean enabled) {
        item = new ItemStack(m, 1, data);
        this.id = id;
        this.slot = slot;
        this.forGui = forGui;
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

    boolean isForGui() {
        return forGui;
    }

    boolean isEnabled() {
        return enabled;
    }

    int getSlot() {
        return slot;
    }

    static void updateTools(FileConfiguration config) {
        for(ArmorStandTool t : values()) {
            ItemMeta im = t.item.getItemMeta();
            im.setDisplayName(ChatColor.YELLOW + config.getString("tool." + t.id + ".name"));
            im.setLore(config.getStringList("tool." + t.id + ".lore"));
            t.item.setItemMeta(im);
        }
    }

    static void give(Player p) {
        PlayerInventory i = p.getInventory();
        for(ArmorStandTool t : values()) {
            if(t.enabled && !t.forGui) {
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
package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public enum ArmorStandTool {
    HEADX   ("headX",       Material.JACK_O_LANTERN,        (short) 0, 12, false, true, "astools.use"),
    HEADY   ("headY",       Material.JACK_O_LANTERN,        (short) 0, 13, false, true, "astools.use"),
    HEADZ   ("headZ",       Material.JACK_O_LANTERN,        (short) 0, 14, false, true, "astools.use"),
    LARMX   ("lArmX",       Material.TORCH,                 (short) 0, 27, false, true, "astools.use"),
    LARMY   ("lArmY",       Material.TORCH,                 (short) 0, 28, false, true, "astools.use"),
    LARMZ   ("lArmZ",       Material.TORCH,                 (short) 0, 29, false, true, "astools.use"),
    RARMX   ("rArmX",       Material.REDSTONE_TORCH,        (short) 0, 30, false, true, "astools.use"),
    RARMY   ("rArmY",       Material.REDSTONE_TORCH,        (short) 0, 31, false, true, "astools.use"),
    RARMZ   ("rArmZ",       Material.REDSTONE_TORCH,        (short) 0, 32, false, true, "astools.use"),
    MOVEX   ("moveX",       Material.SHEARS,                (short) 0,  3, false, true, "astools.use"),
    MOVEY   ("moveY",       Material.SHEARS,                (short) 0,  4, false, true, "astools.use"),
    MOVEZ   ("moveZ",       Material.SHEARS,                (short) 0,  5, false, true, "astools.use"),
    LLEGX   ("lLegX",       Material.BONE,                  (short) 0, 18, false, true, "astools.use"),
    LLEGY   ("lLegY",       Material.BONE,                  (short) 0, 19, false, true, "astools.use"),
    LLEGZ   ("lLegZ",       Material.BONE,                  (short) 0, 20, false, true, "astools.use"),
    RLEGX   ("rLegX",       Material.BLAZE_ROD,             (short) 0, 21, false, true, "astools.use"),
    RLEGY   ("rLegY",       Material.BLAZE_ROD,             (short) 0, 22, false, true, "astools.use"),
    RLEGZ   ("rLegZ",       Material.BLAZE_ROD,             (short) 0, 23, false, true, "astools.use"),
    BODYX   ("bodyX",       Material.NETHER_BRICKS,         (short) 0,  9, false, true, "astools.use"),
    BODYY   ("bodyY",       Material.NETHER_BRICKS,         (short) 0, 10, false, true, "astools.use"),
    BODYZ   ("bodyZ",       Material.NETHER_BRICKS,         (short) 0, 11, false, true, "astools.use"),
    SUMMON  ("summon",      Material.ARMOR_STAND,           (short) 0,  0, false, true, "astools.summon"),
    GUI     ("gui",         Material.NETHER_STAR,           (short) 0,  1, false, true, "astools.use"),
    ROTAT   ("rotat",       Material.MAGMA_CREAM,           (short) 0,  2, false, true, "astools.use"),
    CLONE   ("gui_clone",   Material.GLOWSTONE_DUST,        (short) 0, 15, true,  true, "astools.clone"),
    SAVE    ("gui_save",    Material.DIAMOND,               (short) 0, 16, true,  true, "astools.cmdblock"),
    INVIS   ("gui_invis",   Material.GOLD_NUGGET,           (short) 0, 7,  true,  true, "astools.use"),
    SIZE    ("gui_size",    Material.EMERALD,               (short) 0, 25, true,  true, "astools.use"),
    BASE    ("gui_base",    Material.STONE_SLAB,            (short) 0, 24, true,  true, "astools.use"),
    GRAV    ("gui_grav",    Material.GHAST_TEAR,            (short) 0, 32, true,  true, "astools.use"),
    ARMS    ("gui_arms",    Material.ARROW,                 (short) 0, 23, true,  true, "astools.use"),
    NAME    ("gui_name",    Material.NAME_TAG,              (short) 0, 5,  true,  true, "astools.use"),
    SLOTS   ("gui_slots",   Material.IRON_HOE,              (short) 0, 34, true,  true, "astools.use"),
    PHEAD   ("gui_pHead",   Material.PLAYER_HEAD,           (short) 3, 6,  true,  true, "astools.head"),
    INVUL   ("gui_invul",   Material.GLISTERING_MELON_SLICE,(short) 0, 33, true,  true, "astools.use"),
    MOVE    ("gui_move",    Material.FEATHER,               (short) 0, 14, true,  true, "astools.use"),
    GLOW    ("gui_glow",    Material.GLOWSTONE,             (short) 0, 8,  true,  true, "astools.glow");

    private final ItemStack item;
    private final String id;
    private final int slot;
    private boolean enabled;
    private final boolean forGui;
    private final String permission;

    ArmorStandTool(String id, Material m, short data, int slot, boolean forGui, boolean enabled, String permission) {
        item = new ItemStack(m, 1, data);
        this.id = id;
        this.slot = slot;
        this.forGui = forGui;
        this.enabled = enabled;
        this.permission = permission;
    }

    ItemStack getItem() {
        return item;
    }

    private boolean is(ItemStack is) {
        return is != null && is.getType() == item.getType() && is.hasItemMeta() &&
                is.getItemMeta().hasDisplayName() &&
                is.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName());
    }

    boolean isForGui() {
        return forGui;
    }

    void setEnabled(FileConfiguration config) {
        enabled = config.getBoolean("enableTool." + id);
    }

    boolean isEnabled() {
        return enabled;
    }

    String getPermission() {
        return permission;
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

    static ArmorStandTool get(Player p) {
        return get(p.getInventory().getItemInMainHand());
    }

    static boolean isTool(ItemStack is) {
        return get(is) != null;
    }

    static boolean isHoldingTool(Player p) {
        return isTool(p.getInventory().getItemInMainHand());
    }
}
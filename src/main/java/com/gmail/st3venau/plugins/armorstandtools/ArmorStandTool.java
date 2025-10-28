package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public enum ArmorStandTool {
    HEADX   ("headX",       Material.JACK_O_LANTERN,         12, false, "astools.use",     false),
    HEADY   ("headY",       Material.JACK_O_LANTERN,         13, false, "astools.use",     false),
    HEADZ   ("headZ",       Material.JACK_O_LANTERN,         14, false, "astools.use",     false),
    LARMX   ("lArmX",       Material.TORCH,                  27, false, "astools.use",     false),
    LARMY   ("lArmY",       Material.TORCH,                  28, false, "astools.use",     false),
    LARMZ   ("lArmZ",       Material.TORCH,                  29, false, "astools.use",     false),
    RARMX   ("rArmX",       Material.REDSTONE_TORCH,         30, false, "astools.use",     false),
    RARMY   ("rArmY",       Material.REDSTONE_TORCH,         31, false, "astools.use",     false),
    RARMZ   ("rArmZ",       Material.REDSTONE_TORCH,         32, false, "astools.use",     false),
    MOVEX   ("moveX",       Material.SHEARS,                 3,  false, "astools.use",     false),
    MOVEY   ("moveY",       Material.SHEARS,                 4,  false, "astools.use",     false),
    MOVEZ   ("moveZ",       Material.SHEARS,                 5,  false, "astools.use",     false),
    LLEGX   ("lLegX",       Material.BONE,                   18, false, "astools.use",     false),
    LLEGY   ("lLegY",       Material.BONE,                   19, false, "astools.use",     false),
    LLEGZ   ("lLegZ",       Material.BONE,                   20, false, "astools.use",     false),
    RLEGX   ("rLegX",       Material.BLAZE_ROD,              21, false, "astools.use",     false),
    RLEGY   ("rLegY",       Material.BLAZE_ROD,              22, false, "astools.use",     false),
    RLEGZ   ("rLegZ",       Material.BLAZE_ROD,              23, false, "astools.use",     false),
    BODYX   ("bodyX",       Material.NETHER_BRICKS,          9,  false, "astools.use",     false),
    BODYY   ("bodyY",       Material.NETHER_BRICKS,          10, false, "astools.use",     false),
    BODYZ   ("bodyZ",       Material.NETHER_BRICKS,          11, false, "astools.use",     false),
    SUMMON  ("summon",      Material.ARMOR_STAND,            0,  false, "astools.summon",  false),
    GUI     ("gui",         Material.NETHER_STAR,            1,  false, "astools.use",     false),
    ROTAT   ("rotat",       Material.MAGMA_CREAM,            2,  false, "astools.use",     false),
    CLONE   ("gui_clone",   Material.GLOWSTONE_DUST,         44, true,  "astools.clone",   false),
    GEN_CMD ("gui_gen_cmd", Material.COMMAND_BLOCK,          53, true,  "astools.cmdblock",false),
    INVIS   ("gui_invis",   Material.GOLD_NUGGET,            42, true,  "astools.use",     false),
    SIZE    ("gui_size",    Material.EMERALD,                51, true,  "astools.use",     false),
    BASE    ("gui_base",    Material.STONE_SLAB,             41, true,  "astools.use",     false),
    GRAV    ("gui_grav",    Material.GHAST_TEAR,             49, true,  "astools.use",     false),
    ARMS    ("gui_arms",    Material.ARROW,                  40, true,  "astools.use",     false),
    NAME    ("gui_name",    Material.NAME_TAG,               39, true,  "astools.use",     false),
    SLOTS   ("gui_slots",   Material.IRON_HOE,               43, true,  "astools.use",     false),
    PHEAD   ("gui_pHead",   Material.PLAYER_HEAD,            48, true,  "astools.head",    false),
    INVUL   ("gui_invul",   Material.GLISTERING_MELON_SLICE, 50, true,  "astools.use",     false),
    MOVE    ("gui_move",    Material.FEATHER,                25, true,  "astools.use",     false),
    GLOW    ("gui_glow",    Material.GLOWSTONE,              52, true,  "astools.glow",    false),
    HEAD    ("gui_head",    Material.WITHER_SKELETON_SKULL,  7,  true,  "astools.use",     false),
    BODY    ("gui_body",    Material.NETHERITE_CHESTPLATE,   16, true,  "astools.use",     false),
    RARM    ("gui_rarm",    Material.REDSTONE_TORCH,         15, true,  "astools.use",     true),
    LARM    ("gui_larm",    Material.TORCH,                  17, true,  "astools.use",     true),
    RLEG    ("gui_rleg",    Material.BLAZE_ROD,              24, true,  "astools.use",     true),
    LLEG    ("gui_lleg",    Material.BONE,                   26, true,  "astools.use",     true),
    ITEM    ("gui_item",    Material.ARMOR_STAND,            34, true,  "astools.use",     false);

    private final ItemStack item;
    private final String config_id;
    private final int slot;
    private boolean enabled;
    private final boolean forGui;
    private final String permission;
    private final boolean reverseSneaking;

    private String name;

    ArmorStandTool(String config_id, Material m, int slot, boolean forGui, String permission, boolean reverseSneaking) {
        item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        this.config_id = config_id;
        this.slot = slot;
        this.forGui = forGui;
        this.permission = permission;
        this.reverseSneaking = reverseSneaking;
    }

    void showTitle(Player p) {
        boolean sneaking = p.isSneaking();
        ChatColor offColor = ChatColor.WHITE;
        ChatColor onColor = ChatColor.YELLOW;
        ChatColor divColor = ChatColor.BLACK;
        String msg =
            (sneaking ? offColor : onColor) +
            Config.normal + ": X/" + (reverseSneaking ? "Z" : "Y") +
            divColor + " | " +
            (sneaking ? onColor : offColor) +
            Config.crouch + ": X/" + (reverseSneaking ? "Y" : "Z") +
            divColor + " | " +
            offColor + Config.click + ": " + Config.finish;
        p.sendTitle(" ", msg, 0, 600, 0);
    }

    ItemStack getItem() {
        return item;
    }

    private boolean is(ItemStack is) {
        return is != null &&
               is.getType() == item.getType() &&
               is.getItemMeta() != null &&
               is.getItemMeta().hasDisplayName() &&
               item.getItemMeta() != null &&
               is.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName());
    }

    boolean isForGui() {
        return forGui;
    }

    void setEnabled(FileConfiguration config) {
        enabled = config.getBoolean("enableTool." + config_id);
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

    void use(Player p, ArmorStand as) {
        if (as == null || as.isDead()) {
            UUID uuid = p.getUniqueId();
            AST.selectedArmorStand.remove(uuid);
            AST.activeTool.remove(uuid);
            return;
        }

        if (this == MOVE) {
            AST.scheduler().runAtEntity(as, task -> as.teleportAsync(Utils.getLocationFacing(p.getLocation())));
            Utils.title(p, Config.carrying);
            return;
        }

        showTitle(p);
        EulerAngle eulerAngle = switch (this) {
            case HEAD -> as.getHeadPose();
            case BODY -> as.getBodyPose();
            case LARM -> as.getLeftArmPose();
            case RARM -> as.getRightArmPose();
            case LLEG -> as.getLeftLegPose();
            case RLEG -> as.getRightLegPose();
            default -> null;
        };

        if (eulerAngle == null) {
            return;
        }

        eulerAngle = eulerAngle.setX(getPitch(p));
        boolean sneaking = reverseSneaking != p.isSneaking();
        double yaw = getRelativeYaw(p, as);
        eulerAngle = sneaking ? eulerAngle.setZ(yaw) : eulerAngle.setY(yaw);
        switch (this) {
            case HEAD -> as.setHeadPose(eulerAngle);
            case BODY -> as.setBodyPose(eulerAngle);
            case LARM -> as.setLeftArmPose(eulerAngle);
            case RARM -> as.setRightArmPose(eulerAngle);
            case LLEG -> as.setLeftLegPose(eulerAngle);
            case RLEG -> as.setRightLegPose(eulerAngle);
        }
    }

    // Get pitch and format as 0 to 2pi radians
    // Actual pitch multiplied for increased sensitivity
    private double getPitch(Player p) {
        double pitch = p.getLocation().getPitch() * 4;
        while (pitch < 0) {
            pitch += 360;
        }

        while (pitch > 360) {
            pitch -= 360;
        }

        return pitch * Math.PI / 180.0;
    }

    // Get yaw relative to armor stand facing direction and format as 0 to 2pi radians
    // Actual yaw doubled for increased sensitivity, and reversed to create mirror effect
    private double getRelativeYaw(Player p, ArmorStand as) {
        double difference = p.getLocation().getYaw() - as.getLocation().getYaw();
        double yaw = 360.0 - (difference * 2);
        while (yaw < 0) {
            yaw += 360;
        }

        while (yaw > 360) {
            yaw -= 360;
        }

        return yaw * Math.PI / 180.0;
    }

    static void give(Player p) {
        PlayerInventory i = p.getInventory();
        for (ArmorStandTool t : values()) {
            if (t.enabled && !t.forGui) {
                i.setItem(t.slot, t.item);
            }
        }
    }

    static boolean isTool(ItemStack is) {
        return get(is) != null;
    }

    static boolean isHoldingTool(Player p) {
        return isTool(p.getInventory().getItemInMainHand());
    }

    ItemStack updateLore(ArmorStand as) {
        return switch (this) {
            case INVIS -> setLore(item, ChatColor.AQUA + Config.asVisible + ": " + (as.isVisible() ? (ChatColor.GREEN + Config.isTrue) : (ChatColor.RED + Config.isFalse)));
            case SIZE -> setLore(item, ChatColor.AQUA + Config.size + ": " + (as.isSmall() ? (ChatColor.BLUE + Config.small) : (ChatColor.GREEN + Config.normal)));
            case BASE -> setLore(item, ChatColor.AQUA + Config.basePlate + ": " + (as.hasBasePlate() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            case GRAV -> setLore(item, ChatColor.AQUA + Config.gravity + ": " + (as.hasGravity() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            case ARMS -> setLore(item, ChatColor.AQUA + Config.arms + ": " + (as.hasArms() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            case INVUL -> setLore(item, ChatColor.AQUA + Config.invul + ": " + (as.isInvulnerable() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            case SLOTS -> setLore(item, ChatColor.AQUA + Config.equip + ": " + (Utils.hasDisabledSlots(as) ? (ChatColor.GREEN + Config.locked) : (ChatColor.RED + Config.unLocked)));
            case NAME -> setLore(item, ChatColor.AQUA + Config.currently + ": " + (as.getCustomName() == null ? (ChatColor.BLUE + Config.none) : (ChatColor.GREEN + as.getCustomName())));
            case PHEAD -> {
                String name = plrHeadName(as);
                yield setLore(item, ChatColor.AQUA + Config.currently + ": " + (name == null ? (ChatColor.BLUE + Config.none) : (ChatColor.GREEN + name)));
            }
            case GLOW -> setLore(item, ChatColor.AQUA + Config.glow + ": " + (as.isGlowing() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            default -> item;
        };
    }

    private ItemStack setLore(ItemStack is, String... lore) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setLore(Arrays.asList(lore));
            is.setItemMeta(meta);
        }

        return is;
    }

    private String plrHeadName(ArmorStand as) {
        EntityEquipment entityEquipment = as.getEquipment();
        if (entityEquipment.getHelmet() == null || !(entityEquipment.getHelmet().getItemMeta() instanceof SkullMeta meta)) {
            return null;
        }

        if (meta.getOwningPlayer() == null) {
            return null;
        }

        return meta.getOwningPlayer().getName();
    }

    static ArmorStandTool get(Player p) {
        return get(p.getInventory().getItemInMainHand());
    }

    static ArmorStandTool get(ItemStack is) {
        if (is == null || is.getItemMeta() == null || !is.getItemMeta().hasDisplayName()) {
            return null;
        }

        for (ArmorStandTool t : values()) {
            if (t.is(is)) {
                return t;
            }
        }

        return null;
    }

    static void updateTools(FileConfiguration config) {
        for (ArmorStandTool t : values()) {
            t.name = config.getString("tool." + t.config_id + ".name");
            ItemMeta im = t.item.getItemMeta();
            if (im != null) {
                im.setDisplayName(ChatColor.YELLOW + t.name);
                List<String> lore = config.getStringList("tool." + t.config_id + ".lore");
                if (t == GEN_CMD) {
                    String cmdBlk = !lore.isEmpty() ? lore.get(0) : "";
                    String logged = lore.size() > 1 ? lore.get(1) : "";
                    lore.clear();
                    if (!cmdBlk.isEmpty() && Config.saveToolCreatesCommandBlock) {
                        lore.add(cmdBlk);
                    }

                    if (!logged.isEmpty() && Config.logGeneratedSummonCommands) {
                        lore.add(logged);
                    }
                }

                im.setLore(lore);
                t.item.setItemMeta(im);
            }
        }
    }
}

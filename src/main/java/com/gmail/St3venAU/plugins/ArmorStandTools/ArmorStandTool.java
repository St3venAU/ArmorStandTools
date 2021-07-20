package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

import java.util.Arrays;
import java.util.UUID;

public enum ArmorStandTool {

    HEAD    ("gui_head",    Material.WITHER_SKELETON_SKULL,  7,  "astools.use",     false, false),
    BODY    ("gui_body",    Material.NETHERITE_CHESTPLATE,   16, "astools.use",     false, false),
    RARM    ("gui_rarm",    Material.REDSTONE_TORCH,         15, "astools.use",     true,  false),
    LARM    ("gui_larm",    Material.TORCH,                  17, "astools.use",     true,  false),
    RLEG    ("gui_rleg",    Material.BLAZE_ROD,              24, "astools.use",     true,  false),
    LLEG    ("gui_lleg",    Material.BONE,                   26, "astools.use",     true,  false),
    MOVE    ("gui_move",    Material.FEATHER,                8,  "astools.use",     false, false),

    ROTATE  ("gui_rotate", Material.ENDER_PEARL,             25, "astools.use",     false, false),
    MOVE_X  ("gui_moveX",  Material.ORANGE_CANDLE,           33, "astools.use",     false, false),
    MOVE_Y  ("gui_moveY",  Material.LIGHT_BLUE_CANDLE,       34, "astools.use",     false, false),
    MOVE_Z  ("gui_moveZ",  Material.LIME_CANDLE,             35, "astools.use",     false, false),


    NAME    ("gui_name",    Material.NAME_TAG,               3, "astools.use",      false, false),
    INVIS   ("gui_invis",   Material.GOLD_NUGGET,            4, "astools.use",      false, false),
    ARMS    ("gui_arms",    Material.ARROW,                  5, "astools.use",      false, false),
    BASE    ("gui_base",    Material.STONE_SLAB,             12, "astools.use",     false, false),
    SIZE    ("gui_size",    Material.EMERALD,                14, "astools.use",     false, false),
    GRAV    ("gui_grav",    Material.GHAST_TEAR,             13, "astools.use",     false, false),
    INVUL   ("gui_invul",   Material.GLISTERING_MELON_SLICE, 21, "astools.use",     false, false),
    SLOTS   ("gui_slots",   Material.IRON_HOE,               22, "astools.use",     false, false),
    GLOW    ("gui_glow",    Material.GLOWSTONE,              23, "astools.glow",    false, false),
    PHEAD   ("gui_pHead",   Material.PLAYER_HEAD,            30, "astools.head",    false, false),
    SAVE    ("gui_save",    Material.DIAMOND,                31, "astools.cmdblock",false, false),
    CLONE   ("gui_clone",   Material.ARMOR_STAND,            32, "astools.clone",   false, false),

    ADVANCED("gui_advanced",Material.NETHER_STAR,           27, "astools.use",     false,  false),

    RARM_X  ("gui_rArmX",   Material.REDSTONE_TORCH,        36, "astools.use",     false,  true),
    RARM_Y  ("gui_rArmY",   Material.REDSTONE_TORCH,        37, "astools.use",     false,  true),
    RARM_Z  ("gui_rArmZ",   Material.REDSTONE_TORCH,        38, "astools.use",     false,  true),
    LARM_X  ("gui_lArmX",   Material.TORCH,                 39, "astools.use",     false,  true),
    LARM_Y  ("gui_lArmY",   Material.TORCH,                 40, "astools.use",     false,  true),
    LARM_Z  ("gui_lArmZ",   Material.TORCH,                 41, "astools.use",     false,  true),
    HEAD_X  ("gui_headX",   Material.WITHER_SKELETON_SKULL, 42, "astools.use",     false,  true),
    HEAD_Y  ("gui_headY",   Material.WITHER_SKELETON_SKULL, 43, "astools.use",     false,  true),
    HEAD_Z  ("gui_headZ",   Material.WITHER_SKELETON_SKULL, 44, "astools.use",     false,  true),

    RLEG_X  ("gui_rLegX",   Material.BLAZE_ROD,             45, "astools.use",     false,  true),
    RLEG_Y  ("gui_rLegY",   Material.BLAZE_ROD,             46, "astools.use",     false,  true),
    RLEG_Z  ("gui_rLegZ",   Material.BLAZE_ROD,             47, "astools.use",     false,  true),
    LLEG_X  ("gui_lLegX",   Material.BONE,                  48, "astools.use",     false,  true),
    LLEG_Y  ("gui_lLegY",   Material.BONE,                  49, "astools.use",     false,  true),
    LLEG_Z  ("gui_lLegZ",   Material.BONE,                  50, "astools.use",     false,  true),
    BODY_X  ("gui_bodyX",   Material.NETHERITE_CHESTPLATE,  51, "astools.use",     false,  true),
    BODY_Y  ("gui_bodyY",   Material.NETHERITE_CHESTPLATE,  52, "astools.use",     false,  true),
    BODY_Z  ("gui_bodyZ",   Material.NETHERITE_CHESTPLATE,  53, "astools.use",     false,  true);



    private final ItemStack item;
    private final String config_id;
    private final int slot;
    private boolean enabled;
    private final String permission;
    private final boolean reverseSneaking;
    private final boolean advanced;
    private String name;

    ArmorStandTool(String config_id, Material m, int slot, String permission, boolean reverseSneaking, boolean advanced) {
        item = new ItemStack(m);
        this.config_id = config_id;
        this.slot = slot;
        this.enabled = true;
        this.permission = permission;
        this.reverseSneaking = reverseSneaking;
        this.advanced = advanced;
    }

    boolean isAdvanced() {
        return advanced;
    }

    void showTitle(Player p) {
        boolean sneaking = p.isSneaking();
        ChatColor offColor = ChatColor.WHITE;
        ChatColor onColor = ChatColor.YELLOW;
        ChatColor divColor = ChatColor.BLACK;
        String msg;
        if(advanced) {
            msg =   onColor + name +
                    divColor + " | " +
                    offColor + Config.click + ": " + Config.finish;
        } else {
            msg =   (sneaking ? offColor : onColor) +
                    Config.normal + ": X/" + (reverseSneaking ? "Z" : "Y") +
                    divColor + " | " +
                    (sneaking ? onColor : offColor) +
                    Config.crouch + ": X/" + (reverseSneaking ? "Y" : "Z") +
                    divColor + " | " +
                    offColor + Config.click + ": " + Config.finish;
        }
        p.sendTitle(" ", msg, 0, 600, 0);
    }

    private boolean is(ItemStack is) {
        return  is != null &&
                is.getType() == item.getType() &&
                is.getItemMeta() != null &&
                is.getItemMeta().hasDisplayName() &&
                item.getItemMeta() != null &&
                is.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName());
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
        if(as == null || as.isDead()) {
            UUID uuid = p.getUniqueId();
            AST.selectedArmorStand.remove(uuid);
            AST.activeTool.remove(uuid);
            return;
        }
        if(this == MOVE) {
            as.teleport(Utils.getLocationFacing(p.getLocation()));
            Utils.title(p, Config.carrying);
            return;
        }
        showTitle(p);
        EulerAngle eulerAngle = switch (this) {
            case HEAD, HEAD_X, HEAD_Y, HEAD_Z -> as.getHeadPose();
            case BODY, BODY_X, BODY_Y, BODY_Z -> as.getBodyPose();
            case LARM, LARM_X, LARM_Y, LARM_Z -> as.getLeftArmPose();
            case RARM, RARM_X, RARM_Y, RARM_Z -> as.getRightArmPose();
            case LLEG, LLEG_X, LLEG_Y, LLEG_Z -> as.getLeftLegPose();
            case RLEG, RLEG_X, RLEG_Y, RLEG_Z -> as.getRightLegPose();
            default -> null;
        };
        if(eulerAngle == null) return;
        if(advanced) {
            eulerAngle = switch (this) {
                case HEAD_X, BODY_X, LARM_X, RARM_X, LLEG_X, RLEG_X -> eulerAngle.setX(getPitch(p, 8));
                case HEAD_Y, BODY_Y, LARM_Y, RARM_Y, LLEG_Y, RLEG_Y -> eulerAngle.setY(getPitch(p, 8));
                case HEAD_Z, BODY_Z, LARM_Z, RARM_Z, LLEG_Z, RLEG_Z -> eulerAngle.setZ(getPitch(p, 8));
                default -> eulerAngle;
            };
        } else {
            eulerAngle = eulerAngle.setX(getPitch(p, 4));
            boolean sneaking = reverseSneaking != p.isSneaking();
            double yaw = getRelativeYaw(p, as);
            eulerAngle = sneaking ? eulerAngle.setZ(yaw) : eulerAngle.setY(yaw);
        }
        switch (this) {
            case HEAD, HEAD_X, HEAD_Y, HEAD_Z -> as.setHeadPose(eulerAngle);
            case BODY, BODY_X, BODY_Y, BODY_Z -> as.setBodyPose(eulerAngle);
            case LARM, LARM_X, LARM_Y, LARM_Z -> as.setLeftArmPose(eulerAngle);
            case RARM, RARM_X, RARM_Y, RARM_Z -> as.setRightArmPose(eulerAngle);
            case LLEG, LLEG_X, LLEG_Y, LLEG_Z -> as.setLeftLegPose(eulerAngle);
            case RLEG, RLEG_X, RLEG_Y, RLEG_Z -> as.setRightLegPose(eulerAngle);
        }
    }

    // Get pitch and format as 0 to 2pi radians
    // Actual pitch multiplied for increased sensitivity
    private double getPitch(Player p, int multiplier) {
        double pitch = p.getLocation().getPitch() * multiplier;
        while(pitch < 0) {
            pitch += 360;
        }
        while(pitch > 360) {
            pitch -= 360;
        }
        return pitch * Math.PI / 180.0;
    }

    // Get yaw relative to armor stand facing direction and format as 0 to 2pi radians
    // Actual yaw doubled for increased sensitivity, and reversed to create mirror effect
    private double getRelativeYaw(Player p, ArmorStand as) {
        double difference = p.getLocation().getYaw() - as.getLocation().getYaw();
        double yaw = 360.0 - (difference * 2);
        while(yaw < 0) {
            yaw += 360;
        }
        while(yaw > 360) {
            yaw -= 360;
        }
        return yaw * Math.PI / 180.0;
    }

    ItemStack updateLore(ArmorStand as) {
        switch (this) {
            case INVIS:
                return setLore(item, ChatColor.AQUA + Config.asVisible + ": " + (as.isVisible() ? (ChatColor.GREEN + Config.isTrue) : (ChatColor.RED + Config.isFalse)));
            case SIZE:
                return setLore(item, ChatColor.AQUA + Config.size + ": " + (as.isSmall() ? (ChatColor.BLUE + Config.small) : (ChatColor.GREEN + Config.normal)));
            case BASE:
                return setLore(item, ChatColor.AQUA + Config.basePlate + ": " + (as.hasBasePlate() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            case GRAV:
                return setLore(item, ChatColor.AQUA + Config.gravity + ": " + (as.hasGravity() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            case ARMS:
                return setLore(item, ChatColor.AQUA + Config.arms + ": " + (as.hasArms() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            case INVUL:
                return setLore(item, ChatColor.AQUA + Config.invul + ": " + (as.isInvulnerable() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            case SLOTS:
                return setLore(item, ChatColor.AQUA + Config.equip + ": " + (Utils.hasDisabledSlots(as) ? (ChatColor.GREEN + Config.locked) : (ChatColor.RED + Config.unLocked)));
            case NAME:
                return setLore(item, ChatColor.AQUA + Config.currently + ": " + (as.getCustomName() == null ? (ChatColor.BLUE + Config.none) : (ChatColor.GREEN + as.getCustomName())));
            case PHEAD:
                String name = plrHeadName(as);
                return setLore(item, ChatColor.AQUA + Config.currently + ": " + (name == null ? (ChatColor.BLUE + Config.none) : (ChatColor.GREEN + name)));
            case GLOW:
                return setLore(item, ChatColor.AQUA + Config.glow + ": " + (as.isGlowing() ? (ChatColor.GREEN + Config.isOn) : (ChatColor.RED + Config.isOff)));
            default:
                return item;
        }
    }

    private ItemStack setLore(ItemStack is, String... lore) {
        ItemMeta meta = is.getItemMeta();
        if(meta != null) {
            meta.setLore(Arrays.asList(lore));
            is.setItemMeta(meta);
        }
        return is;
    }

    private String plrHeadName(ArmorStand as) {
        EntityEquipment entityEquipment = as.getEquipment();
        if(entityEquipment == null || entityEquipment.getHelmet() == null || !(entityEquipment.getHelmet().getItemMeta() instanceof SkullMeta meta)) return null;
        if(meta.getOwningPlayer() == null) return null;
        return meta.getOwningPlayer().getName();
    }

    static ArmorStandTool get(ItemStack is) {
        if(is == null || is.getItemMeta() == null || !is.getItemMeta().hasDisplayName()) return null;
        for(ArmorStandTool t : values()) {
            if(t.is(is)) return t;
        }
        return null;
    }

    static void updateTools(FileConfiguration config) {
        for(ArmorStandTool t : values()) {
            t.name = config.getString("tool." + t.config_id + ".name");
            ItemMeta im = t.item.getItemMeta();
            if(im != null) {
                im.setDisplayName(ChatColor.YELLOW + t.name);
                im.setLore(config.getStringList("tool." + t.config_id + ".lore"));
                t.item.setItemMeta(im);
            }
        }
    }

}
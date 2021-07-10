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

    HEAD    ("gui_head",    Material.PLAYER_HEAD,            6,  "astools.use",     false),
    BODY    ("gui_body",    Material.NETHER_BRICKS,          15, "astools.use",     false),
    RARM    ("gui_rarm",    Material.REDSTONE_TORCH,         14, "astools.use",     true),
    LARM    ("gui_larm",    Material.TORCH,                  16, "astools.use",     true),
    RLEG    ("gui_rleg",    Material.BLAZE_ROD,              23, "astools.use",     true),
    LLEG    ("gui_lleg",    Material.BONE,                   25, "astools.use",     true),
    MOVE    ("gui_move",    Material.FEATHER,                24, "astools.use",     false),
    CLONE   ("gui_clone",   Material.GLOWSTONE_DUST,         33, "astools.clone",   false),
    SAVE    ("gui_save",    Material.DIAMOND,                43, "astools.cmdblock",false),
    SLOTS   ("gui_slots",   Material.IRON_HOE,               44, "astools.use",     false),
    NAME    ("gui_name",    Material.NAME_TAG,               45, "astools.use",     false),
    PHEAD   ("gui_pHead",   Material.PLAYER_HEAD,            46, "astools.head",    false),
    INVIS   ("gui_invis",   Material.GOLD_NUGGET,            47, "astools.use",     false),
    ARMS    ("gui_arms",    Material.ARROW,                  48, "astools.use",     false),
    BASE    ("gui_base",    Material.STONE_SLAB,             49, "astools.use",     false),
    SIZE    ("gui_size",    Material.EMERALD,                50, "astools.use",     false),
    GRAV    ("gui_grav",    Material.GHAST_TEAR,             51, "astools.use",     false),
    INVUL   ("gui_invul",   Material.GLISTERING_MELON_SLICE, 52, "astools.use",     false),
    GLOW    ("gui_glow",    Material.GLOWSTONE,              53, "astools.glow",    false);

    private final ItemStack item;
    private final String config_id;
    private final int slot;
    private boolean enabled;
    private final String permission;
    private final boolean reverseSneaking;
    private String name;

    ArmorStandTool(String config_id, Material m, int slot, String permission, boolean reverseSneaking) {
        item = new ItemStack(m);
        this.config_id = config_id;
        this.slot = slot;
        this.enabled = true;
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
        EulerAngle eulerAngle;
        switch (this) {
            case MOVE:
                as.teleport(Utils.getLocationFacing(p.getLocation()));
                Utils.title(p, Config.carrying);
                return;
            case HEAD:
                eulerAngle = as.getHeadPose();
                break;
            case BODY:
                eulerAngle = as.getBodyPose();
                break;
            case LARM:
                eulerAngle = as.getLeftArmPose();
                break;
            case RARM:
                eulerAngle = as.getRightArmPose();
                break;
            case LLEG:
                eulerAngle = as.getLeftLegPose();
                break;
            case RLEG:
                eulerAngle = as.getRightLegPose();
                break;
            default:
                return;
        }
        eulerAngle = eulerAngle.setX(getPitch(p));
        boolean sneaking = reverseSneaking != p.isSneaking();
        double yaw = getRelativeYaw(p, as);
        eulerAngle = sneaking ? eulerAngle.setZ(yaw) : eulerAngle.setY(yaw);
        showTitle(p);
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
    // Actual pitch quadrupled for increased sensitivity
    private double getPitch(Player p) {
        double pitch = p.getLocation().getPitch() * 4;
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
        if(entityEquipment == null || entityEquipment.getHelmet() == null || !(entityEquipment.getHelmet().getItemMeta() instanceof SkullMeta)) return null;
        SkullMeta meta = (SkullMeta) entityEquipment.getHelmet().getItemMeta();
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
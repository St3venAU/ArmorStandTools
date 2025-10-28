package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.UUID;

class ArmorStandGUI implements Listener {

    private static final int INV_SLOT_HELMET    = 1;
    private static final int INV_SLOT_CHEST     = 10;
    private static final int INV_SLOT_LEGS      = 19;
    private static final int INV_SLOT_BOOTS     = 28;
    private static final int INV_SLOT_MAIN_HAND = 9;
    private static final int INV_SLOT_OFF_HAND  = 11;

    private static final HashSet<Integer> inUse = new HashSet<>();
    private static final HashSet<Integer> invSlots = new HashSet<>();
    private static ItemStack filler;

    private static final HashSet<Material> helmetTypes = new HashSet<>();
    private static final HashSet<Material> chestTypes = new HashSet<>();
    private static final HashSet<Material> leggingTypes = new HashSet<>();
    private static final HashSet<Material> bootTypes = new HashSet<>();

    private Inventory i;
    private ArmorStand as;

    ArmorStandGUI(ArmorStand as, Player p) {
        if (isInUse(as)) {
            p.sendMessage(ChatColor.RED + Config.guiInUse);
            return;
        }

        AST.plugin.getServer().getPluginManager().registerEvents(this, AST.plugin);
        this.as = as;

        String name = as.getCustomName();
        if (name == null) {
            name = Config.armorStand;
        } else if (name.length() > 32) {
            name = name.substring(0, 32);
        }

        i = Bukkit.createInventory(null, 54, name);
        for (int slot = 0; slot < i.getSize(); slot++) {
            i.setItem(slot, filler);
        }

        for (ArmorStandTool tool : ArmorStandTool.values()) {
            if (tool.isForGui() && tool.isEnabled()) {
                i.setItem(tool.getSlot(), tool.updateLore(as));
            }
        }

        EntityEquipment entityEquipment = as.getEquipment();
        i.setItem(INV_SLOT_MAIN_HAND, entityEquipment.getItemInMainHand());
        i.setItem(INV_SLOT_OFF_HAND,  entityEquipment.getItemInOffHand());
        i.setItem(INV_SLOT_HELMET,    entityEquipment.getHelmet());
        i.setItem(INV_SLOT_CHEST,     entityEquipment.getChestplate());
        i.setItem(INV_SLOT_LEGS,      entityEquipment.getLeggings());
        i.setItem(INV_SLOT_BOOTS,     entityEquipment.getBoots());
        inUse.add(as.getEntityId());
        AST.scheduler().runAtEntity(p, task -> p.openInventory(i));
    }

    static void init() {
        filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta im = filler.getItemMeta();
        if (im != null) {
            im.setDisplayName(" ");
            filler.setItemMeta(im);
        }

        invSlots.add(INV_SLOT_HELMET);
        invSlots.add(INV_SLOT_CHEST);
        invSlots.add(INV_SLOT_LEGS);
        invSlots.add(INV_SLOT_BOOTS);
        invSlots.add(INV_SLOT_MAIN_HAND);
        invSlots.add(INV_SLOT_OFF_HAND);

        String name;
        for (Material m : Material.values()) {
            name = m.name();
            if (name.endsWith("_HELMET")) {
                helmetTypes.add(m);
            }

            if (name.endsWith("_CHESTPLATE")) {
                chestTypes.add(m);
            }

            if (name.endsWith("_LEGGINGS")) {
                leggingTypes.add(m);
            }

            if (name.endsWith("_BOOTS")) {
                bootTypes.add(m);
            }
        }

        helmetTypes.add(Material.PLAYER_HEAD);
        helmetTypes.add(Material.CREEPER_HEAD);
        helmetTypes.add(Material.DRAGON_HEAD);
        helmetTypes.add(Material.ZOMBIE_HEAD);
        helmetTypes.add(Material.SKELETON_SKULL);
        helmetTypes.add(Material.WITHER_SKELETON_SKULL);
        helmetTypes.add(Material.JACK_O_LANTERN);
    }

    static boolean isInUse(ArmorStand as) {
        return inUse.contains(as.getEntityId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(i)) {
            return;
        }

        HandlerList.unregisterAll(this);
        inUse.remove(as.getEntityId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(i) || !(event.getWhoClicked() instanceof Player p)) {
            return;
        }

        if (event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.NUMBER_KEY) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getRawSlot();
        Location l = as.getLocation();
        if (invSlots.contains(slot)) {
            if (AST.checkBlockPermission(p, l.getBlock())) {
                updateArmorStandInventory();
            } else {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED + Config.wgNoPerm);
            }

            return;
        }

        if (event.getClick() == ClickType.SHIFT_LEFT) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (slot > i.getSize() && item != null && !ArmorStandTool.isTool(item) && event.getClickedInventory() != null) {
                if (AST.checkBlockPermission(p, l.getBlock())) {
                    Material m = item.getType();
                    int newSlot = -1;
                    if (helmetTypes.contains(m) && slotIsEmpty(INV_SLOT_HELMET)) {
                        newSlot = INV_SLOT_HELMET;
                    } else if (chestTypes.contains(m) && slotIsEmpty(INV_SLOT_CHEST)) {
                        newSlot = INV_SLOT_CHEST;
                    } else if (leggingTypes.contains(m) && slotIsEmpty(INV_SLOT_LEGS)) {
                        newSlot = INV_SLOT_LEGS;
                    } else if (bootTypes.contains(m) && slotIsEmpty(INV_SLOT_BOOTS)) {
                        newSlot = INV_SLOT_BOOTS;
                    } else if (slotIsEmpty(INV_SLOT_MAIN_HAND)) {
                        newSlot = INV_SLOT_MAIN_HAND;
                    } else if (slotIsEmpty(INV_SLOT_OFF_HAND)) {
                        newSlot = INV_SLOT_OFF_HAND;
                    }

                    if (newSlot != -1) {
                        i.setItem(newSlot, item);
                        event.getClickedInventory().setItem(event.getSlot(), null);
                        updateArmorStandInventory();
                    }
                } else {
                    p.sendMessage(ChatColor.RED + Config.wgNoPerm);
                }
            }

            return;
        }

        if (slot > i.getSize()) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        ArmorStandTool t = ArmorStandTool.get(event.getCurrentItem());
        if (t == null) {
            return;
        }

        if (!AST.playerHasPermission(p, l.getBlock(), t)) {
            p.sendMessage(ChatColor.RED + Config.generalNoPerm);
            return;
        }

        switch (t) {
            case HEAD, BODY, LARM, RARM, LLEG, RLEG -> {
                UUID uuid = p.getUniqueId();
                AST.activeTool.put(uuid, t);
                AST.selectedArmorStand.put(uuid, as);
                AST.scheduler().runAtEntity(p, task -> p.closeInventory());
                t.showTitle(p);
            }
            case INVIS -> {
                as.setVisible(!as.isVisible());
                Utils.title(p, Config.asVisible + ": " + (as.isVisible() ? Config.isTrue : Config.isFalse));
            }
            case CLONE -> {
                AST.scheduler().runAtEntity(p, task -> p.closeInventory());
                ArmorStand clone = Utils.cloneArmorStand(as);
                AST.pickUpArmorStand(clone, p, true);
                Utils.title(p, Config.carrying);
            }
            case GEN_CMD -> {
                String command = Utils.createSummonCommand(as);
                p.sendMessage(command);
                if (Config.saveToolCreatesCommandBlock) {
                    if (Config.requireCreative && p.getGameMode() != GameMode.CREATIVE) {
                        p.sendMessage(ChatColor.RED + Config.creativeRequired);
                    } else {
                        Utils.generateCmdBlock(p.getLocation(), command);
                        Utils.title(p, Config.cbCreated);
                    }
                }

                if (Config.logGeneratedSummonCommands) {
                    Config.logSummonCommand(p.getName(), command);
                }
            }
            case SIZE -> {
                as.setSmall(!as.isSmall());
                Utils.title(p, Config.size + ": " + (as.isSmall() ? Config.small : Config.normal));
            }
            case BASE -> {
                as.setBasePlate(!as.hasBasePlate());
                Utils.title(p, Config.basePlate + ": " + (as.hasBasePlate() ? Config.isOn : Config.isOff));
            }
            case GRAV -> {
                as.setGravity(!as.hasGravity());
                Utils.title(p, Config.gravity + ": " + (as.hasGravity() ? Config.isOn : Config.isOff));
            }
            case ARMS -> {
                as.setArms(!as.hasArms());
                Utils.title(p, Config.arms + ": " + (as.hasArms() ? Config.isOn : Config.isOff));
            }
            case NAME -> {
                AST.scheduler().runAtEntity(p, task -> p.closeInventory());
                AST.setName(p, as);
            }
            case PHEAD -> {
                AST.scheduler().runAtEntity(p, task -> p.closeInventory());
                AST.setPlayerSkull(p, as);
            }
            case INVUL -> {
                boolean inv = !as.isInvulnerable();
                as.setInvulnerable(inv);
                Utils.title(p, Config.invul + ": " + (inv ? Config.isOn : Config.isOff));
            }
            case SLOTS -> Utils.title(p, Config.equip + ": " + (Utils.toggleSlotsDisabled(as) ? Config.locked : Config.unLocked));
            case MOVE -> {
                AST.scheduler().runAtEntity(p, task -> p.closeInventory());
                as.removeMetadata("clone", AST.plugin);
                AST.pickUpArmorStand(as, p, false);
                Utils.title(p, Config.carrying);
            }
            case GLOW -> {
                boolean glowing = !as.isGlowing();
                as.setGlowing(glowing);
                Utils.title(p, Config.glow + ": " + (glowing ? Config.isOn : Config.isOff));
            }
            case ITEM -> {
                World w = p.getWorld();
                boolean commandFeedback = Boolean.TRUE.equals(w.getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK));
                if (commandFeedback) w.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
                AST.scheduler().runNextTick(task -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.createGiveCommand(as, p)));
                if (commandFeedback) w.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
                AST.scheduler().runAtEntity(p, task -> p.closeInventory());
                if (p.getGameMode() != GameMode.CREATIVE) {
                    AST.scheduler().runAtEntity(as, task -> as.remove());
                }
            }
            default -> {
                return;
            }
        }

        i.setItem(t.getSlot(), t.updateLore(as));
    }

    private boolean slotIsEmpty(int slot) {
        ItemStack item = i.getItem(slot);
        return item == null || item.getType() == Material.AIR || item.getAmount() == 0;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(i) || !(event.getWhoClicked() instanceof Player p)) {
            return;
        }

        boolean invModified = false;
        for (int slot : event.getRawSlots()) {
            if (slot < i.getSize()) {
                if (invSlots.contains(slot)) {
                    invModified = true;
                } else {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (invModified) {
            if (AST.checkBlockPermission(p, as.getLocation().getBlock())) {
                updateArmorStandInventory();
            } else {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED + Config.wgNoPerm);
            }
        }
    }

    private void updateArmorStandInventory() {
        AST.scheduler().runAtEntity(as, task -> {
            if (as == null || i == null) {
                return;
            }

            EntityEquipment equipment = as.getEquipment();
            equipment.setItemInMainHand(i.getItem(INV_SLOT_MAIN_HAND));
            equipment.setItemInOffHand(i.getItem(INV_SLOT_OFF_HAND));
            equipment.setHelmet(i.getItem(INV_SLOT_HELMET));
            equipment.setChestplate(i.getItem(INV_SLOT_CHEST));
            equipment.setLeggings(i.getItem(INV_SLOT_LEGS));
            equipment.setBoots(i.getItem(INV_SLOT_BOOTS));
        });
    }
}

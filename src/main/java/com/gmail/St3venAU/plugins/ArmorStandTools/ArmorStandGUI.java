package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

class ArmorStandGUI implements Listener {

    private static final int INV_SLOT_HELMET = 1;
    private static final int INV_SLOT_CHEST  = 10;
    private static final int INV_SLOT_PANTS  = 19;
    private static final int INV_SLOT_BOOTS  = 28;
    private static final int INV_SLOT_R_HAND = 9;
    private static final int INV_SLOT_L_HAND = 11;

    private static final HashSet<Integer> inUse = new HashSet<>();
    private static final HashSet<Integer> invSlots = new HashSet<>();
    private static ItemStack filler;

    private Inventory i;
    private ArmorStand as;

    ArmorStandGUI(ArmorStand as, Player p) {
        if(isInUse(as)) {
            p.sendMessage(ChatColor.RED + Config.guiInUse);
            return;
        }
        if(filler == null) {
            filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
            ItemMeta im = filler.getItemMeta();
            if(im != null) {
                im.setDisplayName(" ");
                filler.setItemMeta(im);
            }
            invSlots.add(INV_SLOT_HELMET);
            invSlots.add(INV_SLOT_CHEST);
            invSlots.add(INV_SLOT_PANTS);
            invSlots.add(INV_SLOT_BOOTS);
            invSlots.add(INV_SLOT_R_HAND);
            invSlots.add(INV_SLOT_L_HAND);
        }
        AST.plugin.getServer().getPluginManager().registerEvents(this, AST.plugin);
        this.as = as;
        String name = as.getCustomName();
        if(name == null) {
            name = Config.armorStand;
        } else if(name.length() > 32) {
            name = name.substring(0, 32);
        }
        boolean showAdvancedTools = AST.showAdvancedTools.contains(p.getUniqueId());
        i = Bukkit.createInventory(null, showAdvancedTools ? 54 : 36, name);
        for(int slot = 0; slot < i.getSize(); slot++) {
            i.setItem(slot, filler);
        }
        for(ArmorStandTool tool : ArmorStandTool.values()) {
            if(tool.isEnabled() && (!tool.isAdvanced() || showAdvancedTools)) {
                i.setItem(tool.getSlot(), tool.updateLore(as));
            }
        }
        EntityEquipment entityEquipment = as.getEquipment();
        if(entityEquipment != null) {
            i.setItem(INV_SLOT_R_HAND, entityEquipment.getItemInMainHand());
            i.setItem(INV_SLOT_L_HAND, entityEquipment.getItemInOffHand());
            i.setItem(INV_SLOT_HELMET, entityEquipment.getHelmet());
            i.setItem(INV_SLOT_CHEST, entityEquipment.getChestplate());
            i.setItem(INV_SLOT_PANTS, entityEquipment.getLeggings());
            i.setItem(INV_SLOT_BOOTS, entityEquipment.getBoots());
        }
        inUse.add(as.getEntityId());
        p.openInventory(i);
    }

    static boolean isInUse(ArmorStand as) {
        return inUse.contains(as.getEntityId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!event.getInventory().equals(i)) return;
        HandlerList.unregisterAll(this);
        inUse.remove(as.getEntityId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!event.getInventory().equals(i) || !(event.getWhoClicked() instanceof Player p)) return;
        if(event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.NUMBER_KEY) {
            event.setCancelled(true);
            return;
        }
        boolean rightClick = event.getClick() == ClickType.RIGHT;
        int slot = event.getRawSlot();
        if(slot > i.getSize()) return;
        Location l = as.getLocation();
        if(invSlots.contains(slot)) {
            if(AST.checkBlockPermission(p, l.getBlock())) {
                updateArmorStandInventory();
            } else {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED + Config.wgNoPerm);
            }
            return;
        }
        event.setCancelled(true);
        if(!(event.getWhoClicked() instanceof Player)) return;
        ArmorStandTool t = ArmorStandTool.get(event.getCurrentItem());
        if(t == null) return;
        if (!AST.playerHasPermission(p, l.getBlock(), t)) {
            p.sendMessage(ChatColor.RED + Config.generalNoPerm);
            return;
        }
        UUID uuid = p.getUniqueId();
        if(t.isAdvanced()) {
            AST.activeTool.put(uuid, t);
            AST.selectedArmorStand.put(uuid, as);
            p.closeInventory();
            t.showTitle(p);
        }
        switch (t) {
            case HEAD:
            case BODY:
            case LARM:
            case RARM:
            case LLEG:
            case RLEG:
                AST.activeTool.put(uuid, t);
                AST.selectedArmorStand.put(uuid, as);
                p.closeInventory();
                t.showTitle(p);
                break;
            case INVIS:
                as.setVisible(!as.isVisible());
                Utils.title(p, Config.asVisible + ": " + (as.isVisible() ? Config.isTrue : Config.isFalse));
                break;
            case CLONE:
                p.closeInventory();
                AST.pickUpArmorStand(Utils.cloneArmorStand(as), p);
                Utils.title(p, Config.carrying);
                break;
            case SAVE:
                if(Config.requireCreative && p.getGameMode() != GameMode.CREATIVE) {
                    p.sendMessage(ChatColor.RED + Config.creativeRequired);
                } else {
                    Utils.generateCmdBlock(p.getLocation(), as);
                    Utils.title(p, Config.cbCreated);
                }
                break;
            case SIZE:
                as.setSmall(!as.isSmall());
                Utils.title(p, Config.size + ": " + (as.isSmall() ? Config.small : Config.normal));
                break;
            case BASE:
                as.setBasePlate(!as.hasBasePlate());
                Utils.title(p, Config.basePlate + ": " + (as.hasBasePlate() ? Config.isOn : Config.isOff));
                break;
            case GRAV:
                as.setGravity(!as.hasGravity());
                Utils.title(p, Config.gravity + ": " + (as.hasGravity() ? Config.isOn : Config.isOff));
                break;
            case ARMS:
                as.setArms(!as.hasArms());
                Utils.title(p, Config.arms + ": " + (as.hasArms() ? Config.isOn : Config.isOff));
                break;
            case NAME:
                p.closeInventory();
                AST.setName(p, as);
                break;
            case PHEAD:
                p.closeInventory();
                AST.setPlayerSkull(p, as);
                break;
            case INVUL:
                boolean inv = !as.isInvulnerable();
                as.setInvulnerable(inv);
                Utils.title(p, Config.invul + ": " + (inv ? Config.isOn : Config.isOff));
                break;
            case SLOTS:
                Utils.title(p, Config.equip + ": " + (Utils.toggleSlotsDisabled(as) ? Config.locked : Config.unLocked));
                break;
            case MOVE:
                p.closeInventory();
                as.removeMetadata("clone", AST.plugin);
                AST.pickUpArmorStand(as, p);
                Utils.title(p, Config.carrying);
                break;
            case MOVE_X:
            case MOVE_Y:
            case MOVE_Z:
                double dist = rightClick ? -0.1 : 0.1;
                l.add(t == ArmorStandTool.MOVE_X ? dist : 0, t == ArmorStandTool.MOVE_Y ? dist : 0,t == ArmorStandTool.MOVE_Z ? dist : 0);
                if (!AST.playerHasPermission(p, l.getBlock(), null)) {
                    p.closeInventory();
                    p.sendMessage(ChatColor.RED + Config.wgNoPerm);
                    break;
                }
                as.setGravity(false);
                as.teleport(l);
                break;
            case ROTATE:
                float yaw = l.getYaw() + (rightClick ? -5 : 5);
                l.setYaw(yaw);
                as.teleport(l);
                break;
            case GLOW:
                boolean glowing = !as.isGlowing();
                as.setGlowing(glowing);
                Utils.title(p, Config.glow + ": " + (glowing ? Config.isOn : Config.isOff));
                break;
            case ADVANCED:
                if(AST.showAdvancedTools.contains(uuid)) {
                    AST.showAdvancedTools.remove(uuid);
                } else {
                    AST.showAdvancedTools.add(uuid);
                }
                p.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(ArmorStandGUI.isInUse(as)) {
                            Utils.title(p, Config.guiInUse);
                            return;
                        }
                        new ArmorStandGUI(as, p);
                    }
                }.runTaskLater(AST.plugin, 1L);
                break;
            default:
                return;
        }
        i.setItem(t.getSlot(), t.updateLore(as));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if(!event.getInventory().equals(i) || !(event.getWhoClicked() instanceof Player p)) return;
        boolean invModified = false;
        for(int slot : event.getRawSlots()) {
            if(slot < i.getSize()) {
                if(invSlots.contains(slot)) {
                    invModified = true;
                } else {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if(invModified) {
            if(AST.checkBlockPermission(p, as.getLocation().getBlock())) {
                updateArmorStandInventory();
            } else {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED + Config.wgNoPerm);
            }
        }
    }

    private void updateArmorStandInventory() {
        new BukkitRunnable() {
            @Override
            public void run() {
                EntityEquipment equipment = as.getEquipment();
                if(as == null || i == null || equipment == null) return;
                equipment.setItemInMainHand(i.getItem(INV_SLOT_R_HAND));
                equipment.setItemInOffHand(i.getItem(INV_SLOT_L_HAND));
                equipment.setHelmet(i.getItem(INV_SLOT_HELMET));
                equipment.setChestplate(i.getItem(INV_SLOT_CHEST));
                equipment.setLeggings(i.getItem(INV_SLOT_PANTS));
                equipment.setBoots(i.getItem(INV_SLOT_BOOTS));
            }
        }.runTaskLater(AST.plugin, 1L);
    }

}
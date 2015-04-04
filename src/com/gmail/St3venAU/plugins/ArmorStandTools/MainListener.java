package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class MainListener implements Listener {

    private final Pattern MC_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
    private final Main plugin;

    MainListener(Main main) {
        this.plugin = main;
    }

    @SuppressWarnings("ConstantConditions")
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            Player p = event.getPlayer();
            if(plugin.carryingArmorStand.containsKey(p.getUniqueId())) {
                if (plugin.playerHasPermission(p, plugin.carryingArmorStand.get(p.getUniqueId()).getLocation().getBlock(), null)) {
                    plugin.carryingArmorStand.remove(p.getUniqueId());
                    Utils.actionBarMsg(p, Config.asDropped);
                    event.setCancelled(true);
                    return;
                } else {
                    p.sendMessage(ChatColor.RED + Config.wgNoPerm);
                }
            }
            ArmorStandTool tool = ArmorStandTool.get(p.getItemInHand());
            if(tool == null) return;
            ArmorStand as = (ArmorStand) event.getRightClicked();
            if (!plugin.playerHasPermission(p, event.getRightClicked().getLocation().getBlock(), tool)) {
                p.sendMessage(ChatColor.RED + Config.generalNoPerm);
                event.setCancelled(true);
                return;
            }
            double num = event.getClickedPosition().getY() - 0.05;
            if (num < 0) {
                num = 0;
            } else if (num > 2) {
                num = 2;
            }
            num = 2.0 - num;
            double angle = num * Math.PI;
            boolean cancel = true;

            switch(tool) {
                case HEADX:
                    as.setHeadPose(as.getHeadPose().setX(angle));
                    break;
                case HEADY:
                    as.setHeadPose(as.getHeadPose().setY(angle));
                    break;
                case HEADZ:
                    as.setHeadPose(as.getHeadPose().setZ(angle));
                    break;
                case LARMX:
                    as.setLeftArmPose(as.getLeftArmPose().setX(angle));
                    break;
                case LARMY:
                    as.setLeftArmPose(as.getLeftArmPose().setY(angle));
                    break;
                case LARMZ:
                    as.setLeftArmPose(as.getLeftArmPose().setZ(angle));
                    break;
                case RARMX:
                    as.setRightArmPose(as.getRightArmPose().setX(angle));
                    break;
                case RARMY:
                    as.setRightArmPose(as.getRightArmPose().setY(angle));
                    break;
                case RARMZ:
                    as.setRightArmPose(as.getRightArmPose().setZ(angle));
                    break;
                case LLEGX:
                    as.setLeftLegPose(as.getLeftLegPose().setX(angle));
                    break;
                case LLEGY:
                    as.setLeftLegPose(as.getLeftLegPose().setY(angle));
                    break;
                case LLEGZ:
                    as.setLeftLegPose(as.getLeftLegPose().setZ(angle));
                    break;
                case RLEGX:
                    as.setRightLegPose(as.getRightLegPose().setX(angle));
                    break;
                case RLEGY:
                    as.setRightLegPose(as.getRightLegPose().setY(angle));
                    break;
                case RLEGZ:
                    as.setRightLegPose(as.getRightLegPose().setZ(angle));
                    break;
                case BODYX:
                    as.setBodyPose(as.getBodyPose().setX(angle));
                    break;
                case BODYY:
                    as.setBodyPose(as.getBodyPose().setY(angle));
                    break;
                case BODYZ:
                    as.setBodyPose(as.getBodyPose().setZ(angle));
                    break;
                case MOVEX:
                    as.teleport(as.getLocation().add(0.05 * (p.isSneaking() ? -1 : 1), 0.0, 0.0));
                    break;
                case MOVEY:
                    as.teleport(as.getLocation().add(0.0, 0.05 * (p.isSneaking() ? -1 : 1), 0.0));
                    break;
                case MOVEZ:
                    as.teleport(as.getLocation().add(0.0, 0.0, 0.05 * (p.isSneaking() ? -1 : 1)));
                    break;
                case ROTAT:
                    Location l = as.getLocation();
                    l.setYaw(((float) num) * 180F);
                    as.teleport(l);
                    break;
                case GUI:
                    new ArmorStandGUI(plugin, as, p);
                    break;
                default:
                    cancel = tool == ArmorStandTool.SUMMON || tool == ArmorStandTool.SAVE  || event.isCancelled();
            }
            event.setCancelled(cancel);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame && ArmorStandTool.isTool(event.getPlayer().getItemInHand())) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (ArmorStandTool.isTool(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if(plugin.carryingArmorStand.containsKey(p.getUniqueId())) {
            ArmorStand as = plugin.carryingArmorStand.get(p.getUniqueId());
            if (as == null || as.isDead()) {
                plugin.carryingArmorStand.remove(p.getUniqueId());
                Utils.actionBarMsg(p, Config.asDropped);
                return;
            }
            as.teleport(Utils.getLocationFacingPlayer(p));
            Utils.actionBarMsg(p, ChatColor.GREEN + Config.carrying);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player p = event.getEntity();
        if(p.getWorld().getGameRuleValue("keepInventory").equalsIgnoreCase("true")) return;
        List<ItemStack> drops = event.getDrops();
        for(ArmorStandTool t : ArmorStandTool.values()) {
            drops.remove(t.getItem());
        }
        if(plugin.savedInventories.containsKey(p.getUniqueId())) {
            drops.addAll(Arrays.asList(plugin.savedInventories.get(p.getUniqueId())));
            plugin.savedInventories.remove(p.getUniqueId());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) return;
        final Player p = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if(event.getInventory().getHolder() != p && ArmorStandTool.isTool(item)) {
            event.setCancelled(true);
            p.updateInventory();
            return;
        }
        if(event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            if(Utils.hasItems(p)) {
                event.setCancelled(true);
                p.updateInventory();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) return;
        final Player p = (Player) event.getWhoClicked();
        if (event.getInventory().getHolder() != p && Utils.containsItems(event.getNewItems().values())) {
            event.setCancelled(true);
            p.updateInventory();
        }
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (ArmorStandTool.isTool(event.getItemDrop().getItemStack())) {
            event.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(plugin.carryingArmorStand.containsKey(uuid)) {
            plugin.returnArmorStand(plugin.carryingArmorStand.get(uuid));
            plugin.carryingArmorStand.remove(uuid);
        }
        if(plugin.savedInventories.containsKey(uuid)) {
            event.getPlayer().getInventory().setContents(plugin.savedInventories.get(uuid));
            plugin.savedInventories.remove(uuid);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if(plugin.carryingArmorStand.containsKey(p.getUniqueId())) {
            if (plugin.playerHasPermission(p, plugin.carryingArmorStand.get(p.getUniqueId()).getLocation().getBlock(), null)) {
                plugin.carryingArmorStand.remove(p.getUniqueId());
                Utils.actionBarMsg(p, Config.asDropped);
                p.setMetadata("lastDrop", new FixedMetadataValue(plugin, System.currentTimeMillis()));
                event.setCancelled(true);
            } else {
                p.sendMessage(ChatColor.RED + Config.wgNoPerm);
            }
            return;
        }
        ArmorStandTool tool = ArmorStandTool.get(event.getItem());
        if(tool == null) return;
        event.setCancelled(true);
        Action action = event.getAction();
        if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            Utils.cycleInventory(p);
        } else if((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) && tool == ArmorStandTool.SUMMON) {
            if (!plugin.playerHasPermission(p, event.getClickedBlock(), tool)) {
                p.sendMessage(ChatColor.RED + Config.generalNoPerm);
                return;
            }
            Location l = Utils.getLocationFacingPlayer(p);
            plugin.pickUpArmorStand(spawnArmorStand(l), p, true);
            Utils.actionBarMsg(p, ChatColor.GREEN + Config.carrying);
            p.updateInventory();
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof ArmorStand && event.getDamager() instanceof Player && ArmorStandTool.isTool(((Player) event.getDamager()).getItemInHand())) {
            event.setCancelled(true);
            if(noCooldown(event.getDamager())) {
                Utils.cycleInventory((Player) event.getDamager());
            }
        }
    }

    private boolean noCooldown(Entity e) {
        for(MetadataValue meta : e.getMetadata("lastDrop")) {
            if(meta.getOwningPlugin() == plugin) {
                return System.currentTimeMillis() - meta.asFloat() > 100;
            }
        }
        return true;
    }

    private ArmorStand spawnArmorStand(Location l) {
        ArmorStand as = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        as.setHelmet(Config.helmet);
        as.setChestplate(Config.chest);
        as.setLeggings(Config.pants);
        as.setBoots(Config.boots);
        as.setItemInHand(Config.itemInHand);
        as.setVisible(Config.isVisible);
        as.setSmall(Config.isSmall);
        as.setArms(Config.hasArms);
        as.setBasePlate(Config.hasBasePlate);
        as.setGravity(Config.hasGravity);
        if(Config.defaultName.length() > 0) {
            as.setCustomName(Config.defaultName);
            as.setCustomNameVisible(true);
        }
        NBT.setSlotsDisabled(as, Config.equipmentLock);
        NBT.setInvulnerable(as, Config.invulnerable);
        return as;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if((b.getType() == Material.SKULL && b.hasMetadata("protected")) || (b.getType() == Material.SIGN_POST && b.hasMetadata("armorStand"))) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        if(event.getBlock().hasMetadata("armorStand")) {
            final Block b = event.getBlock();
            final ArmorStand as = getArmorStand(b);
            boolean delete = true;
            if (as != null) {
                String input = "";
                for (String line : event.getLines()) {
                    if (line != null && line.length() > 0) {
                        input += ChatColor.translateAlternateColorCodes('&', line);
                    }
                }
                if(b.hasMetadata("setName")) {
                    if (input.length() > 0) {
                        as.setCustomName(input);
                        as.setCustomNameVisible(true);
                    } else {
                        as.setCustomName("");
                        as.setCustomNameVisible(false);
                        as.setCustomNameVisible(false);
                    }
                } else if(b.hasMetadata("setSkull")) {
                    if(MC_USERNAME_PATTERN.matcher(input).matches()) {
                        final String name = input;
                        b.setMetadata("protected", new FixedMetadataValue(plugin, true));
                        b.setType(Material.SKULL);
                        final Skull s = (Skull) b.getState();
                        s.setSkullType(SkullType.PLAYER);
                        delete = false;
                        event.getPlayer().sendMessage(ChatColor.GOLD + Config.pleaseWait);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                final boolean ok = Utils.loadProfile(name);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (ok) {
                                            s.setOwner(name);
                                            s.update();
                                            new BukkitRunnable() {
                                                int n = 0;
                                                ItemStack skull;
                                                @Override
                                                public void run() {
                                                    if(++n > 20) {
                                                        this.cancel();
                                                        event.getPlayer().sendMessage(ChatColor.RED + Config.headFailed);
                                                        b.setType(Material.AIR);
                                                        b.setData((byte) 0);
                                                        b.removeMetadata("protected", plugin);
                                                        return;
                                                    }
                                                    skull = b.getDrops().iterator().next();
                                                    if(skull.getType() == Material.SKULL_ITEM && skull.getData().getData() == (byte) 3) {
                                                        SkullMeta meta = (SkullMeta) skull.getItemMeta();
                                                        if(meta.hasOwner() && meta.getOwner().equalsIgnoreCase(name)) {
                                                            as.setHelmet(skull);
                                                            event.getPlayer().sendMessage(ChatColor.GREEN + Config.appliedHead + ChatColor.GOLD + " " + name);
                                                            b.setType(Material.AIR);
                                                            b.setData((byte) 0);
                                                            b.removeMetadata("protected", plugin);
                                                            this.cancel();
                                                        }
                                                    }
                                                }
                                            }.runTaskTimer(plugin, 10L, 10L);
                                        } else {
                                            event.getPlayer().sendMessage(ChatColor.RED + Config.noHead + ChatColor.GOLD + " " + name);
                                            b.setType(Material.AIR);
                                            b.setData((byte) 0);
                                            b.removeMetadata("protected", plugin);
                                        }
                                    }
                                }.runTask(plugin);
                            }
                        }.runTaskAsynchronously(plugin);
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + input + " " + Config.invalidName);
                    }
                }
            }
            event.setCancelled(true);
            b.removeMetadata("armorStand", plugin);
            b.removeMetadata("setName", plugin);
            b.removeMetadata("setSkull", plugin);
            if(delete) {
                b.setType(Material.AIR);
                b.setData((byte) 0);
            }
        }
    }
    
    private ArmorStand getArmorStand(Block b) {
        UUID uuid = null;
        for (MetadataValue value : b.getMetadata("armorStand")) {
            if (value.getOwningPlugin() == plugin) {
                uuid = (UUID) value.value();
            }
        }
        b.removeMetadata("armorStand", plugin);
        if (uuid != null) {
            for(org.bukkit.entity.Entity e : b.getWorld().getEntities()) {
                if(e instanceof ArmorStand && e.getUniqueId().equals(uuid)) {
                    return (ArmorStand) e;
                }
            }
        }
        return null;
    }

}

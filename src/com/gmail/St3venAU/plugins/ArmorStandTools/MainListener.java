package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Skull;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class MainListener implements Listener {

    private final Pattern MC_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
    private final EulerAngle zero = new EulerAngle(0D, 0D, 0D);
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
                if (playerHasPermission(p, plugin.carryingArmorStand.get(p.getUniqueId()).getLocation().getBlock(), null)) {
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
            if (!playerHasPermission(p, event.getRightClicked().getLocation().getBlock(), tool)) {
                p.sendMessage(ChatColor.RED + Config.wgNoPerm);
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
                case INVIS:
                    as.setVisible(!as.isVisible());
                    p.sendMessage(ChatColor.GREEN + Config.asVisible + ": " + (as.isVisible() ? Config.isTrue : Config.isFalse));
                    break;
                case CLONE:
                    pickUpArmorStand(clone(as), p, true);
                    p.sendMessage(ChatColor.GREEN + Config.asCloned);
                    Utils.actionBarMsg(p, ChatColor.GREEN + Config.carrying);
                    break;
                case SAVE:
                    generateCmdBlock(p.getLocation(), as);
                    p.sendMessage(ChatColor.GREEN + Config.cbCreated);
                    break;
                case SIZE:
                    as.setSmall(!as.isSmall());
                    p.sendMessage(ChatColor.GREEN + Config.size + ": " + (as.isSmall() ? Config.small : Config.normal));
                    break;
                case BASE:
                    as.setBasePlate(!as.hasBasePlate());
                    p.sendMessage(ChatColor.GREEN + Config.basePlate + ": " + (as.hasBasePlate() ? Config.isOn : Config.isOff));
                    break;
                case GRAV:
                    as.setGravity(!as.hasGravity());
                    p.sendMessage(ChatColor.GREEN + Config.gravity + ": " + (as.hasGravity() ? Config.isOn : Config.isOff));
                    break;
                case ARMS:
                    as.setArms(!as.hasArms());
                    p.sendMessage(ChatColor.GREEN + Config.arms + ": " + (as.hasArms() ? Config.isOn : Config.isOff));
                    break;
                case NAME:
                    setName(p, as);
                    break;
                case PHEAD:
                    setPlayerSkull(p, as);
                    break;
                case INVUL:
                    p.sendMessage(ChatColor.GREEN + Config.invul + ": " + (NBT.toggleInvulnerability(as) ? Config.isOn : Config.isOff));
                    break;
                case SLOTS:
                    p.sendMessage(ChatColor.GREEN + Config.equip + ": " + (NBT.toggleSlotsDisabled(as) ? Config.locked : Config.unLocked));
                    break;
                case MOVE:
                    UUID uuid = p.getUniqueId();
                    if(plugin.carryingArmorStand.containsKey(uuid)) {
                        plugin.carryingArmorStand.remove(uuid);
                        Utils.actionBarMsg(p, Config.asDropped);
                    } else {
                        pickUpArmorStand(as, p, false);
                        Utils.actionBarMsg(p, ChatColor.GREEN + Config.carrying);
                    }
                    break;
                case NODEL: // Developer tool - do not use
                    if(as.getMaxHealth() == 50) {
                        as.setMaxHealth(20);
                        p.sendMessage(ChatColor.GREEN + "Deletion Protection: Disabled");
                    } else {
                        as.setMaxHealth(50);
                        p.sendMessage(ChatColor.GREEN + "Deletion Protection: Enabled");
                    }
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
        } else if(!(event.getRightClicked() instanceof ArmorStand) && ArmorStandTool.NAME.is(event.getPlayer().getItemInHand())) {
            event.setCancelled(true);
        }
    }

    private ArmorStand clone(ArmorStand as) {
        ArmorStand clone = (ArmorStand) as.getWorld().spawnEntity(as.getLocation().add(1, 0, 0), EntityType.ARMOR_STAND);
        clone.setGravity(as.hasGravity());
        clone.setHelmet(as.getHelmet());
        clone.setChestplate(as.getChestplate());
        clone.setLeggings(as.getLeggings());
        clone.setBoots(as.getBoots());
        clone.setItemInHand(as.getItemInHand());
        clone.setHeadPose(as.getHeadPose());
        clone.setBodyPose(as.getBodyPose());
        clone.setLeftArmPose(as.getLeftArmPose());
        clone.setRightArmPose(as.getRightArmPose());
        clone.setLeftLegPose(as.getLeftLegPose());
        clone.setRightLegPose(as.getRightLegPose());
        clone.setVisible(as.isVisible());
        clone.setBasePlate(as.hasBasePlate());
        clone.setArms(as.hasArms());
        clone.setCustomName(as.getCustomName());
        clone.setCustomNameVisible(as.isCustomNameVisible());
        clone.setSmall(as.isSmall());
        clone.setMaxHealth(as.getMaxHealth());
        NBT.setSlotsDisabled(clone, NBT.getDisabledSlots(as) == 2039583);
        NBT.setInvulnerable(clone, NBT.isInvulnerable(as));
        return clone;
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
            boolean perm = playerHasPermission(p, plugin.carryingArmorStand.get(p.getUniqueId()).getLocation().getBlock(), null);
            if (perm) {
                plugin.carryingArmorStand.remove(p.getUniqueId());
                Utils.actionBarMsg(p, Config.asDropped);
                event.setCancelled(true);
            } else {
                p.sendMessage(ChatColor.RED + Config.wgNoPerm);
            }
            return;
        }
        Action action = event.getAction();
        ArmorStandTool tool = ArmorStandTool.get(event.getItem());
        if(tool == null) return;
        if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            Utils.cycleInventory(p);
        } else if(action == Action.RIGHT_CLICK_BLOCK) {
            if (!playerHasPermission(p, event.getClickedBlock(), tool)) {
                p.sendMessage(ChatColor.RED + Config.wgNoPerm);
                return;
            }
            switch (tool) {
                case SUMMON:
                    event.setCancelled(true);
                    Location l = Utils.getLocationFacingPlayer(p);
                    pickUpArmorStand(spawnArmorStand(l), p, true);
                    Utils.actionBarMsg(p, ChatColor.GREEN + Config.carrying);
                    p.updateInventory();
                    break;
                case NAME:
                    event.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof ArmorStand && event.getDamager() instanceof Player && ArmorStandTool.isTool(((Player) event.getDamager()).getItemInHand())) {
            event.setCancelled(true);
            Utils.cycleInventory((Player) event.getDamager());
        }
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

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    private void generateCmdBlock(Location l, ArmorStand as) {
        Location loc = as.getLocation();
        int dSlots = NBT.getDisabledSlots(as);
        int hand = as.getItemInHand() == null ? 0 : as.getItemInHand().getTypeId();
        int handDmg = as.getItemInHand() == null ? 0 : as.getItemInHand().getDurability();
        int boots = as.getBoots() == null ? 0 : as.getBoots().getTypeId();
        int bootsDmg = as.getBoots() == null ? 0 : as.getBoots().getDurability();
        int legs = as.getLeggings() == null ? 0 : as.getLeggings().getTypeId();
        int legsDmg = as.getLeggings() == null ? 0 : as.getLeggings().getDurability();
        int chest = as.getChestplate() == null ? 0 : as.getChestplate().getTypeId();
        int chestDmg = as.getChestplate() == null ? 0 : as.getChestplate().getDurability();
        int helm = as.getHelmet() == null ? 0 : as.getHelmet().getTypeId();
        int helmDmg = as.getHelmet() == null ? 0 : as.getHelmet().getDurability();
        EulerAngle he = as.getHeadPose();
        EulerAngle ll = as.getLeftLegPose();
        EulerAngle rl = as.getRightLegPose();
        EulerAngle la = as.getLeftArmPose();
        EulerAngle ra = as.getRightArmPose();
        EulerAngle bo = as.getBodyPose();
        String cmd =
                "summon ArmorStand " + Utils.twoDec(loc.getX()) + " " + Utils.twoDec(loc.getY()) + " " + Utils.twoDec(loc.getZ()) + " {"
                        + (as.getMaxHealth() != 20    ? "Attributes:[{Name:\"generic.maxHealth\", Base:" + as.getMaxHealth() + "}]," : "")
                        + (as.isVisible()             ? "" : "Invisible:1,")
                        + (as.hasBasePlate()          ? "" : "NoBasePlate:1,")
                        + (as.hasGravity()            ? "" : "NoGravity:1,")
                        + (as.hasArms()               ? "ShowArms:1," : "")
                        + (as.isSmall()               ? "Small:1," : "")
                        + (NBT.isInvulnerable(as)     ? "Invulnerable:1," : "")
                        + (dSlots == 0                ? "" : ("DisabledSlots:" + dSlots + ","))
                        + (as.isCustomNameVisible()   ? "CustomNameVisible:1," : "")
                        + (as.getCustomName() == null ? "" : ("CustomName:\"" + as.getCustomName() + "\","))
                        + (loc.getYaw() == 0F         ? "" : ("Rotation:[" + Utils.twoDec(loc.getYaw()) + "f],"))
                        + (hand == 0 && boots == 0 && legs == 0 && chest == 0 && helm == 0 ? "" : (
                        "Equipment:["
                                + "{id:" + hand  + ",Damage:" + handDmg  + NBT.getItemStackTags(as.getItemInHand()) + "},"
                                + "{id:" + boots + ",Damage:" + bootsDmg + NBT.getItemStackTags(as.getBoots()) + "},"
                                + "{id:" + legs  + ",Damage:" + legsDmg  + NBT.getItemStackTags(as.getLeggings()) + "},"
                                + "{id:" + chest + ",Damage:" + chestDmg + NBT.getItemStackTags(as.getChestplate()) + "},"
                                + "{id:" + helm  + ",Damage:" + helmDmg  + NBT.getItemStackTags(as.getHelmet()) + NBT.skullOwner(as.getHelmet()) + "}],"))
                        + "Pose:{"
                        + (bo.equals(zero) ? "" : ("Body:["     + Utils.angle(bo.getX()) + "f," + Utils.angle(bo.getY()) + "f," + Utils.angle(bo.getZ()) + "f],"))
                        + (he.equals(zero) ? "" : ("Head:["     + Utils.angle(he.getX()) + "f," + Utils.angle(he.getY()) + "f," + Utils.angle(he.getZ()) + "f],"))
                        + (ll.equals(zero) ? "" : ("LeftLeg:["  + Utils.angle(ll.getX()) + "f," + Utils.angle(ll.getY()) + "f," + Utils.angle(ll.getZ()) + "f],"))
                        + (rl.equals(zero) ? "" : ("RightLeg:[" + Utils.angle(rl.getX()) + "f," + Utils.angle(rl.getY()) + "f," + Utils.angle(rl.getZ()) + "f],"))
                        + (la.equals(zero) ? "" : ("LeftArm:["  + Utils.angle(la.getX()) + "f," + Utils.angle(la.getY()) + "f," + Utils.angle(la.getZ()) + "f],"))
                        + "RightArm:[" + Utils.angle(ra.getX()) + "f," + Utils.angle(ra.getY()) + "f," + Utils.angle(ra.getZ()) + "f]}}";
        Block b = l.getBlock();
        b.setType(Material.COMMAND);
        b.setData((byte) 0);
        CommandBlock cb = (CommandBlock) b.getState();
        cb.setCommand(cmd);
        cb.update();
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
                                            as.setHelmet(b.getDrops().iterator().next());
                                            event.getPlayer().sendMessage(ChatColor.GREEN + Config.appliedHead + ChatColor.GOLD + " " + name);
                                        } else {
                                            event.getPlayer().sendMessage(ChatColor.RED + Config.noHead + ChatColor.GOLD + " " + name);
                                        }
                                        b.setType(Material.AIR);
                                        b.setData((byte) 0);
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

    @SuppressWarnings("deprecation")
    private void setName(Player p, ArmorStand as) {
        Block b = Utils.findAnAirBlock(p.getLocation());
        if(b == null) {
            p.sendMessage(ChatColor.RED + Config.noAirError);
            return;
        }
        if(!checkPermission(p, b)) {
            p.sendMessage(ChatColor.RED + Config.wgNoPerm);
            return;
        }
        b.setData((byte) 0);
        b.setType(Material.SIGN_POST);
        Utils.openSign(p, b);
        b.setMetadata("armorStand", new FixedMetadataValue(plugin, as.getUniqueId()));
        b.setMetadata("setName", new FixedMetadataValue(plugin, true));
    }

    @SuppressWarnings("deprecation")
    private void setPlayerSkull(Player p, ArmorStand as) {
        Block b = Utils.findAnAirBlock(p.getLocation());
        if(b == null) {
            p.sendMessage(ChatColor.RED + Config.noAirError);
            return;
        }
        if(!checkPermission(p, b)) {
            p.sendMessage(ChatColor.RED + Config.wgNoPerm);
            return;
        }
        b.setData((byte) 0);
        b.setType(Material.SIGN_POST);
        Utils.openSign(p, b);
        b.setMetadata("armorStand", new FixedMetadataValue(plugin, as.getUniqueId()));
        b.setMetadata("setSkull", new FixedMetadataValue(plugin, true));
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
    
    private static boolean checkPermission(Player player, Block block) {
        
        // Check PlotSquared
        Location loc = block.getLocation();
        if (PlotSquaredHook.api != null) {
            if (PlotSquaredHook.isPlotWorld(loc)) {
                return PlotSquaredHook.checkPermission(player, loc);
            }
        }
        
        // check WorldGuard
        if(Config.worldGuardPlugin != null) {
            return Config.worldGuardPlugin.canBuild(player, block);
        }
        
        // Use standard permission checking (will support basically any plugin)
        BlockBreakEvent myBreak = new BlockBreakEvent(block, player);
        Bukkit.getServer().getPluginManager().callEvent(myBreak);
        boolean hasPerm = !myBreak.isCancelled();
        BlockPlaceEvent place = new BlockPlaceEvent(block, block.getState(), block, null, player, true);
        Bukkit.getServer().getPluginManager().callEvent(place);
        if (place.isCancelled()) {
          hasPerm = false;
        }
        return hasPerm;
    }
    
    private static boolean playerHasPermission(Player p, Block b, ArmorStandTool tool) {
        if(tool != null && !p.isOp() && (!Utils.hasPermissionNode(p, "astools.use")
        ||(ArmorStandTool.SAVE == tool  && !Utils.hasPermissionNode(p, "astools.cmdblock"))
        ||(ArmorStandTool.CLONE == tool && !Utils.hasPermissionNode(p, "astools.clone")))) {
            p.sendMessage(ChatColor.RED + Config.noPerm);
            return false;
        }
        return checkPermission(p, b);
    }

    void pickUpArmorStand(ArmorStand as, Player p, boolean newlySummoned) {
        plugin.carryingArmorStand.put(p.getUniqueId(), as);
        if(newlySummoned) return;
        as.setMetadata("startLoc", new FixedMetadataValue(plugin, as.getLocation()));
    }

}

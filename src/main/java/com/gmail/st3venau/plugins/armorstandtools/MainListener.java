package com.gmail.st3venau.plugins.armorstandtools;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainListener implements Listener {

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand as)) {
            return;
        }

        Player p = event.getPlayer();
        if (ArmorStandGUI.isInUse(as)) {
            Utils.title(p, Config.guiInUse);
            event.setCancelled(true);
            return;
        }

        if (stopEditing(p, false)) {
            event.setCancelled(true);
            return;
        }

        ArmorStandTool tool = ArmorStandTool.get(p);
        if (tool == null && p.isSneaking() && Config.crouchRightClickOpensGUI && Utils.hasPermissionNode(p, "astools.use")) {
            if (!AST.playerHasPermission(p, as.getLocation().getBlock(), null)) {
                p.sendMessage(ChatColor.RED + Config.generalNoPerm);
                return;
            }

            new ArmorStandGUI(as, p);
            event.setCancelled(true);
            return;
        }

        if (!event.isCancelled() && tool != null) {
            if (!AST.playerHasPermission(p, as.getLocation().getBlock(), tool)) {
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

            switch (tool) {
                case HEADX -> as.setHeadPose(as.getHeadPose().setX(angle));
                case HEADY -> as.setHeadPose(as.getHeadPose().setY(angle));
                case HEADZ -> as.setHeadPose(as.getHeadPose().setZ(angle));
                case LARMX -> as.setLeftArmPose(as.getLeftArmPose().setX(angle));
                case LARMY -> as.setLeftArmPose(as.getLeftArmPose().setY(angle));
                case LARMZ -> as.setLeftArmPose(as.getLeftArmPose().setZ(angle));
                case RARMX -> as.setRightArmPose(as.getRightArmPose().setX(angle));
                case RARMY -> as.setRightArmPose(as.getRightArmPose().setY(angle));
                case RARMZ -> as.setRightArmPose(as.getRightArmPose().setZ(angle));
                case LLEGX -> as.setLeftLegPose(as.getLeftLegPose().setX(angle));
                case LLEGY -> as.setLeftLegPose(as.getLeftLegPose().setY(angle));
                case LLEGZ -> as.setLeftLegPose(as.getLeftLegPose().setZ(angle));
                case RLEGX -> as.setRightLegPose(as.getRightLegPose().setX(angle));
                case RLEGY -> as.setRightLegPose(as.getRightLegPose().setY(angle));
                case RLEGZ -> as.setRightLegPose(as.getRightLegPose().setZ(angle));
                case BODYX -> as.setBodyPose(as.getBodyPose().setX(angle));
                case BODYY -> as.setBodyPose(as.getBodyPose().setY(angle));
                case BODYZ -> as.setBodyPose(as.getBodyPose().setZ(angle));
                case MOVEX -> AST.scheduler().runAtEntity(as, task -> as.teleportAsync(as.getLocation().add(0.05 * (p.isSneaking() ? -1 : 1), 0.0, 0.0)));
                case MOVEY -> AST.scheduler().runAtEntity(as, task -> as.teleportAsync(as.getLocation().add(0.0, 0.05 * (p.isSneaking() ? -1 : 1), 0.0)));
                case MOVEZ -> AST.scheduler().runAtEntity(as, task -> as.teleportAsync(as.getLocation().add(0.0, 0.0, 0.05 * (p.isSneaking() ? -1 : 1))));
                case ROTAT -> {
                    Location l = as.getLocation();
                    l.setYaw(((float) num) * 180F);
                    AST.scheduler().runAtEntity(as, task -> as.teleportAsync(l));
                }
                case GUI -> new ArmorStandGUI(as, p);
                default -> cancel = tool == ArmorStandTool.SUMMON || tool == ArmorStandTool.GEN_CMD || event.isCancelled();
            }

            event.setCancelled(cancel);
            return;
        }
        if ((Config.ignoreWGForASCmdExecution || !event.isCancelled()) && !p.isSneaking()) {
            ArmorStandCmdManager asCmdManager = new ArmorStandCmdManager(as);
            if (asCmdManager.hasCommands() && Utils.hasPermissionNode(p, "astools.ascmd.execute")) {
                event.setCancelled(true);
                asCmdManager.executeCommands(p);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame && ArmorStandTool.isHoldingTool(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (ArmorStandTool.isHoldingTool(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo().getWorld() == null || event.getTo().getWorld().equals(event.getFrom().getWorld())) {
            return;
        }

        final Player p = event.getPlayer();
        final ArmorStand as = AST.getCarryingArmorStand(p);
        if (as == null || as.isDead()) {
            stopEditing(p,false);
            return;
        }

        if (!Config.allowMoveWorld) {
            AST.returnArmorStand(as);
            stopEditing(p, true);
        }

        if (Config.deactivateOnWorldChange && AST.savedInventories.containsKey(p.getUniqueId())) {
            AST.restoreInventory(p);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        stopEditing(p, true);
        if (AST.savedInventories.containsKey(p.getUniqueId())) {
            AST.restoreInventory(event.getPlayer());
        }
    }

    boolean stopEditing(Player p, boolean force) {
        ArmorStand carrying = AST.getCarryingArmorStand(p);
        if (carrying != null && !carrying.isDead()) {
            p.setMetadata("lastEvent", new FixedMetadataValue(AST.plugin, System.currentTimeMillis()));
            if (AST.playerHasPermission(p, carrying.getLocation().getBlock(), null)) {
                Utils.title(p, Config.asDropped);
                carrying.removeMetadata("clone", AST.plugin);
            } else {
                if (force) {
                    AST.returnArmorStand(carrying);
                } else {
                    p.sendMessage(ChatColor.RED + Config.wgNoPerm);
                    return true;
                }
            }
        }

        UUID uuid = p.getUniqueId();
        AST.selectedArmorStand.remove(uuid);
        boolean editing = AST.activeTool.containsKey(uuid);
        if (editing) {
            p.setMetadata("lastEvent", new FixedMetadataValue(AST.plugin, System.currentTimeMillis()));
            AST.activeTool.remove(uuid);
            Utils.title(p, "");
        }

        return editing;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player p = event.getEntity();
        List<ItemStack> drops = event.getDrops();
        for (ArmorStandTool t : ArmorStandTool.values()) {
            drops.remove(t.getItem());
        }

        if (Boolean.TRUE.equals(p.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) {
            return;
        }

        if (Bukkit.getServer().getPluginManager().getPermission("essentials.keepinv") != null && Utils.hasPermissionNode(p, "essentials.keepinv")) {
            return;
        }

        if (AST.savedInventories.containsKey(p.getUniqueId())) {
            drops.addAll(Arrays.asList(AST.savedInventories.get(p.getUniqueId())));
            AST.savedInventories.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player p = (Player) event.getWhoClicked();
        CraftingInventory inventory = event.getInventory();
        for (ItemStack is : inventory.getContents()) {
            if (ArmorStandTool.isTool(is)) {
                event.setCancelled(true);
                p.updateInventory();
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof final Player p)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (event.getInventory().getHolder() != p && ArmorStandTool.isTool(item)) {
            event.setCancelled(true);
            p.updateInventory();
            return;
        }

        if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            if (Utils.hasAnyTools(p)) {
                event.setCancelled(true);
                p.updateInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof final Player p)) {
            return;
        }

        if (event.getInventory().getHolder() != p && Utils.containsItems(event.getNewItems().values())) {
            event.setCancelled(true);
            p.updateInventory();
        }
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (ArmorStandTool.isTool(event.getItemDrop().getItemStack())) {
            AST.scheduler().runAtEntity(event.getItemDrop(), task -> event.getItemDrop().remove());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        final Player p = event.getPlayer();
        if (stopEditing(p, false)) {
            event.setCancelled(true);
            return;
        }

        Action action = event.getAction();
        ItemStack inHand = event.getItem();
        ArmorStandTool tool = ArmorStandTool.get(inHand);
        if (tool == null) {
            return;
        }

        event.setCancelled(true);
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            Utils.cycleInventory(p);
        } else if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) && tool == ArmorStandTool.SUMMON) {
            if (!AST.playerHasPermission(p, event.getClickedBlock(), tool)) {
                p.sendMessage(ChatColor.RED + Config.generalNoPerm);
                return;
            }

            Location l = Utils.getLocationFacing(p.getLocation());
            AST.pickUpArmorStand(spawnArmorStand(l), p, true);
            Utils.title(p, Config.carrying);
        }

        AST.scheduler().runLater(task -> p.updateInventory(), 1L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand as)) {
            return;
        }

        if (ArmorStandGUI.isInUse(as) || as.isInvulnerable()) {
            event.setCancelled(true);
        }

        if (event.getDamager() instanceof Player p) {
            if (stopEditing(p, false)) {
                event.setCancelled(true);
                return;
            }

            if (ArmorStandTool.isHoldingTool(p)) {
                event.setCancelled(true);
                Utils.cycleInventory(p);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ArmorStand as) {
            if (ArmorStandGUI.isInUse(as) || as.isInvulnerable()) {
                event.setCancelled(true);
            }
        }
    }

    private ArmorStand spawnArmorStand(Location l) {
        World w = l.getWorld();
        assert w != null;
        ArmorStand as = (ArmorStand) w.spawnEntity(l, EntityType.ARMOR_STAND);
        EntityEquipment equipment = as.getEquipment();
        equipment.setHelmet(Config.helmet);
        equipment.setChestplate(Config.chest);
        equipment.setLeggings(Config.pants);
        equipment.setBoots(Config.boots);
        equipment.setItemInMainHand(Config.itemInHand);
        equipment.setItemInOffHand(Config.itemInOffHand);
        as.setVisible(Config.isVisible);
        as.setSmall(Config.isSmall);
        as.setArms(Config.hasArms);
        as.setBasePlate(Config.hasBasePlate);
        as.setGravity(Config.hasGravity);
        as.setInvulnerable(Config.invulnerable);
        if (!Config.defaultName.isEmpty()) {
            as.setCustomName(Config.defaultName);
            as.setCustomNameVisible(true);
        }

        Utils.setSlotsDisabled(as, Config.equipmentLock);
        return as;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if ((b.getType() == Material.PLAYER_HEAD && b.hasMetadata("protected")) || (b.getType() == Material.OAK_SIGN && b.hasMetadata("armorStand"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(final AsyncChatEvent event) {
        if (Config.useCommandForTextInput) {
            return;
        }

        Player player = event.getPlayer();
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (AST.processInput(player, plainMessage)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommand(final PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        String cmd = event.getMessage().split(" ")[0].toLowerCase();
        while (!cmd.isEmpty() && cmd.charAt(0) == '/') {
            cmd = cmd.substring(1);
        }

        if (!cmd.isEmpty() && Config.deniedCommands.contains(cmd) && Utils.hasAnyTools(p)) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + Config.cmdNotAllowed);
        }
    }
}

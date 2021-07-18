package com.gmail.st3venau.plugins.armorstandtools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.MetadataValue;

import java.util.UUID;
import java.util.regex.Pattern;

@SuppressWarnings("CommentedOutCode")
public class MainListener implements Listener {

    private static final Pattern MC_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player p = event.getPlayer();
        if(stopEditing(p, false)) {
            event.setCancelled(true);
            AST.debug("Interaction cancelled as player is already editing");
        }
        if(!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand as = (ArmorStand) event.getRightClicked();
        AST.debug(p.getName() + " right-clicked " + as.getName() + ", Crouching: " + p.isSneaking());
        if(event.isCancelled()) {
            AST.debug("Interaction with Armor Stand was cancelled by a plugin");
        }
        if(!event.isCancelled() && ArmorStandGUI.isInUse(as)) {
            Utils.title(p, Config.guiInUse);
            event.setCancelled(true);
            return;
        }
        if(!event.isCancelled() && p.isSneaking()) {
            if (!AST.playerHasPermission(p, as.getLocation().getBlock(), null)) {
                p.sendMessage(ChatColor.RED + Config.generalNoPerm);
                return;
            }
            new ArmorStandGUI(as, p);
            event.setCancelled(true);
            return;
        }
        if((Config.ignoreWGForASCmdExecution || !event.isCancelled()) && !p.isSneaking()) {
            ArmorStandCmd asCmd = new ArmorStandCmd(as);
            if (asCmd.getCommand() != null) {
                event.setCancelled(true);
                if (Utils.hasPermissionNode(p, "astools.ascmd.execute")) {
                    if (!asCmd.execute(p)) {
                        p.sendMessage(Config.executeCmdError);
                    }
                }
            }
        }
    }

    ArmorStand getCarryingArmorStand(Player p) {
        UUID uuid = p.getUniqueId();
        return  ArmorStandTool.MOVE == AST.activeTool.get(uuid) ? AST.selectedArmorStand.get(uuid) : null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(event.getTo() == null || event.getFrom().getWorld() == event.getTo().getWorld()) return;
        final Player p = event.getPlayer();
        final ArmorStand as = getCarryingArmorStand(p);
        if (as == null || as.isDead()) {
            stopEditing(p,false);
            return;
        }
        if (!Config.allowMoveWorld) {
            AST.returnArmorStand(as);
            stopEditing(p, true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopEditing(event.getPlayer(), true);
    }

    boolean stopEditing(Player p, boolean force) {
        ArmorStand carrying = getCarryingArmorStand(p);
        if(carrying != null && !carrying.isDead()) {
            if (AST.playerHasPermission(p, carrying.getLocation().getBlock(), null)) {
                Utils.title(p, Config.asDropped);
                carrying.removeMetadata("clone", AST.plugin);
            } else {
                if(force) {
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
        if(editing) {
            AST.activeTool.remove(uuid);
            Utils.title(p, "");
        }
        return editing;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if(stopEditing(p, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) event.getEntity();
            if(event.getDamager() instanceof Player && stopEditing((Player) event.getDamager(), false)) {
                event.setCancelled(true);
                return;
            }
            if(ArmorStandGUI.isInUse(as) || as.isInvulnerable()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) event.getEntity();
            if(ArmorStandGUI.isInUse(as) || as.isInvulnerable()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if((b.getType() == Material.PLAYER_HEAD && b.hasMetadata("protected")) || (b.getType() == Material.OAK_SIGN && b.hasMetadata("armorStand"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        final Block b = event.getBlock();
        if(!b.hasMetadata("armorStand")) {
            return;
        }
        final ArmorStand as = getArmorStand(b);
        if (as != null) {
            StringBuilder sb = new StringBuilder();
            for (String line : event.getLines()) {
                if (line != null && line.length() > 0) {
                    sb.append(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            String input = sb.toString();
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
                    if(as.getEquipment() != null) {
                        as.getEquipment().setHelmet(getPlayerHead(input));
                    }
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + input + " " + Config.invalidName);
                }
            }
        }
        event.setCancelled(true);
        b.removeMetadata("armorStand", AST.plugin);
        b.removeMetadata("setName", AST.plugin);
        b.removeMetadata("setSkull", AST.plugin);
        b.setType(Material.AIR);
    }

    @SuppressWarnings("deprecation")
    private ItemStack getPlayerHead(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getServer().getPlayer(playerName);
        if(offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        }
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) item.getItemMeta();
        if(meta == null) {
            Bukkit.getLogger().warning("Skull item meta was null");
            return item;
        }
        meta.setOwningPlayer(offlinePlayer);
        item.setItemMeta(meta);
        return item;
    }

    private ArmorStand getArmorStand(Block b) {
        UUID uuid = null;
        for (MetadataValue value : b.getMetadata("armorStand")) {
            if (value.getOwningPlugin() == AST.plugin) {
                uuid = (UUID) value.value();
            }
        }
        b.removeMetadata("armorStand", AST.plugin);
        if (uuid != null) {
            for(org.bukkit.entity.Entity e : b.getWorld().getEntities()) {
                if(e instanceof ArmorStand && e.getUniqueId().equals(uuid)) {
                    return (ArmorStand) e;
                }
            }
        }
        return null;
    }

    // Give all permissions to all players - for testing only
    /*@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        PermissionAttachment attachment = p.addAttachment(AST.plugin);
        attachment.setPermission("astools.use", true);
        attachment.setPermission("astools.summon", true);
        attachment.setPermission("astools.clone", true);
        attachment.setPermission("astools.head", true);
        attachment.setPermission("astools.reload", true);
        attachment.setPermission("astools.cmdblock", true);
        attachment.setPermission("astools.ascmd.view", true);
        attachment.setPermission("astools.ascmd.remove", true);
        attachment.setPermission("astools.ascmd.assign.player", true);
        attachment.setPermission("astools.ascmd.assign.console", true);
        attachment.setPermission("astools.ascmd.cooldown", true);
        attachment.setPermission("astools.ascmd.execute", true);
        attachment.setPermission("astools.new", true);
        //attachment.setPermission("astools.bypass-wg-flag", true);
    }*/

}

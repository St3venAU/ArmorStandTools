package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin {

    public final HashMap<UUID, ArmorStand> carryingArmorStand = new HashMap<UUID, ArmorStand>();
    public final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<UUID, ItemStack[]>();
    static String NMS_VERSION;

    @Override
    public void onEnable() {
        NMS_VERSION = getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
        CommandExecutor ce = new Commands(this);
        getCommand("astools").setExecutor(ce);
        Config.reload(this);
    }

    @Override
    public void onDisable() {
        for(ArmorStand as : carryingArmorStand.values()) {
            returnArmorStand(as);
        }
        carryingArmorStand.clear();
        Player p;
        for(UUID uuid : savedInventories.keySet()) {
            p = getServer().getPlayer(uuid);
            if(p != null && p.isOnline()) {
                p.getInventory().setContents(savedInventories.get(uuid));
                p.sendMessage(ChatColor.GREEN + Config.invReturned);
            }
        }
        savedInventories.clear();
    }

    void returnArmorStand(ArmorStand as) {
        if(as.hasMetadata("startLoc")) {
            for (MetadataValue value : as.getMetadata("startLoc")) {
                if (value.getOwningPlugin() == this) {
                    as.teleport((Location) value.value());
                    as.removeMetadata("startLoc", this);
                    return;
                }
            }
        }
        as.remove();
    }
}
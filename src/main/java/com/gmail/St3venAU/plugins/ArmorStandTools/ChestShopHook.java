package com.gmail.St3venAU.plugins.ArmorStandTools;

import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChestShopHook implements Listener {

    private final Main plugin;

    public ChestShopHook(Main plugin) {
        this.plugin = plugin;
        PreTransactionEvent.getHandlerList().unregister(plugin); // Avoid multiple registers
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPreTransactionEvent(PreTransactionEvent event) {
        if (plugin.savedInventories.containsKey(event.getClient().getUniqueId())) {
            event.setCancelled(true);
        }
    }

}

package com.gmail.St3venAU.plugins.ArmorStandTools;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("deprecation")
class PlotSquaredHook {
    
    public static PlotAPI api = null;
    private static Main plugin;
    
    public PlotSquaredHook(Main main) {
        PlotSquaredHook.api = new PlotAPI(plugin);
        plugin = main;
    }
    
    public static boolean isPlotWorld(Location loc) {
        World world = loc.getWorld();
        return api.isPlotWorld(world);
    }

    public static boolean checkPermission(Player player, Location loc) {
        Plot plot = api.getPlot(loc);
        PlotPlayer pp = PlotPlayer.wrap(player);
        plugin.debug("Plot: " + plot);
        if (plot == null) {
            plugin.debug("plots.admin.build.road: " + pp.hasPermission("plots.admin.build.road"));
            return pp.hasPermission("plots.admin.build.road");
        }
        UUID uuid = pp.getUUID();
        plugin.debug("plot.isAdded: " + plot.isAdded(uuid));
        plugin.debug("plots.admin.build.other: " + pp.hasPermission("plots.admin.build.other"));
        return plot.isAdded(uuid) || pp.hasPermission("plots.admin.build.other");
    }
}

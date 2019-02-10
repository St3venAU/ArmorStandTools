package com.gmail.St3venAU.plugins.ArmorStandTools;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

class PlotSquaredHook {
    
    public static PlotAPI api;
    private static Main plugin;
    
    public PlotSquaredHook(Main main) {
        PlotSquaredHook.api = new PlotAPI();
        plugin = main;
    }
    
    public static boolean isPlotWorld(Location loc) {
        return api.getPlotSquared().hasPlotArea(loc.getWorld().getName());
    }

    public static boolean checkPermission(Player player, Location location) {
        com.github.intellectualsites.plotsquared.plot.object.Location plotLocation = BukkitUtil.getLocation(location);
        PlotArea plotArea = plotLocation.getPlotArea();
        if(plotArea == null) {
            plugin.debug("plots.admin.build.road: " + player.hasPermission("plots.admin.build.road"));
            return player.hasPermission("plots.admin.build.road");
        }
        Plot plot = plotArea.getPlot(plotLocation);
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

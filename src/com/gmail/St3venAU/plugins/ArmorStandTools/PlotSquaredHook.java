package com.gmail.St3venAU.plugins.ArmorStandTools;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.Permissions;

@SuppressWarnings("deprecation")
class PlotSquaredHook {
    
    public static PlotAPI api = null;
    
    public PlotSquaredHook(Main plugin) {
        PlotSquaredHook.api = new PlotAPI(plugin);
    }
    
    public static boolean isPlotWorld(Location loc) {
        World world = loc.getWorld();
        return api.isPlotWorld(world);
    }

    public static boolean checkPermission(Player player, Location loc) {
        Plot plot = api.getPlot(loc);
        PlotPlayer pp = PlotPlayer.wrap(player);
        if (plot == null) {
            return pp.hasPermission("plots.admin.build.road");
        }
        UUID uuid = pp.getUUID();
        return plot.isAdded(uuid) || pp.hasPermission("plots.admin.build.other");
    }
}

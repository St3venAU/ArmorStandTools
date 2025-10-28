package com.gmail.st3venau.plugins.armorstandtools;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlotSquaredHook {

    public static PlotAPI api = null;

    public static void init() {
        if (api != null) {
            return;
        }

        api = new PlotAPI();
    }

    public static boolean isPlotWorld(Location l) {
        return l.getWorld() != null && api.getPlotSquared().getPlotAreaManager().hasPlotArea(l.getWorld().getName());
    }

    public static Boolean checkPermission(Player p, Location l) {
        if (l.getWorld() == null) {
            return null;
        }

        com.plotsquared.core.location.Location plotLocation = com.plotsquared.core.location.Location.at(l.getWorld().getName(), BlockVector3.at(l.getBlockX(), l.getBlockY(), l.getBlockZ()));
        PlotArea plotArea = plotLocation.getPlotArea();
        if (plotArea == null) {
            return p.hasPermission("plots.admin.build.road");
        }

        Plot plot = plotArea.getPlot(plotLocation);
        PlotPlayer<?> pp = api.wrapPlayer(p.getUniqueId());
        if (pp == null) {
            return null;
        }

        if (plot == null) {
            return pp.hasPermission("plots.admin.build.road");
        }

        UUID uuid = pp.getUUID();
        return plot.isAdded(uuid) || pp.hasPermission("plots.admin.build.other");
    }
}

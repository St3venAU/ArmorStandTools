package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;


import es.pollitoyeye.vehicles.VehiclesMain;


class VehiclesHook {
    
    public static VehiclesMain plugin;
    
    public VehiclesHook(Plugin main) {
        plugin = (VehiclesMain) main;
    }
    
    public static boolean isVehicleArmorStand(ArmorStand as) {
    	String aSCustomName = as.getCustomName();
    	if(aSCustomName == null) {
    		return false;
    	}
    	if(plugin.getEventListener().isVehicleName(aSCustomName)) {
    		return true;
    	}
    	return false;
    }
}

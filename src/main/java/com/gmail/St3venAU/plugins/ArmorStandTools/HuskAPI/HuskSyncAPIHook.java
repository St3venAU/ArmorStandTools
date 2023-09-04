package com.gmail.St3venAU.plugins.ArmorStandTools.HuskAPI;

import net.william278.husksync.api.HuskSyncAPI;
import net.william278.husksync.player.OnlineUser;
import net.william278.husksync.player.User;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HuskSyncAPIHook {
    private final HuskSyncAPI mHuskSyncAPI;
    public HuskSyncAPIHook() {
        mHuskSyncAPI = HuskSyncAPI.getInstance();
    }

    public void SaveUser(Player player) {
        OnlineUser user = mHuskSyncAPI.getUser(player);
        mHuskSyncAPI.saveUserData(user);
    }
}

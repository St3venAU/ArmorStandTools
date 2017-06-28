package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.entity.ArmorStand;

import java.util.Set;

@SuppressWarnings("unused")
class NMS_v1_9_R1 extends NMS {

    public NMS_v1_9_R1(String nmsVersion) {
        super(
                nmsVersion,                             // NMS Version
                "ArmorStand",                           // Armor Stand summon name
                "bz",                                   // Disabled slots field name
                "a",                                    // getKey field name
                "IChatBaseComponent$ChatSerializer",    // ChatSerializer field name
                true,                                   // Version has an off hand
                true                                    // Version supports scoreboard tags
        );
    }

    @Override
    boolean addScoreboardTag(ArmorStand as, String tag) {
        try {
            Object nmsEntity = getNmsEntity(as);
            return (Boolean) getNMSClass("Entity").getMethod("a", String.class).invoke(nmsEntity, tag);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    void removeScoreboardTag(ArmorStand as, String tag) {
        try {
            Object nmsEntity = getNmsEntity(as);
            getNMSClass("Entity").getMethod("b", String.class).invoke(nmsEntity, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    Set<String> getScoreboardTags(ArmorStand as) {
        try {
            Object nmsEntity = getNmsEntity(as);
            return (Set<String>) getNMSClass("Entity").getMethod("P").invoke(nmsEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}

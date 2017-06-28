package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.entity.ArmorStand;

@SuppressWarnings("unused")
class NMS_v1_8_R3 extends NMS {

    public NMS_v1_8_R3(String nmsVersion) {
        super(
                nmsVersion,                             // NMS Version
                "ArmorStand",                           // Armor Stand summon name
                "bi",                                   // Disabled slots field name
                "a",                                    // getKey field name
                "IChatBaseComponent$ChatSerializer",    // ChatSerializer field name
                false,                                  // Version has an off hand
                false                                   // Version supports scoreboard tags
        );
    }

    @Override
    boolean isInvulnerable(ArmorStand as) {
        Object nmsEntity = getNmsEntity(as);
        if (nmsEntity == null) return false;
        Object tag = getTag(nmsEntity);
        return tag != null && getInvulnerableBoolean(tag);
    }

    @Override
    void setInvulnerable(ArmorStand as, boolean invulnerable) {
        Object nmsEntity = getNmsEntity(as);
        if (nmsEntity == null) return;
        Object tag = getTag(nmsEntity);
        if(tag == null) return;
        setInvulnerableBoolean(tag, invulnerable);
        saveTagF(nmsEntity, tag);
    }

}

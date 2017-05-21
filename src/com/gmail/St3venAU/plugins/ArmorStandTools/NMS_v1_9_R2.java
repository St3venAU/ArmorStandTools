package com.gmail.St3venAU.plugins.ArmorStandTools;

@SuppressWarnings("unused")
class NMS_v1_9_R2 extends NMS {

    public NMS_v1_9_R2() {
        super(
                "v1_9_R2",                              // NMS Version
                "ArmorStand",                           // Armor Stand summon name
                "bA",                                   // Disabled slots field name
                "a",                                    // getKey field name
                "IChatBaseComponent$ChatSerializer",    // ChatSerializer field name
                true                                    // Version has an off hand
        );
    }

}

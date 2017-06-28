package com.gmail.St3venAU.plugins.ArmorStandTools;

@SuppressWarnings("unused")
class NMS_v1_11_R1 extends NMS {

    public NMS_v1_11_R1(String nmsVersion) {
        super(
                nmsVersion,                             // NMS Version
                "minecraft:armor_stand",                // Armor Stand summon name
                "bA",                                   // Disabled slots field name
                "a",                                    // getKey field name
                "IChatBaseComponent$ChatSerializer",    // ChatSerializer field name
                true,                                  // Version has an off hand
                true                                   // Version supports scoreboard tags
        );
    }

}

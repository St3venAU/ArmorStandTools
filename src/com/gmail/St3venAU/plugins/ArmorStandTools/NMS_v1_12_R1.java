package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

@SuppressWarnings("unused")
class NMS_v1_12_R1 extends NMS {

    public NMS_v1_12_R1() {
        super(
                "v1_12_R1",                             // NMS Version
                "minecraft:armor_stand",                // Armor Stand summon name
                "bB",                                   // Disabled slots field name
                "getKey",                               // getKey field name
                "IChatBaseComponent$ChatSerializer",    // ChatSerializer field name
                true                                    // Version has an off hand
        );
    }

    @Override
    void actionBarMsg(Player p, String msg) {
        try {
            Object chat = getNMSClass("ChatSerializer").getMethod("a", String.class).invoke(null, "{\"text\":\"" + msg + "\",\"color\":\"green\"}");
            Object packet;
            Constructor constructor = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent"), getNMSClass("ChatMessageType"));
            packet = constructor.newInstance(chat, getNMSClass("ChatMessageType").getEnumConstants()[2]);
            sendPacket(p, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

package com.gmail.st3venau.plugins.armorstandtools;

enum CommandType {

    PLAYER ("Player",  "plr", "astools.ascmd.add.player"),
    CONSOLE("Console", "con", "astools.ascmd.add.console"),
    BUNGEE ("Bungee",  "bun", "astools.ascmd.add.bungee");

    private final String name;
    private final String tag;
    private final String addPermission;

    CommandType(String name, String tag, String addPermission) {
        this.name = name;
        this.tag = tag;
        this.addPermission = addPermission;
    }

    String getTag() {
        return tag;
    }

    String getName() {
        return name;
    }

    String getAddPermission() {
        return addPermission;
    }

    static CommandType fromTag(String tag) {
        for (CommandType type : values()) {
            if (type.tag.equalsIgnoreCase(tag)) {
                return type;
            }
        }

        return null;
    }

    static CommandType fromName(String name) {
        for (CommandType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}

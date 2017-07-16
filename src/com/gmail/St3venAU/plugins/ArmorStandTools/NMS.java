package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

abstract class NMS {

    private final static EulerAngle zero = new EulerAngle(0D, 0D, 0D);

    private final boolean offHand, scoreboardTags;

    private final String
            nmsVersion,
            summonEntityName,
            disabledSlotsFieldName,
            keyFieldName,
            chatSerializerFieldName;

    NMS(String nmsVersion, String summonEntityName, String disabledSlotsFieldName, String keyFieldName, String chatSerializerFieldName, boolean offHand, boolean scoreboardTags) {
        this.nmsVersion = nmsVersion;
        this.summonEntityName = summonEntityName;
        this.disabledSlotsFieldName = disabledSlotsFieldName;
        this.keyFieldName = keyFieldName;
        this.chatSerializerFieldName = chatSerializerFieldName;
        this.offHand = offHand;
        this.scoreboardTags = scoreboardTags;
    }

    public boolean hasOffHand() {
        return offHand;
    }

    public boolean supportsScoreboardTags() {
        return scoreboardTags;
    }

    boolean isInvulnerable(ArmorStand as) {
        return as.isInvulnerable();
    }

    void setInvulnerable(ArmorStand as, boolean invulnerable) {
        as.setInvulnerable(invulnerable);
    }

    boolean toggleInvulnerability(ArmorStand as) {
        boolean isInvulnerable = !isInvulnerable(as);
        setInvulnerable(as, isInvulnerable);
        return isInvulnerable;
    }

    boolean toggleSlotsDisabled(ArmorStand as) {
        boolean slotsDisabled = getDisabledSlots(as) == 0;
        setSlotsDisabled(as, slotsDisabled);
        return slotsDisabled;
    }

    int getDisabledSlots(ArmorStand as) {
        Object nmsEntity = getNmsEntity(as);
        if(nmsEntity == null) return 0;
        Field f;
        try {
            f = nmsEntity.getClass().getDeclaredField(disabledSlotsFieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return 0;
        }
        f.setAccessible(true);
        try {
            return (Integer) f.get(nmsEntity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    void setSlotsDisabled(ArmorStand as, boolean slotsDisabled) {
        Object nmsEntity = getNmsEntity(as);
        if (nmsEntity == null) return;
        Field f;
        try {
            f = nmsEntity.getClass().getDeclaredField(disabledSlotsFieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }
        f.setAccessible(true);
        try {
            f.set(nmsEntity, slotsDisabled ? 0xFFFFFF : 0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    boolean equipmentLocked(ArmorStand as) {
        return getDisabledSlots(as) == 0xFFFFFF;
    }

    Object getNmsEntity(org.bukkit.entity.Entity entity) {
        try {
            return entity.getClass().getMethod("getHandle").invoke(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getItemStackTags(ItemStack is) {
        if(is == null) {
            return "";
        }
        StringBuilder tags = new StringBuilder("");
        if(is.getItemMeta() != null && is.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta armorMeta = (LeatherArmorMeta) is.getItemMeta();
            tags.append("display:{color:");
            tags.append(armorMeta.getColor().asRGB());
            tags.append("}");
        }
        Map<Enchantment, Integer> enchants = is.getEnchantments();
        if(enchants.size() > 0) {
            if(tags.length() > 0) {
                tags.append(",");
            }
            tags.append("ench:[");

            for(Enchantment e : enchants.keySet()) {
                tags.append("{id:");
                //noinspection deprecation
                tags.append(e.getId());
                tags.append(",lvl:");
                tags.append(enchants.get(e));
                tags.append("},");
            }

            tags.setCharAt(tags.length() - 1, ']');
        }
        return tags.length() == 0 ? "" : (",tag:{" + tags.toString() + "}");
    }

    private String skullOwner(ItemStack is) {
        if(is == null || is.getItemMeta() == null || !(is.getItemMeta() instanceof SkullMeta)) {
            return "";
        }
        SkullMeta skull = (SkullMeta) is.getItemMeta();
        if(skull.hasOwner()) {
            return ",tag:{SkullOwner:\"" + skull.getOwner() + "\"}";
        } else {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    private String getNmsName(Material m) {
        try {
            Class block = getNMSClass("Block");
            Class item = getNMSClass("Item");
            Class registryBlocks = getNMSClass("RegistryBlocks");
            Class registryMaterials = getNMSClass("RegistryMaterials");
            Class regKey = getNMSClass("MinecraftKey");
            Object registry = block.getDeclaredField("REGISTRY").get(null);
            Set<Object> set = (Set<Object>) registry.getClass().getMethod("keySet").invoke(registry);
            for(Object key : set) {
                Object b = registryBlocks.getMethod("get", Object.class).invoke(registry, key);
                Integer id = (Integer) block.getMethod("getId", block).invoke(null, b);
                //noinspection deprecation
                if(id == m.getId()) {
                    return (String) regKey.getMethod(keyFieldName).invoke(key);
                }
            }
            registry = item.getDeclaredField("REGISTRY").get(null);
            set = (Set<Object>) registry.getClass().getMethod("keySet").invoke(registry);
            for(Object key : set) {
                Object i = registryMaterials.getMethod("get", Object.class).invoke(registry, key);
                Integer id = (Integer) item.getMethod("getId", item).invoke(null, i);
                //noinspection deprecation
                if(id == m.getId()) {
                    return (String) regKey.getMethod(keyFieldName).invoke(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    void openSign(Player p, Block b) {
        try {
            Object world = b.getWorld().getClass().getMethod("getHandle").invoke(b.getWorld());
            Object blockPos = getNMSClass("BlockPosition").getConstructor(int.class, int.class, int.class).newInstance(b.getX(), b.getY(), b.getZ());
            Object sign = world.getClass().getMethod("getTileEntity", getNMSClass("BlockPosition")).invoke(world, blockPos);
            Object player = p.getClass().getMethod("getHandle").invoke(p);
            player.getClass().getMethod("openSign", getNMSClass("TileEntitySign")).invoke(player, sign);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        if(nmsClassString.equals("ChatSerializer")) {
            nmsClassString = chatSerializerFieldName;
        }
        return Class.forName("net.minecraft.server." + nmsVersion + "." + nmsClassString);
    }

    void sendPacket(Player p, Object packet) {
        try {
            Object player = p.getClass().getMethod("getHandle").invoke(p);
            Object connection = player.getClass().getField("playerConnection").get(player);
            connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void actionBarMsg(Player p, String msg) {
        try {
            Object chat = getNMSClass("ChatSerializer").getMethod("a", String.class).invoke(null, "{\"text\":\"" + msg + "\",\"color\":\"green\"}");
            Object packet = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent"), byte.class).newInstance(chat, (byte) 2);
            sendPacket(p, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void generateCmdBlock(Location l, ArmorStand as) {
        if(offHand) {
            generateCmdBlockWithOffHand(l, as);
        } else {
            generateCmdBlockWithoutOffHand(l, as);
        }
    }

    private void generateCmdBlockWithOffHand(Location l, ArmorStand as) {
        Location loc = as.getLocation();
        int dSlots = getDisabledSlots(as);
        String hand, boots, legs, chest, helm, offHand;
        int handDmg, offHandDmg;
        hand = as.getEquipment().getItemInMainHand() == null ? "air" : getNmsName(as.getEquipment().getItemInMainHand().getType());
        offHand = as.getEquipment().getItemInOffHand() == null ? "air" : getNmsName(as.getEquipment().getItemInOffHand().getType());
        boots = as.getBoots() == null ? "air" : getNmsName(as.getBoots().getType());
        legs = as.getLeggings() == null ? "air" : getNmsName(as.getLeggings().getType());
        chest = as.getChestplate() == null ? "air" : getNmsName(as.getChestplate().getType());
        helm = as.getHelmet() == null ? "air" : getNmsName(as.getHelmet().getType());
        handDmg = as.getEquipment().getItemInMainHand() == null ? 0 : as.getEquipment().getItemInMainHand().getDurability();
        offHandDmg = as.getEquipment().getItemInOffHand() == null ? 0 : as.getEquipment().getItemInOffHand().getDurability();

        int bootsDmg = as.getBoots() == null ? 0 : as.getBoots().getDurability();
        int legsDmg = as.getLeggings() == null ? 0 : as.getLeggings().getDurability();
        int chestDmg = as.getChestplate() == null ? 0 : as.getChestplate().getDurability();
        int helmDmg = as.getHelmet() == null ? 0 : as.getHelmet().getDurability();
        EulerAngle he = as.getHeadPose();
        EulerAngle ll = as.getLeftLegPose();
        EulerAngle rl = as.getRightLegPose();
        EulerAngle la = as.getLeftArmPose();
        EulerAngle ra = as.getRightArmPose();
        EulerAngle bo = as.getBodyPose();
        String cmd = "summon " + summonEntityName + " " + Utils.twoDec(loc.getX()) + " " + Utils.twoDec(loc.getY()) + " " + Utils.twoDec(loc.getZ()) + " {"
                + (as.isVisible() ? "" : "Invisible:1,")
                + (as.hasBasePlate() ? "" : "NoBasePlate:1,")
                + (as.hasGravity() ? "" : "NoGravity:1,")
                + (as.hasArms() ? "ShowArms:1," : "")
                + (as.isSmall() ? "Small:1," : "")
                + (isInvulnerable(as) ? "Invulnerable:1," : "")
                + (dSlots == 0 ? "" : ("DisabledSlots:" + dSlots + ","))
                + (as.isCustomNameVisible() ? "CustomNameVisible:1," : "")
                + (as.getCustomName() == null ? "" : ("CustomName:\"" + as.getCustomName() + "\","))
                + (loc.getYaw() == 0F ? "" : ("Rotation:[" + Utils.twoDec(loc.getYaw()) + "f],"))
                + (as.getBoots() == null && as.getLeggings() == null && as.getChestplate() == null && as.getHelmet() == null ? "" : (
                "ArmorItems:["
                        + "{id:" + boots + ",Count:" + as.getBoots().getAmount() + ",Damage:" + bootsDmg + getItemStackTags(as.getBoots()) + "},"
                        + "{id:" + legs + ",Count:" + as.getLeggings().getAmount() + ",Damage:" + legsDmg + getItemStackTags(as.getLeggings()) + "},"
                        + "{id:" + chest + ",Count:" + as.getChestplate().getAmount() + ",Damage:" + chestDmg + getItemStackTags(as.getChestplate()) + "},"
                        + "{id:" + helm + ",Count:" + as.getHelmet().getAmount() + ",Damage:" + helmDmg + getItemStackTags(as.getHelmet()) + skullOwner(as.getHelmet()) + "}],"))
                + (as.getEquipment().getItemInMainHand() == null && as.getEquipment().getItemInOffHand() == null ? "" : (
                "HandItems:["
                        + "{id:" + hand + ",Count:" + as.getEquipment().getItemInMainHand().getAmount() + ",Damage:" + handDmg + getItemStackTags(as.getEquipment().getItemInMainHand()) + "},"
                        + "{id:" + offHand + ",Count:" + as.getEquipment().getItemInOffHand().getAmount() + ",Damage:" + offHandDmg + getItemStackTags(as.getEquipment().getItemInOffHand()) + "}],"))
                + "Pose:{"
                + (bo.equals(zero) ? "" : ("Body:[" + Utils.angle(bo.getX()) + "f," + Utils.angle(bo.getY()) + "f," + Utils.angle(bo.getZ()) + "f],"))
                + (he.equals(zero) ? "" : ("Head:[" + Utils.angle(he.getX()) + "f," + Utils.angle(he.getY()) + "f," + Utils.angle(he.getZ()) + "f],"))
                + (ll.equals(zero) ? "" : ("LeftLeg:[" + Utils.angle(ll.getX()) + "f," + Utils.angle(ll.getY()) + "f," + Utils.angle(ll.getZ()) + "f],"))
                + (rl.equals(zero) ? "" : ("RightLeg:[" + Utils.angle(rl.getX()) + "f," + Utils.angle(rl.getY()) + "f," + Utils.angle(rl.getZ()) + "f],"))
                + (la.equals(zero) ? "" : ("LeftArm:[" + Utils.angle(la.getX()) + "f," + Utils.angle(la.getY()) + "f," + Utils.angle(la.getZ()) + "f],"))
                + "RightArm:[" + Utils.angle(ra.getX()) + "f," + Utils.angle(ra.getY()) + "f," + Utils.angle(ra.getZ()) + "f]}}";
        Block b = l.getBlock();
        b.setType(Material.COMMAND);
        //noinspection deprecation
        b.setData((byte) 0);
        CommandBlock cb = (CommandBlock) b.getState();
        cb.setCommand(cmd);
        cb.update();
    }

    @SuppressWarnings("deprecation")
    private void generateCmdBlockWithoutOffHand(Location l, ArmorStand as) {
        Location loc = as.getLocation();
        int dSlots = Main.nms.getDisabledSlots(as);
        String hand, boots, legs, chest, helm;
        int handDmg;

        hand = getItemInMainHand(as) == null ? "0" : String.valueOf(getItemInMainHand(as).getTypeId());
        boots = as.getBoots() == null ? "0" : String.valueOf(as.getBoots().getTypeId());
        legs = as.getLeggings() == null ? "0" : String.valueOf(as.getLeggings().getTypeId());
        chest = as.getChestplate() == null ? "0" : String.valueOf(as.getChestplate().getTypeId());
        helm = as.getHelmet() == null ? "0" : String.valueOf(as.getHelmet().getTypeId());
        handDmg = getItemInMainHand(as) == null ? 0 : getItemInMainHand(as).getDurability();

        int bootsDmg = as.getBoots() == null ? 0 : as.getBoots().getDurability();
        int legsDmg = as.getLeggings() == null ? 0 : as.getLeggings().getDurability();
        int chestDmg = as.getChestplate() == null ? 0 : as.getChestplate().getDurability();
        int helmDmg = as.getHelmet() == null ? 0 : as.getHelmet().getDurability();
        EulerAngle he = as.getHeadPose();
        EulerAngle ll = as.getLeftLegPose();
        EulerAngle rl = as.getRightLegPose();
        EulerAngle la = as.getLeftArmPose();
        EulerAngle ra = as.getRightArmPose();
        EulerAngle bo = as.getBodyPose();
        String cmd = "summon ArmorStand " + Utils.twoDec(loc.getX()) + " " + Utils.twoDec(loc.getY()) + " " + Utils.twoDec(loc.getZ()) + " {"
                + (as.isVisible() ? "" : "Invisible:1,")
                + (as.hasBasePlate() ? "" : "NoBasePlate:1,")
                + (as.hasGravity() ? "" : "NoGravity:1,")
                + (as.hasArms() ? "ShowArms:1," : "")
                + (as.isSmall() ? "Small:1," : "")
                + (Main.nms.isInvulnerable(as) ? "Invulnerable:1," : "")
                + (dSlots == 0 ? "" : ("DisabledSlots:" + dSlots + ","))
                + (as.isCustomNameVisible() ? "CustomNameVisible:1," : "")
                + (as.getCustomName() == null ? "" : ("CustomName:\"" + as.getCustomName() + "\","))
                + (loc.getYaw() == 0F ? "" : ("Rotation:[" + Utils.twoDec(loc.getYaw()) + "f],"))
                + (getItemInMainHand(as) == null && as.getBoots() == null && as.getLeggings() == null && as.getChestplate() == null && as.getHelmet() == null ? "" : (
                "Equipment:["
                        + "{id:" + hand + ",Count:" + getItemInMainHand(as).getAmount() + ",Damage:" + handDmg + getItemStackTags(getItemInMainHand(as)) + "},"
                        + "{id:" + boots + ",Count:" + as.getBoots().getAmount() + ",Damage:" + bootsDmg + getItemStackTags(as.getBoots()) + "},"
                        + "{id:" + legs + ",Count:" + as.getLeggings().getAmount() + ",Damage:" + legsDmg + getItemStackTags(as.getLeggings()) + "},"
                        + "{id:" + chest + ",Count:" + as.getChestplate().getAmount() + ",Damage:" + chestDmg + getItemStackTags(as.getChestplate()) + "},"
                        + "{id:" + helm + ",Count:" + as.getHelmet().getAmount() + ",Damage:" + helmDmg + getItemStackTags(as.getHelmet()) + skullOwner(as.getHelmet()) + "}],"))
                + "Pose:{"
                + (bo.equals(zero) ? "" : ("Body:[" + Utils.angle(bo.getX()) + "f," + Utils.angle(bo.getY()) + "f," + Utils.angle(bo.getZ()) + "f],"))
                + (he.equals(zero) ? "" : ("Head:[" + Utils.angle(he.getX()) + "f," + Utils.angle(he.getY()) + "f," + Utils.angle(he.getZ()) + "f],"))
                + (ll.equals(zero) ? "" : ("LeftLeg:[" + Utils.angle(ll.getX()) + "f," + Utils.angle(ll.getY()) + "f," + Utils.angle(ll.getZ()) + "f],"))
                + (rl.equals(zero) ? "" : ("RightLeg:[" + Utils.angle(rl.getX()) + "f," + Utils.angle(rl.getY()) + "f," + Utils.angle(rl.getZ()) + "f],"))
                + (la.equals(zero) ? "" : ("LeftArm:[" + Utils.angle(la.getX()) + "f," + Utils.angle(la.getY()) + "f," + Utils.angle(la.getZ()) + "f],"))
                + "RightArm:[" + Utils.angle(ra.getX()) + "f," + Utils.angle(ra.getY()) + "f," + Utils.angle(ra.getZ()) + "f]}}";
        Block b = l.getBlock();
        b.setType(Material.COMMAND);
        b.setData((byte) 0);
        CommandBlock cb = (CommandBlock) b.getState();
        cb.setCommand(cmd);
        cb.update();
    }

    ArmorStand clone(ArmorStand as) {
        ArmorStand clone = (ArmorStand) as.getWorld().spawnEntity(as.getLocation().add(1, 0, 0), EntityType.ARMOR_STAND);
        clone.setGravity(as.hasGravity());
        clone.setHelmet(as.getHelmet());
        clone.setChestplate(as.getChestplate());
        clone.setLeggings(as.getLeggings());
        clone.setBoots(as.getBoots());
        setItemInMainHand(clone, getItemInMainHand(as));
        if(offHand) {
            setItemInOffHand(clone, getItemInOffHand(as));
        }
        clone.setHeadPose(as.getHeadPose());
        clone.setBodyPose(as.getBodyPose());
        clone.setLeftArmPose(as.getLeftArmPose());
        clone.setRightArmPose(as.getRightArmPose());
        clone.setLeftLegPose(as.getLeftLegPose());
        clone.setRightLegPose(as.getRightLegPose());
        clone.setVisible(as.isVisible());
        clone.setBasePlate(as.hasBasePlate());
        clone.setArms(as.hasArms());
        clone.setCustomName(as.getCustomName());
        clone.setCustomNameVisible(as.isCustomNameVisible());
        clone.setSmall(as.isSmall());
        setSlotsDisabled(clone, getDisabledSlots(as) == 0xFFFFFF);
        setInvulnerable(clone, isInvulnerable(as));
        if(Main.nms.supportsScoreboardTags()) {
            ArmorStandCmd.cloneASCommand(as, clone);
        }
        return clone;
    }

    ItemStack getItemInMainHand(Player p) {
        if(offHand) {
            return p.getInventory().getItemInMainHand();
        } else {
            //noinspection deprecation
            return p.getInventory().getItemInHand();
        }
    }

    ItemStack getItemInMainHand(ArmorStand as) {
        if(offHand) {
            return as.getEquipment().getItemInMainHand();
        } else {
            //noinspection deprecation
            return as.getEquipment().getItemInHand();
        }
    }

    void setItemInMainHand(ArmorStand as, ItemStack is) {
        if(offHand) {
            as.getEquipment().setItemInMainHand(is);
        } else {
            //noinspection deprecation
            as.getEquipment().setItemInHand(is);
        }
    }

    ItemStack getItemInOffHand(ArmorStand as) {
        if(!offHand) return null;
        return as.getEquipment().getItemInOffHand();
    }

    void setItemInOffHand(ArmorStand as, ItemStack is) {
        if(!offHand) return;
        as.getEquipment().setItemInOffHand(is);
    }

    Object getTag(Object nmsEntity) {

        try {
            Method method = nmsEntity.getClass().getMethod("getNBTTag");
            Object tag = method.invoke(nmsEntity);
            if(tag == null) {
                tag = getNMSClass("NBTTagCompound").newInstance();
            }
            method = nmsEntity.getClass().getMethod("c", getNMSClass("NBTTagCompound"));
            method.invoke(nmsEntity, tag);
            return tag;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    boolean getInvulnerableBoolean(Object tag) {
        try {
            return (Boolean) tag.getClass().getMethod("getBoolean", String.class).invoke(tag, "Invulnerable");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    void setInvulnerableBoolean(Object tag, boolean value) {
        try {
            tag.getClass().getMethod("setBoolean", String.class, boolean.class).invoke(tag, "Invulnerable", value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void saveTagF(Object nmsEntity, Object tag) {
        try {
            nmsEntity.getClass().getMethod("f", getNMSClass("NBTTagCompound")).invoke(nmsEntity, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean addScoreboardTag(ArmorStand as, String tag) {
        return as.addScoreboardTag(tag);
    }

    void removeScoreboardTag(ArmorStand as, String tag) {
        as.removeScoreboardTag(tag);
    }

    Set<String> getScoreboardTags(ArmorStand as) {
        return as.getScoreboardTags();
    }

}

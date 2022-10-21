package com.blank038.servermarket.nms;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * @author Blank038
 * @since 1.3.9-SNAPSHOT
 */
public abstract class BaseNMSControl {
    protected final String version;
    protected Class<?> craftItemStack, nmsItem, nbtClass;

    public BaseNMSControl() {
        this.version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            this.craftItemStack = Class.forName(String.format("org.bukkit.craftbukkit.%s.inventory.CraftItemStack", this.version));
            this.nmsItem = Class.forName(String.format("net.minecraft.server.%s.ItemStack", this.version));
            this.nbtClass = Class.forName(String.format("net.minecraft.server.%s.NBTTagCompound", this.version));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public abstract ItemStack addNbt(ItemStack itemStack, String key, String value);

    public abstract boolean hasKey(ItemStack itemStack, String key);

    public abstract String getValue(ItemStack itemStack, String key);

    public String getVersion() {
        return this.version;
    }

    public abstract String getImplName();
}

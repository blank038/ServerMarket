package com.blank038.servermarket.nms.impl;

import com.blank038.servermarket.nms.BaseNMSControl;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Blank038
 */
public class DefaultNMSControlImpl extends BaseNMSControl {
    private Method refNMSCopy, refBukkitCopy, refGetTag, refHasKey, refGetStr, refSetStr, refSetTag;

    public DefaultNMSControlImpl() {
        super();
        try {
            this.refNMSCopy = this.craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
            this.refBukkitCopy = this.craftItemStack.getDeclaredMethod("asBukkitCopy", this.nmsItem);
            this.refGetTag = this.nmsItem.getMethod("getTag");
            this.refHasKey = this.nbtClass.getMethod("hasKey", String.class);
            this.refGetStr = this.nbtClass.getMethod("getString", String.class);
            this.refSetStr = this.nbtClass.getMethod("setString", String.class, String.class);
            this.refSetTag = this.nmsItem.getMethod("setTag", this.nbtClass);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ItemStack addNbt(ItemStack itemStack, String key, String value) {
        try {
            Object nmsItem = this.refNMSCopy.invoke(null, itemStack);
            Object nbt = this.refGetTag.invoke(nmsItem);
            if (nbt == null) {
                nbt = this.nbtClass.newInstance();
            }
            this.refSetStr.invoke(nbt, key, value);
            this.refSetTag.invoke(nmsItem, nbt);
            return (ItemStack) this.refBukkitCopy.invoke(null, nmsItem);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return itemStack;
    }

    @Override
    public boolean hasKey(ItemStack itemStack, String key) {
        try {
            Object nmsItem = this.refNMSCopy.invoke(null, itemStack);
            Object nbt = this.refGetTag.invoke(nmsItem);
            if (nbt == null) {
                return false;
            }
            return (boolean) this.refHasKey.invoke(nbt, key);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getValue(ItemStack itemStack, String key) {
        try {
            Object nmsItem = this.refNMSCopy.invoke(null, itemStack);
            Object nbt = this.refGetTag.invoke(nmsItem);
            if (nbt == null) {
                return null;
            }
            return (String) this.refGetStr.invoke(nbt, key);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getImplName() {
        return "default";
    }
}

package com.blank038.servermarket.internal.util;

import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Blank038
 */
public class ItemUtil {

    public static ItemStack generateItem(String material, int amount, short data, int customData) {
        ItemStack itemStack = new ItemStack(Material.valueOf(material.toUpperCase()), amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            ((Damageable) itemMeta).setDamage(data);
            if (customData != -1) {
                itemMeta.setCustomModelData(customData);
            }
        } else {
            itemStack.setDurability(data);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}

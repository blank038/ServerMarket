package com.blank038.servermarket.util;

import org.bukkit.inventory.ItemStack;

/**
 * @author Blank038
 */
public class ItemUtil {

    public static boolean isSimilar(ItemStack itemStack, String filter) {
        if (itemStack == null) {
            return false;
        }
        String latestFilter = filter.toLowerCase();
        if (itemStack.getType().name().toLowerCase().contains(latestFilter)) {
            return true;
        }
        if (itemStack.hasItemMeta()) {
            return (itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().toLowerCase().contains(latestFilter))
                    || (itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore().stream().anyMatch((s) -> s.toLowerCase().contains(latestFilter)));
        }
        return false;
    }
}

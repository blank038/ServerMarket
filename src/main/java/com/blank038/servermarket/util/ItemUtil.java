package com.blank038.servermarket.util;

import com.blank038.servermarket.ServerMarket;
import org.bukkit.Material;
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

    public static Material valueOf(String type) {
        try {
            return Material.valueOf(type.toUpperCase());
        } catch (Exception ignored) {
            ServerMarket.getInstance().getConsoleLogger().log("Loaded material error: " + type);
            return Material.STONE;
        }
    }
}

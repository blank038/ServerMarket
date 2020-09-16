package com.blank038.servermarket.nms;

import org.bukkit.inventory.ItemStack;

public interface NBTBase {

    String get(ItemStack itemStack, String key);

    boolean contains(ItemStack itemStack, String key);
}

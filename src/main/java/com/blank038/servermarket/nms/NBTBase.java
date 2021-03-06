package com.blank038.servermarket.nms;

import org.bukkit.inventory.ItemStack;

/**
 * @author Blank038
 */
public interface NBTBase {

    /**
     * 获取物品 NBT 数据
     *
     * @param itemStack 目标物品
     * @param key       目标键
     * @return 目标值
     */
    String get(ItemStack itemStack, String key);

    /**
     * 判断物品是否有指定键
     *
     * @param itemStack 目标物品
     * @param key       目标键
     * @return 是否含有
     */
    boolean contains(ItemStack itemStack, String key);

    /**
     * 增加物品 NBT 数据
     *
     * @param itemStack 目标物品
     * @param key       目标键
     * @param value     目标值
     * @return 操作后物品
     */
    ItemStack addTag(ItemStack itemStack, String key, String value);
}

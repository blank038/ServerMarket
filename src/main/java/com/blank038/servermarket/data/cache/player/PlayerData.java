package com.blank038.servermarket.data.cache.player;

import com.blank038.servermarket.data.cache.sale.SaleItem;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
@Getter
public class PlayerData {
    private final Map<String, ItemStack> storeItems = new HashMap<>();
    private final List<String> records;
    private final UUID ownerUniqueId;

    public PlayerData(UUID uuid, FileConfiguration data) {
        this.ownerUniqueId = uuid;
        records = data.getStringList("info");
        // 读取物品
        if (data.contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                storeItems.put(key, data.getItemStack("items." + key));
            }
        }
    }

    /**
     * 判断暂存库是否有物品
     *
     * @param uuid 物品编号
     * @return 是否拥有
     */
    public boolean hasStoretem(String uuid) {
        return storeItems.containsKey(uuid);
    }

    /**
     * 从暂存箱中移除某个物品
     *
     * @param uuid 物品编号
     * @return 移除的物品
     */
    public ItemStack removeStoreItem(String uuid) {
        return storeItems.remove(uuid);
    }

    /**
     * 增加暂存物品至暂存箱
     *
     * @param itemStack 增加的物品
     */
    public void addStoreItem(ItemStack itemStack) {
        storeItems.put(UUID.randomUUID().toString(), itemStack.clone());
    }

    /**
     * 增加物品至暂存箱且增加购买记录
     *
     * @param saleItem 购买的商品
     */
    public void addStoreItem(SaleItem saleItem) {
        ItemStack itemStack = saleItem.getSaleItem();
        storeItems.put(UUID.randomUUID().toString(), itemStack.clone());
        String displayMmae = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                itemStack.getItemMeta().getDisplayName() : itemStack.getType().name();
        records.add(saleItem.getOwnerName() + "//" + saleItem.getPrice() + "//" + System.currentTimeMillis() + "//" +
                displayMmae + "//" + itemStack.getAmount() + "//" + saleItem.getSaleUUID());
    }
}
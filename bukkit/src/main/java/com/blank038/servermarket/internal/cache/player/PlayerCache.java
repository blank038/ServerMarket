package com.blank038.servermarket.internal.cache.player;

import com.blank038.servermarket.api.event.PlayerStoreItemAddEvent;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
@Getter
public class PlayerCache {
    private final Map<String, ItemStack> storeItems = new HashMap<>();
    private final UUID ownerUniqueId;
    @Setter
    private boolean newData;

    public PlayerCache(UUID uuid, FileConfiguration data) {
        this.ownerUniqueId = uuid;
        // 读取物品
        if (data.contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                storeItems.put(key, data.getItemStack("items." + key));
            }
        }
    }

    public PlayerCache(UUID uuid, FileConfiguration data, boolean newData) {
        this(uuid, data);
        this.newData = newData;
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
     * 从暂存箱中移除某个物品, Only YAML
     *
     * @param uuid 物品编号
     * @return 移除的物品
     */
    public ItemStack removeStoreItem(String uuid) {
        return storeItems.remove(uuid);
    }

    public Map<String, ItemStack> getStoreItems() {
        return new HashMap<>(storeItems);
    }

    /**
     * 增加暂存物品至暂存箱
     *
     * @param itemStack 增加的物品
     */
    public void addStoreItem(ItemStack itemStack, String reason) {
        // call PlayerStoreItemAddEvent
        PlayerStoreItemAddEvent event = new PlayerStoreItemAddEvent(this.getOwnerUniqueId(), itemStack, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getItemStack() != null) {
            storeItems.put(UUID.randomUUID().toString(), event.getItemStack().clone());
        }
    }

    /**
     * 增加物品至暂存箱且增加购买记录
     *
     * @param saleItem 购买的商品
     */
    public void addStoreItem(SaleCache saleItem, String reason) {
        // call PlayerStoreItemAddEvent
        PlayerStoreItemAddEvent event = new PlayerStoreItemAddEvent(this.getOwnerUniqueId(), saleItem.getSaleItem(), reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getItemStack() != null) {
            storeItems.put(UUID.randomUUID().toString(), event.getItemStack().clone());
        }
    }

    public FileConfiguration saveToConfiguration() {
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, ItemStack> entry : this.getStoreItems().entrySet()) {
            data.set("items." + entry.getKey(), entry.getValue());
        }
        return data;
    }
}
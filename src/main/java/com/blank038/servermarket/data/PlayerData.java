package com.blank038.servermarket.data;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.gui.SaleItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final HashMap<String, ItemStack> items = new HashMap<>();
    private final List<String> info;
    private final String name;

    public PlayerData(String name) {
        this.name = name;
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/data/", name + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 读取文件
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        info = data.getStringList("info");
        // 读取物品
        if (data.contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                items.put(key, data.getItemStack("items." + key));
            }
        }
    }

    /**
     * 获取玩家交易记录
     *
     * @return 交易记录
     */
    public List<String> getInfo() {
        return info;
    }

    /**
     * 获取暂存库的物品列表
     *
     * @return 物品列表
     */
    public HashMap<String, ItemStack> getItems() {
        return items;
    }

    /**
     * 判断暂存库是否有物品
     *
     * @param uuid 物品编号
     * @return 是否拥有
     */
    public boolean contains(String uuid) {
        return items.containsKey(uuid);
    }

    /**
     * 移除某个物品
     *
     * @param uuid 物品编号
     * @return 移除的物品
     */
    public ItemStack remove(String uuid) {
        return items.remove(uuid);
    }

    /**
     * 增加暂存物品至暂存箱
     *
     * @param itemStack 增加的物品
     */
    public void addItem(ItemStack itemStack) {
        items.put(UUID.randomUUID().toString(), itemStack.clone());
    }

    /**
     * 增加物品至暂存箱且增加购买记录
     *
     * @param saleItem 购买的商品
     */
    public void addItem(SaleItem saleItem) {
        ItemStack itemStack = saleItem.getSafeItem();
        items.put(UUID.randomUUID().toString(), itemStack.clone());
        String displayMmae = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                itemStack.getItemMeta().getDisplayName() : itemStack.getType().name();
        info.add(saleItem.getOwnerName() + "//" + saleItem.getPrice() + "//" + System.currentTimeMillis() + "//" +
                displayMmae + "//" + itemStack.getAmount() + "//" + saleItem.getSaleUUID());
    }

    /**
     * 保存玩家数据
     */
    public void save() {
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/data/", name + ".yml");
        FileConfiguration data = new YamlConfiguration();
        data.set("info", info);
        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            data.set("items." + entry.getKey(), entry.getValue());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package com.blank038.servermarket.data.handler.impl;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.cache.market.MarketConfigData;
import com.blank038.servermarket.data.cache.sale.SaleItem;
import com.blank038.servermarket.data.handler.AbstractStorageHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
public class YamlStorageHandlerImpl extends AbstractStorageHandler {

    @Override
    public void load(String market) {
        if (this.marketStorageDataMap.containsKey(market)) {
            this.save(market, this.marketStorageDataMap.get(market).getSales());
        }
        this.marketStorageDataMap.remove(market);
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/saleData/", market + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            SaleItem saleItem = new SaleItem(data.getConfigurationSection(key));
            this.marketStorageDataMap.get(market).addSale(saleItem.getSaleUUID(), saleItem);
        }
    }

    @Override
    public boolean hasSale(String market, String saleId) {
        if (this.marketStorageDataMap.containsKey(market)) {
            return this.marketStorageDataMap.get(market).hasSale(saleId);
        }
        return false;
    }

    @Override
    public SaleItem getSaleItem(String market, String saleId) {
        return null;
    }

    @Override
    public SaleItem removeSaleItem(String market, String saleId) {
        return null;
    }

    @Override
    public void addSale(String market, SaleItem saleItem) {

    }

    @Override
    public void save(String market, Map<String, SaleItem> map) {
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/saleData/", market + ".yml");
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, SaleItem> entry : map.entrySet()) {
            data.set(entry.getKey(), entry.getValue().toSection());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeTimeOutItem() {
        this.marketStorageDataMap.forEach((k, v) -> {
            MarketConfigData marketConfigData = MarketConfigData.MARKET_DATA.get(k);
            if (marketConfigData == null) {
                return;
            }
            // 开始计算
            Iterator<Map.Entry<String, SaleItem>> iterator = v.getSales().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SaleItem> entry = iterator.next();
                int second = (int) ((System.currentTimeMillis() - entry.getValue().getPostTime()) / 1000L);
                if (second >= marketConfigData.getEffectiveTime()) {
                    iterator.remove();
                    UUID uuid = UUID.fromString(entry.getValue().getOwnerUUID());
                    ServerMarket.getApi().addItem(uuid, entry.getValue().getSafeItem());
                }
            }
        });
    }

    @Override
    public void saveAll() {

    }
}

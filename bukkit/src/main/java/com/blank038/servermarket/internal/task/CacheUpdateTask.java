package com.blank038.servermarket.internal.task;

import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.handler.CacheHandler;
import com.blank038.servermarket.internal.plugin.ServerMarket;

import java.util.Map;

public class CacheUpdateTask implements Runnable {

    @Override
    public void run() {
        DataContainer.MARKET_DATA.forEach((k, v) -> {
            Map<String, SaleCache> map = ServerMarket.getStorageHandler().getSaleItemsByMarket(k);
            CacheHandler.uploadSales(k, map);
        });
    }
}

package com.blank038.servermarket.internal.task.impl;

import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.internal.config.GeneralOption;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.handler.CacheHandler;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.task.AbstractTask;

import java.util.Map;

public class CacheUpdateTask extends AbstractTask {

    @Override
    public long getDelay() {
        return GeneralOption.cacheUpdateInterval;
    }

    @Override
    public long getPeroid() {
        return GeneralOption.cacheUpdateInterval;
    }

    @Override
    public void run() {
        DataContainer.MARKET_DATA.forEach((k, v) -> {
            Map<String, SaleCache> map = ServerMarket.getStorageHandler().getSaleItemsByMarket(k);
            CacheHandler.uploadSales(k, map);
        });
    }
}

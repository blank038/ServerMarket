package com.blank038.servermarket.data.handler;

import com.blank038.servermarket.data.cache.market.MarketStorageData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Blank038
 */
public abstract class AbstractStorageHandler implements IStorageHandler {
    protected final Map<String, MarketStorageData> marketStorageDataMap = new HashMap<>();

    @Override
    public void load(String market) {
    }

    public MarketStorageData getMarketStorageData(String marketId) {
        return this.marketStorageDataMap.getOrDefault(marketId, null);
    }
}

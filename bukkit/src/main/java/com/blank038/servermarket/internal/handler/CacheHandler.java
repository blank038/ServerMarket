package com.blank038.servermarket.internal.handler;

import com.blank038.servermarket.internal.cache.sale.SaleCache;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class CacheHandler {
    private static final Map<String, MarketContainer> CONTAINER_MAP = new HashMap<>();

    public static void uploadSales(String market, Map<String, SaleCache> map) {
        CONTAINER_MAP.putIfAbsent(market, new MarketContainer());
        CONTAINER_MAP.get(market).uploadSales(map);
    }

    public static Map<String, SaleCache> querySales(String market) {
        if (CONTAINER_MAP.containsKey(market)) {
            return CONTAINER_MAP.get(market).getSaleCacheMap();
        }
        return new HashMap<>();
    }

    public static void removeSaleCache(String market, String saleId) {
        if (CONTAINER_MAP.containsKey(market)) {
            CONTAINER_MAP.get(market).removeSale(saleId);
        }
    }

    @Getter
    public static class MarketContainer {
        private final Map<String, SaleCache> saleCacheMap = new HashMap<>();

        public void uploadSales(Map<String, SaleCache> map) {
            this.saleCacheMap.clear();
            this.saleCacheMap.putAll(map);
        }

        public void removeSale(String uuid) {
            this.saleCacheMap.remove(uuid);
        }
    }
}

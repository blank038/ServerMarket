package com.blank038.servermarket.data.cache.market;

import com.blank038.servermarket.data.cache.sale.SaleCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * It's only used in YAML storage mode.
 *
 * @author Blank038
 */
public class MarketStorageData {
    private final String market;
    private final Map<String, SaleCache> saleMap = new HashMap<>();

    public MarketStorageData(String market) {
        this.market = market;
    }

    public boolean hasSale(String saleId) {
        return this.saleMap.containsKey(saleId);
    }

    public void addSale(String saleId, SaleCache saleItem) {
        this.saleMap.put(saleId, saleItem);
    }

    public void addSale(Map<String, SaleCache> saleMap) {
        this.saleMap.clear();
        this.saleMap.putAll(saleMap);
    }

    public Optional<SaleCache> getSale(String saleId) {
        if (this.saleMap.containsKey(saleId)) {
            return Optional.of(this.saleMap.get(saleId));
        }
        return Optional.empty();
    }

    public Optional<SaleCache> removeSale(String saleId) {
        if (this.saleMap.containsKey(saleId)) {
            return Optional.of(this.saleMap.remove(saleId));
        }
        return Optional.empty();
    }

    public Map<String, SaleCache> getSales() {
        return this.saleMap;
    }

    public String getMarketSourceId() {
        return this.market;
    }
}

package com.blank038.servermarket.api.event;

import com.blank038.servermarket.data.cache.market.MarketConfigData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Blank038
 */
public class MarketLoadEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final MarketConfigData marketData;

    public MarketLoadEvent(MarketConfigData marketData) {
        this.marketData = marketData;
    }

    public MarketConfigData getmarketData() {
        return  this.marketData;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

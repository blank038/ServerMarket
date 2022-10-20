package com.blank038.servermarket.api.event;

import com.blank038.servermarket.data.storage.MarketData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Blank038
 */
public class MarketLoadEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final MarketData MARKET_DATA;

    public MarketLoadEvent(MarketData marketData) {
        this.MARKET_DATA = marketData;
    }

    public MarketData getmarketData() {
        return  this.MARKET_DATA;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

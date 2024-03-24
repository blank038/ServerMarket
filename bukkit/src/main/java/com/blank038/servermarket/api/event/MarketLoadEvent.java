package com.blank038.servermarket.api.event;

import com.blank038.servermarket.api.entity.MarketData;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Blank038
 */
@Getter
public class MarketLoadEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final MarketData marketData;

    public MarketLoadEvent(MarketData marketData) {
        this.marketData = marketData;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

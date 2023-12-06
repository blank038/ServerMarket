package com.blank038.servermarket.api.event;

import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * @author Blank038
 */
@Getter
public class PlayerSaleEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final MarketData marketData;
    private final SaleCache saleCache;

    public PlayerSaleEvent(Player who, MarketData marketData, SaleCache cache) {
        super(who);
        this.marketData = marketData;
        this.saleCache = cache;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static class Buy extends PlayerSaleEvent {

        public Buy(Player who, MarketData marketData, SaleCache cache) {
            super(who, marketData, cache);
        }
    }

    public static class Sell extends PlayerSaleEvent {

        public Sell(Player who, MarketData marketData, SaleCache cache) {
            super(who, marketData, cache);
        }
    }
}

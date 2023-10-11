package com.blank038.servermarket.api.event;

import com.blank038.servermarket.data.cache.sale.SaleCache;
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

    private final SaleCache saleCache;

    public PlayerSaleEvent(Player who, SaleCache cache) {
        super(who);
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

        public Buy(Player who, SaleCache cache) {
            super(who, cache);
        }
    }

    public static class Sell extends PlayerSaleEvent {

        public Sell(Player who, SaleCache cache) {
            super(who, cache);
        }
    }
}

package com.blank038.servermarket.internal.listen.impl;

import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.api.event.PlayerSaleEvent;
import com.blank038.servermarket.internal.cache.other.SaleLog;
import com.blank038.servermarket.internal.enums.LogType;
import com.blank038.servermarket.internal.listen.AbstractListener;
import org.bukkit.event.EventHandler;

/**
 * @author Blank038
 */
public class CoreListener extends AbstractListener {

    @EventHandler
    public void onSaleSell(PlayerSaleEvent.Sell event) {
        SaleLog saleLog = SaleLog.builder()
                .logType(LogType.SELL)
                .sourceMarket(event.getMarketData().getSourceId())
                .triggerTime(System.currentTimeMillis())
                .saleCache(event.getSaleCache())
                .triggerPlayerUUID(event.getPlayer().getUniqueId())
                .build();
        ServerMarket.getStorageHandler().addLog(saleLog);
    }

    @EventHandler
    public void onSaleBuy(PlayerSaleEvent.Buy event) {
        SaleLog saleLog = SaleLog.builder()
                .logType(LogType.BUY)
                .sourceMarket(event.getMarketData().getSourceId())
                .triggerTime(System.currentTimeMillis())
                .saleCache(event.getSaleCache())
                .triggerPlayerUUID(event.getPlayer().getUniqueId())
                .build();
        ServerMarket.getStorageHandler().addLog(saleLog);
    }
}

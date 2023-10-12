package com.blank038.servermarket.listen.impl;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.api.event.PlayerSaleEvent;
import com.blank038.servermarket.data.cache.other.SaleLog;
import com.blank038.servermarket.enums.LogType;
import com.blank038.servermarket.listen.AbstractListener;
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

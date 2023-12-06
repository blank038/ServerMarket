package com.blank038.servermarket.internal.listen.impl;

import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.economy.BaseEconomy;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.internal.listen.AbstractListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Blank038
 * @date 2021/03/05
 */
public class PlayerCommonListener extends AbstractListener {

    /**
     * 玩家加入服务器事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ServerMarket.getStorageHandler().getOrLoadPlayerCache(player.getUniqueId(), false);
        // check player offline transactions
        Bukkit.getScheduler().runTaskAsynchronously(ServerMarket.getInstance(), () -> this.checkResult(player));
    }

    /**
     * 玩家离开服务器事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            ServerMarket.getStorageHandler().savePlayerData(uuid, true);
            return true;
        });
        future.exceptionally((e) -> false);
        future.thenAccept((v) -> {
            if (v) {
                ServerMarket.getStorageHandler().setLock(uuid, false);
            }
        });
    }

    private synchronized void checkResult(Player player) {
        ServerMarket.getStorageHandler().getOfflineTransactionByPlayer(player.getUniqueId()).forEach((k, v) -> {
            if (ServerMarket.getStorageHandler().removeOfflineTransaction(k)) {
                // 获取市场数据
                MarketData marketData = DataContainer.MARKET_DATA.getOrDefault(v.getSourceMarket(), null);
                // 获取可获得货币
                double price = v.getAmount(), last = marketData == null ? price : (price - price * marketData.getPermsValueForPlayer(marketData.getTaxSection(), player));
                // 判断货币桥是否存在
                if (BaseEconomy.PAY_TYPES.containsKey(v.getPayType())) {
                    DecimalFormat df = new DecimalFormat("#0.00");
                    BaseEconomy.getEconomyBridge(v.getPayType()).give(player, v.getEconomyType(), last);
                    player.sendMessage(I18n.getStrAndHeader("sale-sell")
                            .replace("%economy%", marketData == null ? "" : marketData.getDisplayName())
                            .replace("%money%", df.format(price)).replace("%last%", df.format(last)));
                }
            }
        });
    }
}

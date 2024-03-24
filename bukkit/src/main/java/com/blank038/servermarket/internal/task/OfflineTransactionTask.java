package com.blank038.servermarket.internal.task;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.api.platform.wrapper.ITaskWrapper;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.economy.BaseEconomy;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

/**
 * @author Blank038
 */
public class OfflineTransactionTask implements Runnable {
    private static ITaskWrapper taskWrapper;

    public OfflineTransactionTask() {
        ServerMarketApi.getPlatformApi().runTaskTimerAsynchronously(ServerMarket.getInstance(), () -> {

        }, 1200L, 1200L);
    }

    @Override
    public synchronized void run() {
        Bukkit.getOnlinePlayers().forEach(this::checkResult);
    }

    private void checkResult(Player player) {
        ServerMarket.getStorageHandler().getOfflineTransactionByPlayer(player.getUniqueId()).forEach((k, v) -> {
            if (ServerMarket.getStorageHandler().removeOfflineTransaction(k)) {
                // 获取市场数据
                MarketData marketData = DataContainer.MARKET_DATA.getOrDefault(v.getSourceMarket(), null);
                // 获取可获得货币
                double price = v.getAmount(), tax = 0;
                if (marketData != null) {
                    tax = price * marketData.getPermsValueForPlayer(marketData.getTaxSection(), player, false);
                }
                double last = price - tax;
                // send taxes
                ServerMarketApi.sendTaxes(v.getPayType(), v.getEconomyType(), tax);
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

    public static void restart() {
        if (taskWrapper != null) {
            taskWrapper.cancel();
        }
        int interval = ServerMarket.getInstance().getConfig().getInt("settings.offline-transaction-interval");
        taskWrapper = ServerMarketApi.getPlatformApi().runTaskTimerAsynchronously(
                ServerMarket.getInstance(),
                new OfflineTransactionTask(),
                interval,
                interval
        );
    }
}

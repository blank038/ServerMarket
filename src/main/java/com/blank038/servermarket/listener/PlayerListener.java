package com.blank038.servermarket.listener;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.bridge.BaseBridge;
import com.blank038.servermarket.i18n.I18n;
import com.blank038.servermarket.data.cache.market.MarketConfigData;
import com.blank038.servermarket.data.cache.player.PlayerData;
import com.blank038.servermarket.data.cache.sale.ResultData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Blank038
 * @date 2021/03/05
 */
public class PlayerListener implements Listener {

    /**
     * 玩家加入服务器事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData.PLAYER_DATA.put(event.getPlayer().getUniqueId(), new PlayerData(player.getUniqueId()));
        Bukkit.getScheduler().runTaskAsynchronously(ServerMarket.getInstance(), () -> this.checkResult(player));
    }

    /**
     * 玩家离开服务器事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (PlayerData.PLAYER_DATA.containsKey(event.getPlayer().getUniqueId())) {
            PlayerData.PLAYER_DATA.remove(event.getPlayer().getUniqueId()).save();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        for (Map.Entry<String, MarketConfigData> entry : MarketConfigData.MARKET_DATA.entrySet()) {
            if (entry.getValue().performSellCommand(event.getPlayer(), event.getMessage())) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private synchronized void checkResult(Player player) {
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, ResultData> entry : ResultData.RESULT_DATA.entrySet()) {
            if (entry.getValue().getOwnerUUID().equals(player.getPlayer().getUniqueId())) {
                // 获取市场数据
                MarketConfigData marketData = MarketConfigData.MARKET_DATA.getOrDefault(entry.getValue().getSourceMarket(), null);
                // 获取可获得货币
                double price = entry.getValue().getResultAmount(),
                        last = marketData == null ? price : marketData.getLastMoney(marketData.getTaxSection(), player, price);
                // 判断货币桥是否存在
                if (BaseBridge.PAY_TYPES.containsKey(entry.getValue().getPayType())) {
                    DecimalFormat df = new DecimalFormat("#0.00");
                    ServerMarket.getInstance().getEconomyBridge(entry.getValue().getPayType()).give(player, entry.getValue().getEconmyType(), last);
                    player.sendMessage(I18n.getString("sale-sell", true)
                            .replace("%economy%", marketData == null ? "" : marketData.getDisplayName())
                            .replace("%money%", df.format(price)).replace("%last%", df.format(last)));
                    keys.add(entry.getKey());
                }
            }
        }
        for (String key : keys) {
            ResultData.RESULT_DATA.remove(key);
        }
    }
}

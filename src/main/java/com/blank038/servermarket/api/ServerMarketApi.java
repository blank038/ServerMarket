package com.blank038.servermarket.api;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.DataContainer;
import com.blank038.servermarket.data.cache.market.MarketData;
import com.blank038.servermarket.data.cache.other.OfflineTransactionData;
import com.blank038.servermarket.enums.PayType;
import com.blank038.servermarket.filter.FilterBuilder;
import com.blank038.servermarket.gui.impl.MarketGui;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @author Blank038
 * @date 2021/03/05
 */
public class ServerMarketApi {
    private final ServerMarket plugin = ServerMarket.getInstance();

    public MarketData fuzzySearchMarketData(String key) {
        for (Map.Entry<String, MarketData> entry : DataContainer.MARKET_DATA.entrySet()) {
            if (entry.getKey().equals(key) || entry.getValue().getDisplayName().contains(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 打开市场, 如果市场编号为 null 则打开默认市场
     *
     * @param player 目标玩家
     * @param key    目标市场编号
     */
    public void openMarket(Player player, String key, int page, FilterBuilder filter) {
        MarketData marketData = DataContainer.MARKET_DATA.containsKey(key) ? DataContainer.MARKET_DATA.get(key)
                : DataContainer.MARKET_DATA.get(plugin.getConfig().getString("default-market"));
        if (marketData == null) {
            return;
        }
        if (player.hasPermission(marketData.getPermission())) {
            new MarketGui(marketData.getMarketKey(), page, filter).openGui(player);
        }
    }

    public void addOfflineTransaction(String uuid, PayType payType, String ectType, double moeny, String sourceMarket) {
        ConfigurationSection section = new YamlConfiguration();
        section.set("amount", moeny);
        section.set("pay-type", payType.name());
        section.set("owner-uuid", uuid);
        section.set("eco-type", ectType);
        section.set("source-market", sourceMarket);
        // 存入数据
        OfflineTransactionData resultData = new OfflineTransactionData(section);
        ServerMarket.getStorageHandler().addOfflineTransaction(resultData);
    }
}

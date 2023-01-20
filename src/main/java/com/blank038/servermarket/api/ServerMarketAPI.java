package com.blank038.servermarket.api;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.cache.market.MarketConfigData;
import com.blank038.servermarket.data.cache.player.PlayerData;
import com.blank038.servermarket.data.cache.sale.SaleItem;
import com.blank038.servermarket.filter.FilterBuilder;
import com.blank038.servermarket.gui.MarketGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 * @date 2021/03/05
 */
public class ServerMarketAPI {
    private final ServerMarket instance = ServerMarket.getInstance();

    public void addItem(UUID uuid, SaleItem saleItem) {
        PlayerData data = PlayerData.PLAYER_DATA.getOrDefault(uuid, new PlayerData(uuid));
        data.addItem(saleItem);
        data.save();
    }

    public void addItem(UUID uuid, ItemStack itemStack) {
        PlayerData data = PlayerData.PLAYER_DATA.getOrDefault(uuid, new PlayerData(uuid));
        data.addItem(itemStack);
        data.save();
    }

    public MarketConfigData fuzzySearchMarketData(String key) {
        for (Map.Entry<String, MarketConfigData> entry : MarketConfigData.MARKET_DATA.entrySet()) {
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
        MarketConfigData marketData = MarketConfigData.MARKET_DATA.containsKey(key) ? MarketConfigData.MARKET_DATA.get(key)
                : MarketConfigData.MARKET_DATA.get(instance.getConfig().getString("default-market"));
        if (marketData == null) {
            return;
        }
        if (player.hasPermission(marketData.getPermission())) {
            new MarketGui(marketData.getMarketKey(), page, filter).openGui(player);
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return PlayerData.PLAYER_DATA.getOrDefault(uuid, new PlayerData(uuid));
    }
}

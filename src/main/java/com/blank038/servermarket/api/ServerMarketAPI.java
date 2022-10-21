package com.blank038.servermarket.api;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.storage.MarketData;
import com.blank038.servermarket.data.cache.PlayerData;
import com.blank038.servermarket.data.sale.SaleItem;
import com.blank038.servermarket.filter.FilterBuilder;
import com.blank038.servermarket.gui.MarketGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author Blank038
 * @date 2021/03/05
 */
public class ServerMarketAPI {
    private final ServerMarket INSTANCE;

    public ServerMarketAPI(ServerMarket serverMarket) {
        this.INSTANCE = serverMarket;
    }

    public void addItem(UUID uuid, SaleItem saleItem) {
        PlayerData data = PlayerData.PLAYER_DATA.getOrDefault(uuid, new PlayerData(uuid));
        data.addItem(saleItem);
        data.save();
    }

    public void addItem(UUID uuid, ItemStack itemStack) {
        PlayerData data =  PlayerData.PLAYER_DATA.getOrDefault(uuid, new PlayerData(uuid));
        data.addItem(itemStack);
        data.save();
    }

    /**
     * 打开市场, 如果市场编号为 null 则打开默认市场
     *
     * @param player 目标玩家
     * @param key    目标市场编号
     */
    public void openMarket(Player player, String key, int page, FilterBuilder filter) {
        MarketData marketData = MarketData.MARKET_DATA.containsKey(key) ? MarketData.MARKET_DATA.get(key)
                : MarketData.MARKET_DATA.get(INSTANCE.getConfig().getString("default-market"));
        if (marketData == null) {
            return;
        }
        if (player.hasPermission(marketData.getPermission())) {
            new MarketGui(key, page, filter).openGui(player);
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return PlayerData.PLAYER_DATA.getOrDefault(uuid, new PlayerData(uuid));
    }
}

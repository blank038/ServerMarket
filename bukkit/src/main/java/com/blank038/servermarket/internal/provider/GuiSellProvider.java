package com.blank038.servermarket.internal.provider;

import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiSellProvider {
    private static final Map<UUID, String> GUI_SELL = new ConcurrentHashMap<>();

    public static void add(UUID uuid, String marketId) {
        GuiSearchProvider.remove(uuid);
        GUI_SELL.put(uuid, marketId);
    }

    public static void remove(UUID uuid) {
        GUI_SELL.remove(uuid);
    }

    public static boolean sell(Player player, String input) {
        String marketId = GUI_SELL.remove(player.getUniqueId());
        if (marketId == null) {
            return false;
        }
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(I18n.getStrAndHeader("sell-cancelled"));
            return true;
        }
        MarketData marketData = DataContainer.MARKET_DATA.get(marketId);
        if (marketData == null) {
            player.sendMessage(I18n.getStrAndHeader("market-error"));
            return true;
        }
        Bukkit.getScheduler().runTask(ServerMarket.getInstance(), () -> {
            SellHelper.performSell(player, marketData, input);
        });
        return true;
    }
}

package com.blank038.servermarket;

import com.blank038.servermarket.data.MarketData;
import com.blank038.servermarket.data.PlayerData;
import com.blank038.servermarket.data.gui.SaleItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Blank038
 * @date 2021/03/05
 */
public class ServerMarketAPI {
    private final ServerMarket INSTANCE;

    public ServerMarketAPI(ServerMarket serverMarket) {
        this.INSTANCE = serverMarket;
    }

    public void addItem(String name, SaleItem saleItem) {
        PlayerData data = INSTANCE.datas.getOrDefault(name, new PlayerData(name));
        data.addItem(saleItem);
        data.save();
    }

    public void addItem(String name, ItemStack itemStack) {
        PlayerData data = INSTANCE.datas.getOrDefault(name, new PlayerData(name));
        data.addItem(itemStack);
        data.save();
    }

    /**
     * 打开市场, 如果市场编号为 null 则打开默认市场
     *
     * @param player 目标玩家
     * @param key    目标市场编号
     */
    public void openMarket(Player player, String key) {
        MarketData marketData = MarketData.MARKET_DATA.containsKey(key) ? MarketData.MARKET_DATA.get(key)
                : MarketData.MARKET_DATA.get(INSTANCE.getConfig().getString("default-market"));
        if (marketData == null) {
            return;
        }
        if (player.hasPermission(marketData.getPermission())) {
            marketData.openGui(player, 1);
        }
    }

    public double getLastMoney(Player player, double money) {
        double tax = ServerMarket.getInstance().getConfig().getDouble("tax.default");
        for (String key : ServerMarket.getInstance().getConfig().getConfigurationSection("tax").getKeys(false)) {
            double tempTax = ServerMarket.getInstance().getConfig().getDouble("tax." + key);
            if (player.hasPermission("servermarket.tax." + key) && tempTax < tax) {
                tax = tempTax;
            }
        }
        return money - money * tax;
    }

    public PlayerData getPlayerData(String name) {
        return INSTANCE.datas.getOrDefault(name, new PlayerData(name));
    }
}

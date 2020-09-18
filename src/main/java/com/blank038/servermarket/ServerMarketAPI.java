package com.blank038.servermarket;

import com.blank038.servermarket.data.PlayerData;
import org.bukkit.inventory.ItemStack;

public class ServerMarketAPI {
    private final ServerMarket serverMarket;

    public ServerMarketAPI(ServerMarket serverMarket) {
        this.serverMarket = serverMarket;
    }

    public void addItem(String name, ItemStack itemStack) {
        PlayerData data = serverMarket.datas.getOrDefault(name, new PlayerData(name));
        data.addItem(itemStack);
        data.save();
    }

    public PlayerData getPlayerData(String name) {
        return serverMarket.datas.getOrDefault(name, new PlayerData(name));
    }
}

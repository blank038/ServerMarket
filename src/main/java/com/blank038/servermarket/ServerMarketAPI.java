package com.blank038.servermarket;

import com.blank038.servermarket.data.PlayerData;
import com.blank038.servermarket.data.gui.SaleItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ServerMarketAPI {
    private final ServerMarket serverMarket;

    public ServerMarketAPI(ServerMarket serverMarket) {
        this.serverMarket = serverMarket;
    }

    public void addItem(String name, SaleItem saleItem) {
        PlayerData data = serverMarket.datas.getOrDefault(name, new PlayerData(name));
        data.addItem(saleItem);
        data.save();
    }

    public void addItem(String name, ItemStack itemStack) {
        PlayerData data = serverMarket.datas.getOrDefault(name, new PlayerData(name));
        data.addItem(itemStack);
        data.save();
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
        return serverMarket.datas.getOrDefault(name, new PlayerData(name));
    }

    public List<Integer> getInt(List<String> list) {
        List<Integer> integers = new ArrayList<>();
        for (String i : list) {
            if (i.contains("-")) {
                String[] split = i.split("-");
                int min = Integer.parseInt(split[0]), max = Integer.parseInt(split[1]);
                for (int x = min; x <= max; x++) {
                    if (!integers.contains(x)) {
                        integers.add(x);
                    }
                }
            } else if (i.contains(",")) {
                String[] split = i.split(",");
                for (String x : split) {
                    integers.add(Integer.parseInt(x));
                }
            } else {
                integers.add(Integer.parseInt(i));
            }
        }
        return integers;
    }
}

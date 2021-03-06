package com.blank038.servermarket.bridge;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.enums.PayType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;

/**
 * @author Blank038
 */
public abstract class BaseBridge {
    public static final HashMap<PayType, BaseBridge> PAY_TYPES = new HashMap<>();

    public BaseBridge(PayType payType) {
        PAY_TYPES.put(payType, this);
        ServerMarket.getInstance().log("&6 * &f挂钩货币: &e" + payType.getPlugin());
    }

    /**
     * 查询余额
     *
     * @param player 目标玩家
     * @param key    货币名
     * @return 货币余额
     */
    public abstract double balance(OfflinePlayer player, String key);

    /**
     * 基于玩家货币
     *
     * @param player 目标玩家
     * @param amount 数量
     */
    public abstract void give(OfflinePlayer player, double amount);

    /**
     * 扣除玩家货币
     *
     * @param player 目标玩家
     * @param amount 数量
     * @return 是否成功
     */
    public abstract boolean take(OfflinePlayer player, double amount);

    public static <T> T register(Class<T> c) {
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化货币桥
     */
    public static void initBridge() {
        for (PayType type : PayType.values()) {
            if (Bukkit.getPluginManager().getPlugin(type.getPlugin()) != null) {
                BaseBridge.register(type.getBridgeClass());
            }
        }
    }
}
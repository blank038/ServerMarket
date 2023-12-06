package com.blank038.servermarket.internal.economy;

import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.enums.PayType;
import com.blank038.servermarket.internal.i18n.I18n;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public abstract class BaseEconomy {
    public static final Map<PayType, BaseEconomy> PAY_TYPES = new HashMap<>();

    public BaseEconomy(PayType payType) {
        PAY_TYPES.put(payType, this);
        ServerMarket.getInstance().getConsoleLogger().log(false,
                I18n.getProperties().getProperty("hook-economy").replace("%s", payType.getPlugin()));
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
     * @param key    货币类型
     * @param amount 数量
     */
    public abstract void give(OfflinePlayer player, String key, double amount);

    /**
     * 扣除玩家货币
     *
     * @param player 目标玩家
     * @param key    货币类型
     * @param amount 数量
     * @return 是否成功
     */
    public abstract boolean take(OfflinePlayer player, String key, double amount);

    public static <T extends BaseEconomy> T register(Class<T> c) {
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot register economy: " + c.getName());
        }
        return null;
    }

    /**
     * 初始化货币桥
     */
    public static void initEconomies() {
        if (PAY_TYPES.isEmpty()) {
            for (PayType type : PayType.values()) {
                if (Bukkit.getPluginManager().getPlugin(type.getPlugin()) != null) {
                    BaseEconomy.register(type.getBridgeClass());
                }
            }
        }
    }

    public static BaseEconomy getEconomyBridge(PayType payType) {
        return BaseEconomy.PAY_TYPES.getOrDefault(payType, null);
    }
}
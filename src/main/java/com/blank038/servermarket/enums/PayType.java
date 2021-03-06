package com.blank038.servermarket.enums;

import com.blank038.servermarket.bridge.NyEcoBridge;
import com.blank038.servermarket.bridge.PlayerPointBridge;
import com.blank038.servermarket.bridge.VaultBridge;

/**
 * 支付类型枚举
 * 方便后续扩展额外的支付方式
 *
 * @author Blank038
 * @date 2021/03/05
 */
public enum PayType {
    /**
     * Vault 插件
     */
    VAULT("Vault", VaultBridge.class),
    /**
     * PlayerPoints 插件
     */
    PLAYER_POINTS("PlayerPoints", PlayerPointBridge.class),
    /**
     * NyEconomy 插件
     */
    NY_ECONOMY("NyEconomy", NyEcoBridge.class);

    private final String plugin;
    private final Class<?> aClass;

    PayType(String plugin, Class<?> c) {
        this.plugin = plugin;
        this.aClass = c;
    }

    public String getPlugin() {
        return plugin;
    }

    public Class<?> getBridgeClass() {
        return aClass;
    }
}

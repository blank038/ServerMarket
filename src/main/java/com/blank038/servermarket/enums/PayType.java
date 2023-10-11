package com.blank038.servermarket.enums;

import com.blank038.servermarket.economy.BaseEconomy;
import com.blank038.servermarket.economy.impl.NyEconomyImpl;
import com.blank038.servermarket.economy.impl.PlayerPointEconomyImpl;
import com.blank038.servermarket.economy.impl.VaultEconomyImpl;
import lombok.Getter;

/**
 * 支付类型枚举
 * 方便后续扩展额外的支付方式
 *
 * @author Blank038
 * @date 2021/03/05
 */
@Getter
public enum PayType {
    /**
     * Vault 插件
     */
    VAULT("Vault", VaultEconomyImpl.class),
    /**
     * PlayerPoints 插件
     */
    PLAYER_POINTS("PlayerPoints", PlayerPointEconomyImpl.class),
    /**
     * NyEconomy 插件
     */
    NY_ECONOMY("NyEconomy", NyEconomyImpl.class);

    private final String plugin;
    private final Class<? extends BaseEconomy> bridgeClass;

    PayType(String plugin, Class<? extends BaseEconomy> c) {
        this.plugin = plugin;
        this.bridgeClass = c;
    }
}

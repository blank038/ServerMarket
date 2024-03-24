package com.blank038.servermarket.internal.economy.impl;

import com.blank038.servermarket.internal.economy.BaseEconomy;
import com.blank038.servermarket.internal.enums.PayType;
import com.mc9y.nyeconomy.api.NyEconomyAPI;
import org.bukkit.OfflinePlayer;

/**
 * @author Blank038
 */
public class NyEconomyImpl extends BaseEconomy {
    private final NyEconomyAPI nyEconomyApi;

    public NyEconomyImpl() {
        super(PayType.NY_ECONOMY);
        this.nyEconomyApi = com.mc9y.nyeconomy.Main.getNyEconomyAPI();
    }

    @Override
    public double balance(OfflinePlayer player, String key) {
        return this.nyEconomyApi.getBalance(key, player.getName());
    }

    @Override
    public void give(OfflinePlayer player, String key, double amount) {
        this.nyEconomyApi.deposit(key, player.getName(), (int) Math.max(1, amount));
    }

    @Override
    public boolean take(OfflinePlayer player, String key, double amount) {
        this.nyEconomyApi.withdraw(key, player.getName(), (int) Math.max(1, amount));
        return true;
    }
}

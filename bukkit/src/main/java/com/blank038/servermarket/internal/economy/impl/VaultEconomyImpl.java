package com.blank038.servermarket.internal.economy.impl;

import com.blank038.servermarket.internal.economy.BaseEconomy;
import com.blank038.servermarket.internal.enums.PayType;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @author Blank038
 */
@SuppressWarnings(value = {"unused"})
public class VaultEconomyImpl extends BaseEconomy {
    private Economy economyProvider;

    public VaultEconomyImpl() {
        super(PayType.VAULT);
        this.checkEconomy();
    }

    private void checkEconomy() {
        if (this.economyProvider != null) {
            return;
        }
        RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (service == null) {
            return;
        }
        this.economyProvider = service.getProvider();
    }

    @Override
    public double balance(OfflinePlayer player, String key) {
        this.checkEconomy();
        if (this.economyProvider == null) {
            return 0.0D;
        }
        return economyProvider.getBalance(player);
    }

    @Override
    public void give(OfflinePlayer player, String key, double amount) {
        this.checkEconomy();
        if (this.economyProvider == null) {
            return;
        }
        economyProvider.depositPlayer(player, Math.max(1, amount));
    }

    @Override
    public boolean take(OfflinePlayer player, String key, double amount) {
        this.checkEconomy();
        if (this.economyProvider == null) {
            return false;
        }
        return economyProvider.withdrawPlayer(player, Math.max(1, amount)).transactionSuccess();
    }
}

package com.blank038.servermarket.bridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class VaultBridge implements IBridge {
    private final Economy economy;

    public VaultBridge() {
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public double balance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    @Override
    public void give(OfflinePlayer player, double amount) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public boolean take(OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
}

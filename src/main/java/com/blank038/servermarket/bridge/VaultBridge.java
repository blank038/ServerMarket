package com.blank038.servermarket.bridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VaultBridge implements IBridge {
    private final Economy economy;

    public VaultBridge() {
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public double balance(Player player) {
        return economy.getBalance(player);
    }

    @Override
    public void give(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public boolean take(Player player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
}

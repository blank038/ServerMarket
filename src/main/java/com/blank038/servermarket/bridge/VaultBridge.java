package com.blank038.servermarket.bridge;

import com.blank038.servermarket.enums.PayType;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * @author Blank038
 */
@SuppressWarnings(value = {"unused"})
public class VaultBridge extends BaseBridge {
    private final Economy economy;

    public VaultBridge() {
        super(PayType.VAULT);
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public double balance(OfflinePlayer player, String key) {
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

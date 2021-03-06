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
    private final Economy ECONOMY_PROVIDER;

    public VaultBridge() {
        super(PayType.VAULT);
        ECONOMY_PROVIDER = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public double balance(OfflinePlayer player, String key) {
        return ECONOMY_PROVIDER.getBalance(player);
    }

    @Override
    public void give(OfflinePlayer player, String key, double amount) {
        ECONOMY_PROVIDER.depositPlayer(player, Math.max(1, amount));
    }

    @Override
    public boolean take(OfflinePlayer player, String key, double amount) {
        return ECONOMY_PROVIDER.withdrawPlayer(player, Math.max(1, amount)).transactionSuccess();
    }
}

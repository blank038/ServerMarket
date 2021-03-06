package com.blank038.servermarket.bridge;

import com.blank038.servermarket.enums.PayType;
import com.mc9y.nyeconomy.api.NyEconomyAPI;
import org.bukkit.OfflinePlayer;

/**
 * @author Blank038
 */
public class NyEcoBridge extends BaseBridge {
    private final NyEconomyAPI NY_ECONOMY_API;

    public NyEcoBridge() {
        super(PayType.NY_ECONOMY);
        this.NY_ECONOMY_API = com.mc9y.nyeconomy.Main.getNyEconomyAPI();
    }

    @Override
    public double balance(OfflinePlayer player, String key) {
        return this.NY_ECONOMY_API.getBalance(player.getName(), key);
    }

    @Override
    public void give(OfflinePlayer player, String key, double amount) {
        this.NY_ECONOMY_API.deposit(key, player.getName(), (int) amount);
    }

    @Override
    public boolean take(OfflinePlayer player, String key, double amount) {
        this.NY_ECONOMY_API.withdraw(key, player.getName(), (int) amount);
        return true;
    }
}

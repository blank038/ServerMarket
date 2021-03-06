package com.blank038.servermarket.bridge;

import com.blank038.servermarket.enums.PayType;
import org.bukkit.OfflinePlayer;

/**
 * @author Blank038
 */
public class NyEcoBridge extends BaseBridge {

    public NyEcoBridge() {
        super(PayType.NY_ECONOMY);
    }

    @Override
    public double balance(OfflinePlayer player, String key) {
        return 0;
    }

    @Override
    public void give(OfflinePlayer player, double amount) {

    }

    @Override
    public boolean take(OfflinePlayer player, double amount) {
        return false;
    }
}

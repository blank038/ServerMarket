package com.blank038.servermarket.bridge;

import com.blank038.servermarket.enums.PayType;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Blank038
 * @date 2021/03/06
 */
public class PlayerPointBridge extends BaseBridge {
    private final PlayerPointsAPI ppa;

    public PlayerPointBridge() {
        super(PayType.PLAYER_POINTS);
        ppa = JavaPlugin.getPlugin(PlayerPoints.class).getAPI();
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

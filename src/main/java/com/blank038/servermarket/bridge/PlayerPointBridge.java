package com.blank038.servermarket.bridge;

import com.blank038.servermarket.enums.PayType;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.storage.StorageHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Blank038
 * @date 2021/03/06
 */
public class PlayerPointBridge extends BaseBridge {
    private final PlayerPoints PLAYER_POINTS_PLUGIN;

    public PlayerPointBridge() {
        super(PayType.PLAYER_POINTS);
        this.PLAYER_POINTS_PLUGIN = JavaPlugin.getPlugin(PlayerPoints.class);
    }

    @Override
    public double balance(OfflinePlayer player, String key) {
        return this.PLAYER_POINTS_PLUGIN.getAPI().look(player.getUniqueId());
    }

    @Override
    public void give(OfflinePlayer player, String key, double amount) {
        // 通过 StorageHandler 给予点券, 不会触发事件
        int total = (int) (this.PLAYER_POINTS_PLUGIN.getAPI().look(player.getUniqueId()) + Math.max(1, amount));
        this.PLAYER_POINTS_PLUGIN.getModuleForClass(StorageHandler.class).setPoints(player.getUniqueId().toString(), total);
    }

    @Override
    public boolean take(OfflinePlayer player, String key, double amount) {
        return this.PLAYER_POINTS_PLUGIN.getAPI().take(player.getUniqueId(), (int) Math.max(1, amount));
    }
}

package com.blank038.servermarket.internal.listen.impl;

import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.listen.AbstractListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Blank038
 * @date 2021/03/05
 */
public class PlayerCommonListener extends AbstractListener {

    /**
     * 玩家加入服务器事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ServerMarket.getStorageHandler().getOrLoadPlayerCache(player.getUniqueId(), false);
    }

    /**
     * 玩家离开服务器事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            ServerMarket.getStorageHandler().savePlayerData(uuid, true);
            return true;
        });
        future.exceptionally((e) -> false);
        future.thenAccept((v) -> {
            if (v) {
                ServerMarket.getStorageHandler().setLock(uuid, false);
            }
        });
    }
}

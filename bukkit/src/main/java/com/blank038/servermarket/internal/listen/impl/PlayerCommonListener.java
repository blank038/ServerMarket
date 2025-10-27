package com.blank038.servermarket.internal.listen.impl;

import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.listen.AbstractListener;
import com.blank038.servermarket.internal.provider.GuiSearchProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Blank038
 */
public class PlayerCommonListener extends AbstractListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ServerMarket.getStorageHandler().getOrLoadPlayerCache(player.getUniqueId(), false);
    }

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
        // Remove search cache
        GuiSearchProvider.remove(uuid);
    }

    @EventHandler
    public void onSearch(AsyncPlayerChatEvent event) {
        if (GuiSearchProvider.search(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
        }
    }
}

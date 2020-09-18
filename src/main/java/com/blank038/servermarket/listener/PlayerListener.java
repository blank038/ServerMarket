package com.blank038.servermarket.listener;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    /**
     * 玩家加入服务器事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ServerMarket.getInstance().datas.put(event.getPlayer().getName(), new PlayerData(event.getPlayer().getName()));
    }

    /**
     * 玩家离开服务器事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (ServerMarket.getInstance().datas.containsKey(event.getPlayer().getName())) {
            ServerMarket.getInstance().datas.remove(event.getPlayer().getName()).save();
        }
    }
}

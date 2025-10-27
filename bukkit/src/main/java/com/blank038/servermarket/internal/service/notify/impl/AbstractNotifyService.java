package com.blank038.servermarket.internal.service.notify.impl;

import com.blank038.servermarket.internal.service.notify.INotifyService;
import org.bukkit.Bukkit;

/**
 * @author Blank038
 */
public abstract class AbstractNotifyService implements INotifyService {

    @Override
    public void broadcast(String message) {
        Bukkit.getOnlinePlayers().forEach((player) -> player.sendMessage(message));
    }
}

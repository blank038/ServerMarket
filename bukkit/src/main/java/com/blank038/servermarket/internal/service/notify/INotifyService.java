package com.blank038.servermarket.internal.service.notify;

import com.blank038.servermarket.internal.cache.other.NotifyCache;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Blank038
 */
public interface INotifyService {

    void register(ConfigurationSection config);

    void push(NotifyCache notifyCache);

    void broadcast(String message);
}

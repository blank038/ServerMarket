package com.blank038.servermarket.internal.service.notify.impl.self;

import com.blank038.servermarket.internal.cache.other.NotifyCache;
import com.blank038.servermarket.internal.service.notify.impl.AbstractNotifyService;
import org.bukkit.configuration.ConfigurationSection;


/**
 * @author Blank038
 */
public class SelfNotifyServiceImpl extends AbstractNotifyService {

    @Override
    public void register(ConfigurationSection config) {
    }

    @Override
    public void push(NotifyCache cache) {
        this.broadcast(cache.message);
    }
}

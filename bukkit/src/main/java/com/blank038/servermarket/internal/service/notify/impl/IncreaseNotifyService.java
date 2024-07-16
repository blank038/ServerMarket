package com.blank038.servermarket.internal.service.notify.impl;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.api.platform.wrapper.ITaskWrapper;
import com.blank038.servermarket.internal.plugin.ServerMarket;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
public abstract class IncreaseNotifyService extends AbstractNotifyService {
    private final List<Integer> pushedIndexes = new ArrayList<>();
    private ITaskWrapper wrapper;

    public boolean hasIndex(int index) {
        return this.pushedIndexes.contains(index);
    }

    public void addIndex(int index) {
        this.pushedIndexes.add(index);
    }
    
    public void update() {
    }
    
    public void runTask(int seconds) {
        if (wrapper != null) {
            wrapper.cancel();
        }
        wrapper = ServerMarketApi.getPlatformApi().runTaskTimerAsynchronously(
                ServerMarket.getInstance(),
                () -> this.update(),
                seconds,
                seconds
        );
    }
}

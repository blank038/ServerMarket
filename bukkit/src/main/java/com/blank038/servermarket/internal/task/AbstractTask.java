package com.blank038.servermarket.internal.task;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.api.platform.wrapper.ITaskWrapper;
import com.blank038.servermarket.internal.plugin.ServerMarket;

public abstract class AbstractTask implements ITask {
    private ITaskWrapper wrapper;

    @Override
    public ITaskWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public void restart() {
        if (wrapper != null) {
            wrapper.cancel();
        }
        wrapper = ServerMarketApi.getPlatformApi().runTaskTimerAsynchronously(
                ServerMarket.getInstance(),
                this,
                getDelay(),
                getPeroid()
        );
    }
}

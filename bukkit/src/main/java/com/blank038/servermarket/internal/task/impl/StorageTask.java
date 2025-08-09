package com.blank038.servermarket.internal.task.impl;

import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.task.AbstractTask;

public class StorageTask extends AbstractTask {

    @Override
    public long getDelay() {
        return 60;
    }

    @Override
    public long getPeroid() {
        return 60;
    }

    @Override
    public void run() {
        ServerMarket.getStorageHandler().saveAll();
    }
}

package com.blank038.servermarket.internal.task;

import com.blank038.servermarket.api.platform.wrapper.ITaskWrapper;

public interface ITask extends Runnable {

    ITaskWrapper getWrapper();

    long getDelay();

    long getPeroid();

    void restart();
}

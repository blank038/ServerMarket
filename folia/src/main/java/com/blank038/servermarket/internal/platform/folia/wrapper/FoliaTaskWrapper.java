package com.blank038.servermarket.internal.platform.folia.wrapper;

import com.blank038.servermarket.api.platform.wrapper.ITaskWrapper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.AllArgsConstructor;

/**
 * @author Blank038
 */
@AllArgsConstructor
public class FoliaTaskWrapper implements ITaskWrapper {
    private final ScheduledTask task;

    @Override
    public void cancel() {
        this.task.cancel();
    }
}

package com.blank038.servermarket.internal.handler;

import com.blank038.servermarket.internal.task.ITask;
import com.blank038.servermarket.internal.task.impl.CacheUpdateTask;
import com.blank038.servermarket.internal.task.impl.OfflineTransactionTask;
import com.blank038.servermarket.internal.task.impl.StorageTask;
import com.blank038.servermarket.internal.task.impl.TimeoutDisposeTask;

import java.util.HashMap;
import java.util.Map;

public class TaskHandler {
    public static final Map<String, ITask> INTERNAL_TASKS = new HashMap<>();

    static {
        INTERNAL_TASKS.put("cache_update", new CacheUpdateTask());
        INTERNAL_TASKS.put("offline-transaction", new OfflineTransactionTask());
        INTERNAL_TASKS.put("storage", new StorageTask());
        INTERNAL_TASKS.put("timeout-dispose", new TimeoutDisposeTask());
    }

    public static void restartInternalTasks() {
        INTERNAL_TASKS.values().forEach(ITask::restart);
    }
}

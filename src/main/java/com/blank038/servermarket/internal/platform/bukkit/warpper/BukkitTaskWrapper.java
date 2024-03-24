package com.blank038.servermarket.internal.platform.bukkit.warpper;

import com.blank038.servermarket.api.platform.wrapper.ITaskWrapper;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Blank038
 */
@AllArgsConstructor
public class BukkitTaskWrapper implements ITaskWrapper {

    public final BukkitTask bukkitTask;

    @Override
    public void cancel() {
        bukkitTask.cancel();
    }
}

package com.blank038.servermarket.api.platform;

import com.blank038.servermarket.api.platform.wrapper.ITaskWrapper;
import org.bukkit.plugin.java.JavaPlugin;

public interface IPlatformApi {

    ITaskWrapper runTask(JavaPlugin plugin, Runnable runnable);

    ITaskWrapper runTaskAsynchronously(JavaPlugin plugin, Runnable runnable);

    ITaskWrapper runTaskTimerAsynchronously(JavaPlugin plugin, Runnable runnable, long delaySecond, long periodSecond);
}

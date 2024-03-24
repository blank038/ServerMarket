package com.blank038.servermarket.internal.platform.bukkit;

import com.blank038.servermarket.api.platform.IPlatformApi;
import com.blank038.servermarket.api.platform.wrapper.ITaskWrapper;
import com.blank038.servermarket.internal.platform.bukkit.warpper.BukkitTaskWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Blank038
 */
public class BukkitPlatformApi implements IPlatformApi {

    @Override
    public ITaskWrapper runTask(JavaPlugin plugin, Runnable runnable) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTask(plugin, runnable);
        return new BukkitTaskWrapper(bukkitTask);
    }

    @Override
    public ITaskWrapper runTaskTimerAsynchronously(JavaPlugin plugin, Runnable runnable, long delay, long period) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
        return new BukkitTaskWrapper(bukkitTask);
    }
}

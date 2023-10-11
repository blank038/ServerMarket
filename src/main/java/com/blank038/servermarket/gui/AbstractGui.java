package com.blank038.servermarket.gui;

import com.blank038.servermarket.ServerMarket;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
public abstract class AbstractGui {
    protected static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(ServerMarket.getInstance(), () -> {
            COOLDOWN.entrySet().removeIf((entry) -> System.currentTimeMillis() > entry.getValue());
        }, 1200L, 1200L);
    }
}

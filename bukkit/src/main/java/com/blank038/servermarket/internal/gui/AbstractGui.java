package com.blank038.servermarket.internal.gui;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
public abstract class AbstractGui implements InventoryHolder {
    protected static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    static {
        ServerMarketApi.getPlatformApi().runTaskTimerAsynchronously(ServerMarket.getInstance(), () -> {
            synchronized (COOLDOWN) {
                COOLDOWN.entrySet().removeIf((entry) -> System.currentTimeMillis() > entry.getValue());
            }
        }, 60, 60);
    }

    public boolean isCooldown(UUID uuid) {
        if (System.currentTimeMillis() <= COOLDOWN.getOrDefault(uuid, 0L)) {
            return true;
        }
        COOLDOWN.put(uuid, System.currentTimeMillis() + ServerMarket.getInstance().getConfig().getInt("cooldown.action"));
        return false;
    }
}

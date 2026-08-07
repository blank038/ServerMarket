package com.blank038.servermarket.internal.gui;

import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.internal.gui.context.GuiContext;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
public abstract class AbstractGui implements IGui {
    protected static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    protected GuiContext context;

    static {
        ServerMarketApi.getPlatformApi().runTaskTimerAsynchronously(ServerMarket.getInstance(), () -> {
            synchronized (COOLDOWN) {
                COOLDOWN.entrySet().removeIf((entry) -> System.currentTimeMillis() > entry.getValue());
            }
        }, 60, 60);
    }

    public AbstractGui(GuiContext context) {
        this.context = context;
    }

    @Override
    public GuiContext getContext() {
        return context;
    }

    public boolean isCooldown(UUID uuid) {
        if (System.currentTimeMillis() <= COOLDOWN.getOrDefault(uuid, 0L)) {
            return true;
        }
        COOLDOWN.put(uuid, System.currentTimeMillis() + ServerMarket.getInstance().getConfig().getInt("cooldown.action"));
        return false;
    }

    protected void preventInventoryDrag(GuiModel model) {
        Listener listener = new Listener() {
            @EventHandler
            public void onInventoryDrag(InventoryDragEvent event) {
                if (event.getInventory() == model.getInventory()) {
                    event.setCancelled(true);
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(listener, ServerMarket.getInstance());
        model.onClose((event) -> HandlerList.unregisterAll(listener));
    }
}

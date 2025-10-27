package com.blank038.servermarket.internal.provider;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.api.handler.filter.FilterHandler;
import com.blank038.servermarket.api.handler.filter.impl.KeyFilterImpl;
import com.blank038.servermarket.api.handler.filter.impl.TypeFilterImpl;
import com.blank038.servermarket.internal.gui.context.GuiContext;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiSearchProvider {
    private static final Map<UUID, GuiContext> GUI_SEARCH = new ConcurrentHashMap<>();

    public static void add(UUID uuid, GuiContext context) {
        GUI_SEARCH.put(uuid, context);
    }

    public static void remove(UUID uuid) {
        GUI_SEARCH.remove(uuid);
    }

    public static boolean search(Player player, String search) {
        GuiContext context = GUI_SEARCH.remove(player.getUniqueId());
        if (context == null) {
            return false;
        }
        if (search.equalsIgnoreCase("cancel")) {
            return true;
        }
        // TODO: Support search for type
        FilterHandler filter = new FilterHandler()
                .addKeyFilter(new KeyFilterImpl(search))
                .setTypeFilter(new TypeFilterImpl(Lists.newArrayList("none")));
        context.setPage(1);
        context.setFilter(filter);
        // Open the market in the main thread
        Bukkit.getScheduler().runTask(ServerMarket.getInstance(), () -> {
            ServerMarketApi.openMarket(player, context);
        });
        return true;
    }
}

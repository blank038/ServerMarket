package com.blank038.servermarket.data.handler;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.api.event.InitializeStorageHandlerEvent;
import com.blank038.servermarket.data.cache.player.PlayerCache;
import com.blank038.servermarket.data.handler.impl.YamlStorageHandlerImpl;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Blank038
 */
public abstract class AbstractStorageHandler implements IStorageHandler {
    protected static final HashMap<UUID, PlayerCache> PLAYER_DATA_MAP = new HashMap<>();

    protected final ServerMarket pluign = ServerMarket.getInstance();

    @Override
    public void load(String market) {
    }

    public static void check() {
        if (ServerMarket.getStorageHandler() == null) {
            IStorageHandler storageHandler = null;
            switch (ServerMarket.getInstance().getConfig().getString("data-option.type").toLowerCase()) {
                case "mysql":
                    break;
                case "yaml":
                default:
                    storageHandler = new YamlStorageHandlerImpl();
                    break;
            }
            InitializeStorageHandlerEvent event = new InitializeStorageHandlerEvent(storageHandler);
            Bukkit.getPluginManager().callEvent(event);
            // Set storage hadnler
            ServerMarket.setStorageHandler(event.getStorageHandler());
            ServerMarket.getStorageHandler().initialize();
        }
    }
}

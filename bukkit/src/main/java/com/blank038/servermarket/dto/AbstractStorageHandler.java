package com.blank038.servermarket.dto;

import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.api.event.InitializeStorageHandlerEvent;
import com.blank038.servermarket.internal.cache.player.PlayerCache;
import com.blank038.servermarket.dto.impl.MysqlStorageHandlerImpl;
import com.blank038.servermarket.dto.impl.YamlStorageHandlerImpl;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
public abstract class AbstractStorageHandler implements IStorageHandler {
    protected static final Map<UUID, PlayerCache> PLAYER_DATA_MAP = new HashMap<>();

    protected final ServerMarket pluign = ServerMarket.getInstance();

    @Override
    public void load(String market) {
    }

    public static void check() {
        if (ServerMarket.getStorageHandler() == null) {
            IStorageHandler storageHandler;
            switch (ServerMarket.getInstance().getConfig().getString("data-option.type").toLowerCase()) {
                case "mysql":
                    storageHandler = new MysqlStorageHandlerImpl();
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

    @Override
    public void saveAllPlayerData() {
        PLAYER_DATA_MAP.entrySet().stream().forEach((entry) -> this.savePlayerData(entry.getValue(), false));
    }

    @Override
    public void setLock(UUID uuid, boolean locked) {
    }

    @Override
    public boolean isLocked(UUID uuid) {
        return false;
    }

    public void removePlyerData(UUID uuid) {
        PLAYER_DATA_MAP.entrySet().removeIf(entry -> entry.getKey().equals(uuid));
    }
}

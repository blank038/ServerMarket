package com.blank038.servermarket.internal.plugin;

import com.aystudio.core.bukkit.plugin.AyPlugin;
import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.api.handler.sort.AbstractSortHandler;
import com.blank038.servermarket.internal.data.convert.LegacyBackup;
import com.blank038.servermarket.dto.AbstractStorageHandler;
import com.blank038.servermarket.dto.IStorageHandler;
import com.blank038.servermarket.internal.economy.BaseEconomy;
import com.blank038.servermarket.internal.command.MainCommand;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.internal.listen.impl.CoreListener;
import com.blank038.servermarket.internal.listen.impl.PlayerCommonListener;
import com.blank038.servermarket.internal.listen.impl.PlayerLatestListener;
import com.blank038.servermarket.internal.metrics.Metrics;
import com.blank038.servermarket.internal.platform.PlatformHandler;
import com.blank038.servermarket.internal.task.OfflineTransactionTask;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import lombok.Getter;
import lombok.Setter;

/**
 * Global market plugin for Bukkit.
 * (version: 1.8 ~ 1.16)
 *
 * @author Blank038
 */
@SuppressWarnings(value = {"unused"})
public class ServerMarket extends AyPlugin {
    @Getter
    private static ServerMarket instance;
    @Getter
    @Setter
    private static IStorageHandler storageHandler;


    @Override
    public void onEnable() {
        instance = this;
        // initialize platform
        PlatformHandler.initPlatform();
        // begin loading
        this.getConsoleLogger().setPrefix("&f[&eServerMarket&f] &8");
        this.loadConfig(true);
        // register command executor
        new MainCommand(this).register();
        // register listeners
        new CoreListener().register();
        new PlayerCommonListener().register();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            new PlayerLatestListener().register();
        }
        // register sort handler
        AbstractSortHandler.registerDefaults();
        // start tasks
        ServerMarketApi.getPlatformApi().runTaskTimerAsynchronously(this, storageHandler::removeTimeOutItem, 200L, 200L);
        ServerMarketApi.getPlatformApi().runTaskTimerAsynchronously(this, storageHandler::saveAll, 1200L, 1200L);
        // inject metrics
        new Metrics(this, 20031);
    }

    @Override
    public void onDisable() {
        if (storageHandler != null) {
            storageHandler.saveAll();
        }
    }

    /**
     * 检测配置文件是否存在, 并且重载配置文件
     */
    public void loadConfig(boolean start) {
        this.getConsoleLogger().log(false, " ");
        this.getConsoleLogger().log(false, "   &3ServerMarket &bv" + this.getDescription().getVersion());
        this.getConsoleLogger().log(false, " ");
        this.saveDefaultConfig();
        this.reloadConfig();
        // Initialize I18n
        new I18n(this.getConfig().getString("language", "zh_CN"));
        // restart the task for offline transaction
        OfflineTransactionTask.restart();
        // Run legacy converter
        LegacyBackup.check();
        if (this.isEnabled()) {
            // Initialize IStorageHandler
            AbstractStorageHandler.check();
            storageHandler.reload();
            // Initialize economy
            BaseEconomy.initEconomies();
            // Initialize DataContainer
            DataContainer.loadData();
            // Save the default files
            for (String fileName : new String[]{"gui/store.yml"}) {
                this.saveResource(fileName, fileName);
            }
            this.getConsoleLogger().log(false, I18n.getProperties().getProperty("load-completed")
                    .replace("%s", String.valueOf(DataContainer.MARKET_DATA.size())));
            this.getConsoleLogger().log(false, " ");
        }
    }
}
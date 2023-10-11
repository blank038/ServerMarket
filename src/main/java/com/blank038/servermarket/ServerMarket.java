package com.blank038.servermarket;

import com.aystudio.core.bukkit.plugin.AyPlugin;
import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.data.handler.AbstractStorageHandler;
import com.blank038.servermarket.data.handler.IStorageHandler;
import com.blank038.servermarket.economy.BaseEconomy;
import com.blank038.servermarket.command.MainCommand;
import com.blank038.servermarket.data.DataContainer;
import com.blank038.servermarket.i18n.I18n;
import com.blank038.servermarket.data.cache.market.MarketData;
import com.blank038.servermarket.listen.impl.CoreListener;
import com.blank038.servermarket.listen.impl.PlayerListener;
import com.blank038.servermarket.metrics.Metrics;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

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
    private static ServerMarketApi api;
    @Getter
    @Setter
    private static IStorageHandler storageHandler;


    @Override
    public void onEnable() {
        instance = this;
        api = new ServerMarketApi();
        // 开始载入
        this.getConsoleLogger().setPrefix("&f[&eServerMarket&f] &8");
        this.loadConfig(true);
        // 注册命令、事件及线程
        super.getCommand("servermarket").setExecutor(new MainCommand(this));
        // 注册事件监听类
        new CoreListener().register();
        new PlayerListener().register();
        // start tasks
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, storageHandler::removeTimeOutItem, 200L, 200L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, storageHandler::saveAll, 1200L, 1200L);
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
        // Initialize IStorageHandler
        AbstractStorageHandler.check();
        storageHandler.reload();
        // Initialize economy
        BaseEconomy.initEconomies();
        // Initialize I18n
        new I18n(this.getConfig().getString("language", "zh_CN"));
        // Save the default files
        for (String fileName : new String[]{"gui/store.yml"}) {
            this.saveResource(fileName, fileName);
        }
        // Initialize DataContainer
        DataContainer.loadData();
        this.getConsoleLogger().log(false, "&6 * &f加载完成, 已读取 &a" + MarketData.MARKET_DATA.size() + "&f 个市场");
        this.getConsoleLogger().log(false, " ");
    }
}
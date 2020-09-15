package com.blank038.servermarket;

import org.bukkit.plugin.java.JavaPlugin;

public class ServerMarket extends JavaPlugin {
    private static ServerMarket serverMarket;

    public static ServerMarket getInstance() {
        return serverMarket;
    }

    @Override
    public void onEnable() {
        serverMarket = this;
        // 载入插件配置文件
        loadConfig();
    }

    /**
     * 检测配置文件是否存在, 并且重载配置文件
     */
    public void loadConfig() {
        getDataFolder().mkdir();
        saveDefaultConfig();
        reloadConfig();
        // 重新加载数据中的物品

    }
}
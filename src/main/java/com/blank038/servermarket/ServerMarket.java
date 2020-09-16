package com.blank038.servermarket;

import com.blank038.servermarket.config.LangConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Global market plugin for Bukkit.
 * (version: 1.8 ~ 1.16)
 *
 * @author Blank038
 */
public class ServerMarket extends JavaPlugin {
    private static ServerMarket serverMarket;
    // 语言配置类
    private LangConfiguration lang;

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
        // 设定语言配置
        if (lang == null) lang = new LangConfiguration();
        else lang.reload();
        // 重新加载数据中的物品

    }
}
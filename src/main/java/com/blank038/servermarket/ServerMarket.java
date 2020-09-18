package com.blank038.servermarket;

import com.blank038.servermarket.bridge.IBridge;
import com.blank038.servermarket.bridge.VaultBridge;
import com.blank038.servermarket.command.MainCommand;
import com.blank038.servermarket.config.LangConfiguration;
import com.blank038.servermarket.data.PlayerData;
import com.blank038.servermarket.data.SaleItem;
import com.blank038.servermarket.listener.PlayerListener;
import com.blank038.servermarket.nms.NBTBase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

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
    // 获取经济桥类
    private IBridge ecoBridge;
    // NMS 接口
    private NBTBase nbtBase;
    // 商品列表
    public final HashMap<String, SaleItem> sales = new HashMap<>();
    // 玩家存档
    public final HashMap<String, PlayerData> datas = new HashMap<>();

    public static ServerMarket getInstance() {
        return serverMarket;
    }

    public IBridge getEconomyBridge() {
        return ecoBridge;
    }

    public NBTBase getNBTBase() {
        return nbtBase;
    }

    @Override
    public void onEnable() {
        serverMarket = this;
        // 初始化经济桥
        ecoBridge = new VaultBridge();
        // 载入插件配置文件
        loadConfig();
        // 注册命令
        getCommand("servermarket").setExecutor(new MainCommand(this));
        // 注册事件监听类
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
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
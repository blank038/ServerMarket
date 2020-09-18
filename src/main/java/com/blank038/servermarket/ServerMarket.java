package com.blank038.servermarket;

import com.blank038.servermarket.bridge.IBridge;
import com.blank038.servermarket.bridge.VaultBridge;
import com.blank038.servermarket.command.MainCommand;
import com.blank038.servermarket.config.LangConfiguration;
import com.blank038.servermarket.data.PlayerData;
import com.blank038.servermarket.data.gui.SaleItem;
import com.blank038.servermarket.listener.PlayerListener;
import com.blank038.servermarket.nms.NBTBase;
import com.blank038.servermarket.nms.sub.v1_12_R1;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    // API 接口
    private ServerMarketAPI serverMarketAPI;
    // 商品列表
    public final HashMap<String, SaleItem> sales = new HashMap<>();
    // 玩家存档
    public final HashMap<String, PlayerData> datas = new HashMap<>();
    // 返还玩家的钱
    public final HashMap<String, Double> results = new HashMap<>();

    public static ServerMarket getInstance() {
        return serverMarket;
    }

    public IBridge getEconomyBridge() {
        return ecoBridge;
    }

    public NBTBase getNBTBase() {
        return nbtBase;
    }

    public ServerMarketAPI getApi() {
        return serverMarketAPI;
    }

    @Override
    public void onEnable() {
        // 检测 NMS 版本
        String version = ((Object) Bukkit.getServer()).getClass().getPackage().getName().split("\\.")[3];
        boolean disable = false;
        switch (version) {
            case "v1_12_R1":
                nbtBase = new v1_12_R1();
                break;
            case "???":
                break;
            default:
                this.getLogger().info("服务器版本不支持, 关闭插件");
                disable = true;
                this.setEnabled(false);
        }
        if (disable) return;
        serverMarket = this;
        // 初始化经济桥
        ecoBridge = new VaultBridge();
        // 初始化 API
        serverMarketAPI = new ServerMarketAPI(this);
        // 载入插件配置文件
        loadConfig();
        // 注册命令
        getCommand("servermarket").setExecutor(new MainCommand(this));
        // 注册事件监听类
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        // 建立线程定时保存
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveSaleList, 1200L, 1200L);
        // 载入在线玩家数据
        for (Player player : Bukkit.getOnlinePlayers()) {
            datas.put(player.getName(), new PlayerData(player.getName()));
        }
        // 定时存储玩家数据
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::savePlayerData, 1200L, 1200L);
        getLogger().info("插件加载完成, 已读取 " + sales.size() + " 个商品数据.");
        getLogger().info("感谢使用, 作者: Blank038 版本: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        saveSaleList();
        savePlayerData();
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
        // 判断玩家存档目录是否存在
        File dataFolder = new File(getDataFolder(), "data");
        dataFolder.mkdirs();
        // 判断资源文件是否存在
        for (String fileNmae : new String[]{"gui.yml", "store.yml"}) {
            File f = new File(getDataFolder(), fileNmae);
            if (!f.exists()) {
                saveResource(fileNmae, true);
            }
        }
        // 重新加载数据中的物品
        reloadSaleItem();
        // 开始读取离线玩家获得金币
        if (!results.isEmpty()) {
            saveResults();
        }
        results.clear();
        File resultFile = new File(getDataFolder(), "results.yml");
        FileConfiguration resultData = YamlConfiguration.loadConfiguration(resultFile);
        if (!resultFile.exists()) {
            try {
                resultFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (String key : resultData.getKeys(false)) {
            results.put(key, resultData.getDouble(key));
        }
    }

    private void reloadSaleItem() {
        if (!sales.isEmpty()) {
            saveSaleList();
        }
        File file = new File(getDataFolder(), "data.yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sales.clear();
        // 读取配置文件中的物品
        for (String key : data.getKeys(false)) {
            sales.put(key, new SaleItem(data.getConfigurationSection(key)));
        }
    }

    private void saveResults() {
        File resultFile = new File(getDataFolder(), "results.yml");
        FileConfiguration resultData = new YamlConfiguration();
        for (Map.Entry<String, Double> entry : results.entrySet()) {
            resultData.set(entry.getKey(), entry.getValue());
        }
        try {
            resultData.save(resultFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSaleList() {
        File file = new File(getDataFolder(), "data.yml");
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, SaleItem> entry : sales.entrySet()) {
            data.set(entry.getKey(), entry.getValue().toSection());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerData() {
        for (Map.Entry<String, PlayerData> entry : datas.entrySet()) {
            entry.getValue().save();
        }
    }

    public void addMoney(String name, double moeny) {
        if (results.containsKey(name)) {
            results.replace(name, results.get(name) + moeny);
        } else {
            results.put(name, moeny);
        }
    }
}
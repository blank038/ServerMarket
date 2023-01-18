package com.blank038.servermarket;

import com.aystudio.core.bukkit.plugin.AyPlugin;
import com.blank038.servermarket.api.ServerMarketAPI;
import com.blank038.servermarket.bridge.BaseBridge;
import com.blank038.servermarket.command.MainCommand;
import com.blank038.servermarket.data.DataContainer;
import com.blank038.servermarket.i18n.I18n;
import com.blank038.servermarket.data.storage.MarketData;
import com.blank038.servermarket.data.cache.PlayerData;
import com.blank038.servermarket.data.storage.ResultData;
import com.blank038.servermarket.enums.PayType;
import com.blank038.servermarket.listener.PlayerListener;
import com.blank038.servermarket.util.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Global market plugin for Bukkit.
 * (version: 1.8 ~ 1.16)
 *
 * @author Blank038
 */
@SuppressWarnings(value = {"unused"})
public class ServerMarket extends AyPlugin {
    private static ServerMarket serverMarket;
    /**
     * API 接口
     */
    private static ServerMarketAPI serverMarketAPI;


    @Override
    public void onEnable() {
        serverMarket = this;
        serverMarketAPI = new ServerMarketAPI();
        // 开始载入
        this.getConsoleLogger().setPrefix("&f[&eServerMarket&f] &8");
        this.loadConfig(true);
        // 注册命令、事件及线程
        super.getCommand("servermarket").setExecutor(new MainCommand(this));
        // 注册事件监听类
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveSaleList, 1200L, 1200L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::savePlayerData, 1200L, 1200L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveResults, 1200L, 1200L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, MarketData::removeTimeOutItem, 200L, 200L);
        // 载入在线玩家数据
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData.PLAYER_DATA.put(player.getUniqueId(), new PlayerData(player.getUniqueId()));
        }
    }

    @Override
    public void onDisable() {
        this.saveSaleList();
        this.savePlayerData();
        this.saveResults();
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
        // 挂钩货币
        if (start) {
            // 初始化货币桥
            BaseBridge.initBridge();
        }
        // 设定语言配置
        new I18n();
        // 判断玩家存档目录是否存在
        File dataFolder = new File(getDataFolder(), "data");
        dataFolder.mkdirs();
        // 判断资源文件是否存在
        for (String fileName : new String[]{"store.yml"}) {
            File f = new File(getDataFolder(), fileName);
            if (!f.exists()) {
                saveResource(fileName, true);
            }
        }
        // 载入数据容器
        DataContainer.loadData();
        // 重新加载数据中的物品
        this.reloadSaleItem();
        this.getConsoleLogger().log(false, "&6 * &f加载完成, 已读取 &a" + MarketData.MARKET_DATA.size() + "&f 个市场");
        // 开始读取离线玩家获得金币
        if (!ResultData.RESULT_DATA.isEmpty()) {
            this.saveResults();
        }
        ResultData.RESULT_DATA.clear();
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
            ResultData.RESULT_DATA.put(key, new ResultData(resultData.getConfigurationSection(key)));
        }
        this.getConsoleLogger().log(false, " ");
    }

    public BaseBridge getEconomyBridge(PayType payType) {
        return BaseBridge.PAY_TYPES.getOrDefault(payType, null);
    }


    public void saveSaleList() {
        for (Map.Entry<String, MarketData> entry : MarketData.MARKET_DATA.entrySet()) {
            entry.getValue().saveSaleData();
        }
    }

    public void savePlayerData() {
        for (Map.Entry<UUID, PlayerData> entry : PlayerData.PLAYER_DATA.entrySet()) {
            entry.getValue().save();
        }
    }

    public void addMoney(String uuid, PayType payType, String ectType, double moeny, String sourceMarket) {
        ConfigurationSection section = new YamlConfiguration();
        section.set("amount", moeny);
        section.set("pay-type", payType.name());
        section.set("owner-uuid", uuid);
        section.set("eco-type", ectType);
        section.set("source-market", sourceMarket);
        // 存入数据
        ResultData resultData = new ResultData(section);
        ResultData.RESULT_DATA.put(UUID.randomUUID().toString(), resultData);
    }

    private void reloadSaleItem() {
        if (!MarketData.MARKET_DATA.isEmpty()) {
            this.saveSaleList();
        }
        // 读取市场配置
        File file = new File(getDataFolder(), "market");
        if (!file.exists()) {
            file.mkdir();
            // 输出
            CommonUtil.outputFile(this.getResource("market/example.yml"), new File(getDataFolder() + "/market/", "example.yml"));
        }
        MarketData.MARKET_DATA.clear();
        // 读取市场
        Arrays.stream(Objects.requireNonNull(file.listFiles())).iterator().forEachRemaining(MarketData::new);
    }

    private void saveResults() {
        File resultFile = new File(getDataFolder(), "results.yml");
        FileConfiguration resultData = new YamlConfiguration();
        for (Map.Entry<String, ResultData> entry : ResultData.RESULT_DATA.entrySet()) {
            resultData.set(entry.getKey(), entry.getValue().toSection());
        }
        try {
            resultData.save(resultFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ServerMarketAPI getApi() {
        return serverMarketAPI;
    }

    public static ServerMarket getInstance() {
        return serverMarket;
    }
}
package com.blank038.servermarket.dto.impl;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.dto.AbstractStorageHandler;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.internal.cache.market.MarketCache;
import com.blank038.servermarket.internal.cache.other.OfflineTransactionData;
import com.blank038.servermarket.internal.cache.other.SaleLog;
import com.blank038.servermarket.internal.cache.player.PlayerCache;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Blank038
 */
public class YamlStorageHandlerImpl extends AbstractStorageHandler {
    private static final Map<String, OfflineTransactionData> RESULT_DATA = new HashMap<>();
    private static final Map<String, MarketCache> MARKET_STORAGE_DATA_MAP = new HashMap<>();
    private static final Map<String, ConfigurationSection> LOG_SECTION_MAP = new HashMap<>();

    /*
     * self methods
     */


    private void saveResults() {
        File resultFile = new File(this.pluign.getDataFolder(), "results.yml");
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, OfflineTransactionData> entry : RESULT_DATA.entrySet()) {
            data.set(entry.getKey(), entry.getValue().toSection());
        }
        try {
            data.save(resultFile);
        } catch (IOException e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot save result data");
        }
    }


    /*
     * AbstractStorageHandler methods
     */
    private void saveLogs() {
        synchronized (LOG_SECTION_MAP) {
            File logFolder = new File(ServerMarket.getInstance().getDataFolder(), "logs");
            logFolder.mkdir();

            LocalDate localDate = LocalDate.now();
            String format = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            File logFile = new File(logFolder, format + ".yml");
            FileConfiguration data = YamlConfiguration.loadConfiguration(logFile);
            try {
                LOG_SECTION_MAP.forEach(data::set);
                LOG_SECTION_MAP.clear();
                data.save(logFile);
            } catch (IOException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot save log data: " + format + ".yml");
            }
        }
    }

    @Override
    public void initialize() {
        ServerMarketApi.getPlatformApi().runTaskTimerAsynchronously(ServerMarket.getInstance(), this::saveLogs, 60L, 60L);
    }

    @Override
    public void reload() {
        File dataFolder = new File(ServerMarket.getInstance().getDataFolder(), "data");
        dataFolder.mkdirs();
        // check logs
        if (!LOG_SECTION_MAP.isEmpty()) {
            this.saveLogs();
        }
        // Check results map
        if (!RESULT_DATA.isEmpty()) {
            this.saveResults();
        }
        RESULT_DATA.clear();
        File resultFile = new File(this.pluign.getDataFolder(), "results.yml");
        FileConfiguration resultData = YamlConfiguration.loadConfiguration(resultFile);
        if (!resultFile.exists()) {
            try {
                resultFile.createNewFile();
            } catch (IOException e) {
                this.pluign.getLogger().log(Level.WARNING, e, () -> "wrong when create 'results.yml'");
            }
        }
        for (String key : resultData.getKeys(false)) {
            RESULT_DATA.put(key, new OfflineTransactionData(resultData.getConfigurationSection(key)));
        }
    }

    @Override
    public void load(String market) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            this.save(market, MARKET_STORAGE_DATA_MAP.get(market).getSales());
        }
        MarketCache marketCache = new MarketCache(market);
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/saleData/", market + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            SaleCache saleItem = new SaleCache(market, data.getConfigurationSection(key));
            marketCache.addSale(saleItem.getSaleUUID(), saleItem);
        }
        MARKET_STORAGE_DATA_MAP.put(market, marketCache);
    }

    @Override
    public boolean hasSale(String market, String saleId) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            return MARKET_STORAGE_DATA_MAP.get(market).hasSale(saleId);
        }
        return false;
    }

    @Override
    public int getSaleCountByPlayer(UUID uuid, String market) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            return MARKET_STORAGE_DATA_MAP.get(market).getSaleByPlayer(uuid).size();
        }
        return 0;
    }

    @Override
    public Optional<SaleCache> getSaleItem(String market, String saleId) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            return MARKET_STORAGE_DATA_MAP.get(market).getSale(saleId);
        }
        return Optional.empty();
    }

    @Override
    public Map<String, SaleCache> getSaleItemsByMarket(String market) {
        if (!MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            MarketCache marketCache = new MarketCache(market);
            MARKET_STORAGE_DATA_MAP.put(market, marketCache);
        }
        return MARKET_STORAGE_DATA_MAP.get(market).getSales();
    }

    @Override
    public Optional<SaleCache> removeSaleItem(String market, String saleId) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            return MARKET_STORAGE_DATA_MAP.get(market).removeSale(saleId);
        }
        return Optional.empty();
    }

    @Override
    public boolean addSale(String market, SaleCache saleItem) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            MARKET_STORAGE_DATA_MAP.get(market).addSale(saleItem.getSaleUUID(), saleItem);
            return true;
        }
        return false;
    }

    @Override
    public void addLog(SaleLog log) {
        SaleCache saleCache = log.getSaleCache();
        ConfigurationSection section = new YamlConfiguration(), sale = new YamlConfiguration();
        section.set("triggerPlayerUUID", log.getTriggerPlayerUUID().toString());
        section.set("triggerTime", log.getTriggerTime());
        section.set("market", log.getSourceMarket());
        sale.set("saleUUID", saleCache.getSaleUUID());
        sale.set("ownerUUID", saleCache.getOwnerUUID());
        sale.set("ownerName", saleCache.getOwnerName());
        sale.set("payType", saleCache.getPayType().name());
        sale.set("price", saleCache.getPrice());
        sale.set("postTime", saleCache.getPostTime());
        sale.set("saleItem", saleCache.getSaleItem());
        section.set("sale", sale);
        LOG_SECTION_MAP.put(String.valueOf(log.getTriggerTime()), section);
    }

    @Override
    public void save(String market, Map<String, SaleCache> map) {
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/saleData/", market + ".yml");
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, SaleCache> entry : map.entrySet()) {
            data.set(entry.getKey(), entry.getValue().toSection());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot save sale data: " + market);
        }
    }

    @Override
    public void removeTimeOutItem() {
        MARKET_STORAGE_DATA_MAP.forEach((k, v) -> {
            MarketData marketConfigData = DataContainer.MARKET_DATA.get(k);
            if (marketConfigData == null) {
                return;
            }
            // 开始计算
            Iterator<Map.Entry<String, SaleCache>> iterator = v.getSales().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SaleCache> entry = iterator.next();
                int second = (int) ((System.currentTimeMillis() - entry.getValue().getPostTime()) / 1000L);
                if (second >= marketConfigData.getEffectiveTime()) {
                    iterator.remove();
                    UUID uuid = UUID.fromString(entry.getValue().getOwnerUUID());
                    ServerMarket.getStorageHandler().addItemToStore(uuid, entry.getValue().getSaleItem(), "timeout");
                }
            }
        });
    }

    @Override
    public void saveAll() {
        this.saveAllPlayerData();
        for (Map.Entry<String, MarketCache> entry : MARKET_STORAGE_DATA_MAP.entrySet()) {
            this.save(entry.getKey(), entry.getValue().getSales());
        }
        this.saveResults();
    }

    @Override
    public void saveAllPlayerData() {
        for (Map.Entry<UUID, PlayerCache> entry : PLAYER_DATA_MAP.entrySet()) {
            this.savePlayerData(entry.getValue(), false);
        }
    }

    @Override
    public void savePlayerData(UUID uuid, boolean removeCache) {
        this.getPlayerDataByCache(uuid).ifPresent((playerCache) -> this.savePlayerData(playerCache, removeCache));
    }

    @Override
    public void savePlayerData(PlayerCache playerCache, boolean removeCache) {
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/data/", playerCache.getOwnerUniqueId().toString() + ".yml");
        FileConfiguration data = playerCache.saveToConfiguration();
        try {
            data.save(file);
        } catch (IOException e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot save player data: " + playerCache.getOwnerUniqueId().toString());
        }
        if (removeCache) {
            PLAYER_DATA_MAP.remove(playerCache.getOwnerUniqueId());
        }
    }

    @Override
    public PlayerCache getOrLoadPlayerCache(UUID uuid, boolean forceLoad) {
        if (PLAYER_DATA_MAP.containsKey(uuid)) {
            return PLAYER_DATA_MAP.get(uuid);
        }
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/data/", uuid + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        PLAYER_DATA_MAP.put(uuid, new PlayerCache(uuid, data));
        return PLAYER_DATA_MAP.get(uuid);
    }

    @Override
    public Optional<PlayerCache> getPlayerDataByCache(UUID uuid) {
        if (PLAYER_DATA_MAP.containsKey(uuid)) {
            return Optional.of(PLAYER_DATA_MAP.get(uuid));
        }
        return Optional.empty();
    }

    @Override
    public boolean addItemToStore(UUID uuid, ItemStack itemStack, String reason) {
        Optional<PlayerCache> optional = this.getPlayerDataByCache(uuid);
        try {
            if (optional.isPresent()) {
                optional.get().addStoreItem(itemStack, reason);
            } else {
                PlayerCache playerCache = this.getOrLoadPlayerCache(uuid, true);
                playerCache.addStoreItem(itemStack, reason);
                this.savePlayerData(playerCache, true);
            }
            return true;
        } catch (Exception e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot add item to store: " + uuid.toString());
            return false;
        }
    }

    @Override
    public boolean addItemToStore(UUID uuid, SaleCache saleItem, String reason) {
        Optional<PlayerCache> optional = this.getPlayerDataByCache(uuid);
        try {
            if (optional.isPresent()) {
                optional.get().addStoreItem(saleItem, reason);
            } else {
                PlayerCache playerCache = this.getOrLoadPlayerCache(uuid, true);
                playerCache.addStoreItem(saleItem, reason);
                this.savePlayerData(playerCache, true);
            }
            return true;
        } catch (Exception e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot add item to store: " + uuid.toString());
            return false;
        }
    }

    @Override
    public ItemStack removeStoreItem(UUID uuid, String storeItemId) {
        Optional<PlayerCache> optional = this.getPlayerDataByCache(uuid);
        try {
            if (optional.isPresent()) {
                return optional.get().removeStoreItem(storeItemId);
            } else {
                PlayerCache playerCache = this.getOrLoadPlayerCache(uuid, true);
                ItemStack itemStack = playerCache.removeStoreItem(storeItemId);
                this.savePlayerData(playerCache, true);
                return itemStack;
            }
        } catch (Exception e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot add item to store: " + uuid.toString());
            return null;
        }
    }

    @Override
    public void addOfflineTransaction(OfflineTransactionData data) {
        RESULT_DATA.put(UUID.randomUUID().toString(), data);
    }

    @Override
    public boolean removeOfflineTransaction(String key) {
        RESULT_DATA.remove(key);
        return true;
    }

    @Override
    public Map<String, OfflineTransactionData> getOfflineTransactionByPlayer(UUID ownerUniqueId) {
        return RESULT_DATA.entrySet().stream()
                .filter((s) -> s.getValue().getOwnerUniqueId().equals(ownerUniqueId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

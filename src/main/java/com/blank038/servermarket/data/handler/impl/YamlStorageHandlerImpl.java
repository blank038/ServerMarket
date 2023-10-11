package com.blank038.servermarket.data.handler.impl;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.cache.market.MarketData;
import com.blank038.servermarket.data.cache.market.MarketStorageData;
import com.blank038.servermarket.data.cache.other.OfflineTransactionData;
import com.blank038.servermarket.data.cache.player.PlayerData;
import com.blank038.servermarket.data.cache.sale.SaleItem;
import com.blank038.servermarket.data.handler.AbstractStorageHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Blank038
 */
public class YamlStorageHandlerImpl extends AbstractStorageHandler {
    private static final Map<String, OfflineTransactionData> RESULT_DATA = new HashMap<>();
    private static final Map<String, MarketStorageData> MARKET_STORAGE_DATA_MAP = new HashMap<>();

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


    @Override
    public void initialize() {
    }

    @Override
    public void reload() {
        File dataFolder = new File(ServerMarket.getInstance().getDataFolder(), "data");
        dataFolder.mkdirs();
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
        MarketStorageData marketStorageData = new MarketStorageData(market);
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/saleData/", market + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            SaleItem saleItem = new SaleItem(data.getConfigurationSection(key));
            marketStorageData.addSale(saleItem.getSaleUUID(), saleItem);
        }
        MARKET_STORAGE_DATA_MAP.put(market, marketStorageData);
    }

    @Override
    public boolean hasSale(String market, String saleId) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            return MARKET_STORAGE_DATA_MAP.get(market).hasSale(saleId);
        }
        return false;
    }

    @Override
    public Optional<SaleItem> getSaleItem(String market, String saleId) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            return MARKET_STORAGE_DATA_MAP.get(market).getSale(saleId);
        }
        return Optional.empty();
    }

    @Override
    public Map<String, SaleItem> getSaleItemsByMarket(String market) {
        if (!MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            MarketStorageData marketStorageData = new MarketStorageData(market);
            MARKET_STORAGE_DATA_MAP.put(market, marketStorageData);
        }
        return MARKET_STORAGE_DATA_MAP.get(market).getSales();
    }

    @Override
    public Optional<SaleItem> removeSaleItem(String market, String saleId) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            return MARKET_STORAGE_DATA_MAP.get(market).removeSale(saleId);
        }
        return Optional.empty();
    }

    @Override
    public boolean addSale(String market, SaleItem saleItem) {
        if (MARKET_STORAGE_DATA_MAP.containsKey(market)) {
            MARKET_STORAGE_DATA_MAP.get(market).addSale(saleItem.getSaleUUID(), saleItem);
            return true;
        }
        return false;
    }

    @Override
    public void save(String market, Map<String, SaleItem> map) {
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/saleData/", market + ".yml");
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, SaleItem> entry : map.entrySet()) {
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
            MarketData marketConfigData = MarketData.MARKET_DATA.get(k);
            if (marketConfigData == null) {
                return;
            }
            // 开始计算
            Iterator<Map.Entry<String, SaleItem>> iterator = v.getSales().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SaleItem> entry = iterator.next();
                int second = (int) ((System.currentTimeMillis() - entry.getValue().getPostTime()) / 1000L);
                if (second >= marketConfigData.getEffectiveTime()) {
                    iterator.remove();
                    UUID uuid = UUID.fromString(entry.getValue().getOwnerUUID());
                    ServerMarket.getStorageHandler().addItemToStore(uuid, entry.getValue().getSaleItem());
                }
            }
        });
    }

    @Override
    public void saveAll() {
        this.saveAllPlayerData();
        for (Map.Entry<String, MarketStorageData> entry : MARKET_STORAGE_DATA_MAP.entrySet()) {
            this.save(entry.getKey(), entry.getValue().getSales());
        }
        this.saveResults();
    }

    @Override
    public void saveAllPlayerData() {
        for (Map.Entry<UUID, PlayerData> entry : PLAYER_DATA_MAP.entrySet()) {
            this.savePlayerData(entry.getValue(), false);
        }
    }

    @Override
    public void savePlayerData(UUID uuid, boolean removeCache) {
        this.getPlayerDataByCache(uuid).ifPresent((playerData) -> this.savePlayerData(playerData, removeCache));
    }

    @Override
    public void savePlayerData(PlayerData playerData, boolean removeCache) {
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/data/", playerData.getOwnerUniqueId().toString() + ".yml");
        FileConfiguration data = new YamlConfiguration();
        data.set("info", playerData.getRecords());
        for (Map.Entry<String, ItemStack> entry : playerData.getStoreItems().entrySet()) {
            data.set("items." + entry.getKey(), entry.getValue());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot save player data: " + playerData.getOwnerUniqueId().toString());
        }
        if (removeCache) {
            PLAYER_DATA_MAP.remove(playerData.getOwnerUniqueId());
        }
    }

    @Override
    public PlayerData getOrLoadPlayerCache(UUID uuid) {
        if (PLAYER_DATA_MAP.containsKey(uuid)) {
            return PLAYER_DATA_MAP.get(uuid);
        }
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/data/", uuid + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        PLAYER_DATA_MAP.put(uuid, new PlayerData(uuid, data));
        return PLAYER_DATA_MAP.get(uuid);
    }

    @Override
    public Optional<PlayerData> getPlayerDataByCache(UUID uuid) {
        if (PLAYER_DATA_MAP.containsKey(uuid)) {
            return Optional.of(PLAYER_DATA_MAP.get(uuid));
        }
        return Optional.empty();
    }

    @Override
    public boolean addItemToStore(UUID uuid, ItemStack itemStack) {
        Optional<PlayerData> optional = this.getPlayerDataByCache(uuid);
        try {
            if (optional.isPresent()) {
                optional.get().addStoreItem(itemStack);
            } else {
                PlayerData playerData = this.getOrLoadPlayerCache(uuid);
                playerData.addStoreItem(itemStack);
                this.savePlayerData(playerData, true);
            }
            return true;
        } catch (Exception e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot add item to store: " + uuid.toString());
            return false;
        }
    }

    @Override
    public boolean addItemToStore(UUID uuid, SaleItem saleItem) {
        Optional<PlayerData> optional = this.getPlayerDataByCache(uuid);
        try {
            if (optional.isPresent()) {
                optional.get().addStoreItem(saleItem);
            } else {
                PlayerData playerData = this.getOrLoadPlayerCache(uuid);
                playerData.addStoreItem(saleItem);
                this.savePlayerData(playerData, true);
            }
            return true;
        } catch (Exception e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot add item to store: " + uuid.toString());
            return false;
        }
    }

    @Override
    public ItemStack removeStoreItem(UUID uuid, String storeItemId) {
        Optional<PlayerData> optional = this.getPlayerDataByCache(uuid);
        try {
            if (optional.isPresent()) {
                return optional.get().removeStoreItem(storeItemId);
            } else {
                PlayerData playerData = this.getOrLoadPlayerCache(uuid);
                ItemStack itemStack = playerData.removeStoreItem(storeItemId);
                this.savePlayerData(playerData, true);
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

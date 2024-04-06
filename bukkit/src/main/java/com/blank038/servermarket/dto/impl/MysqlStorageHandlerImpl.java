package com.blank038.servermarket.dto.impl;

import com.aystudio.core.bukkit.thread.BlankThread;
import com.aystudio.core.bukkit.thread.ThreadProcessor;
import com.aystudio.core.bukkit.util.mysql.MySqlStorageHandler;
import com.blank038.servermarket.dto.AbstractStorageHandler;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.cache.other.OfflineTransactionData;
import com.blank038.servermarket.internal.cache.other.SaleLog;
import com.blank038.servermarket.internal.cache.player.PlayerCache;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.internal.enums.PayType;
import com.blank038.servermarket.internal.i18n.I18n;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class MysqlStorageHandlerImpl extends AbstractStorageHandler {
    @Getter
    private static MySqlStorageHandler storageHandler;

    private final String playersTable = "servermarket_players",
            salesTable = "servermarket_sales",
            logsTable = "servermarket_logs",
            offlineTransactionsTable = "servermarket_offline_transactions";

    @Override
    public void initialize() {
        String[] sqlArray = {
                "CREATE TABLE IF NOT EXISTS `servermarket_players`( `player_uuid` VARCHAR(36) NOT NULL, `data` TEXT, `locked` TINYINT, PRIMARY KEY (`player_uuid`)) ENGINE = InnoDB DEFAULT CHARSET = utf8;",
                "CREATE TABLE IF NOT EXISTS `servermarket_sales` ( `sale_uuid` VARCHAR(36) NOT NULL, `market` VARCHAR(20) NOT NULL, `owner_name` VARCHAR(20) NOT NULL, `owner_uuid` VARCHAR(36) NOT NULL, `pay_type` VARCHAR(20) NOT NULL, `eco_type` VARCHAR(50), `price` int, `data` TEXT, `post_time` TIMESTAMP NOT NULL, PRIMARY KEY (`sale_uuid`) ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;",
                "CREATE TABLE IF NOT EXISTS `servermarket_logs` ( `id` INT AUTO_INCREMENT, `trigger_time` TIMESTAMP, `trigger_uuid` VARCHAR(36) NOT NULL, `market` VARCHAR(50), `log_type` VARCHAR(10) NOT NULL, `data` TEXT, PRIMARY KEY (`id`) ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;",
                "CREATE TABLE IF NOT EXISTS `servermarket_offline_transactions` ( `id` INT AUTO_INCREMENT, `owner_uuid` VARCHAR(36) NOT NULL, `market` VARCHAR(50) NOT NULL, `amount` INT, `pay_type` VARCHAR(20) NOT NULL, `eco_type` VARCHAR(50), PRIMARY KEY (`id`) ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;"

        };
        storageHandler = new MySqlStorageHandler(
                ServerMarket.getInstance(),
                ServerMarket.getInstance().getConfig().getString("data-option.url"),
                ServerMarket.getInstance().getConfig().getString("data-option.user"),
                ServerMarket.getInstance().getConfig().getString("data-option.password"),
                sqlArray
        );
        storageHandler.setReconnectionQueryTable("servermarket_players");
    }

    @Override
    public void reload() {
    }

    @Override
    public boolean hasSale(String market, String saleId) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        storageHandler.connect((preparedStatement) -> {
            try {
                preparedStatement.setString(1, saleId);
                preparedStatement.setString(2, market);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    atomicBoolean.set(true);
                }
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to check sale item.");
            }
        }, "SELECT sale_uuid FROM " + salesTable + " WHERE sale_uuid = ? AND market = ?;");
        return atomicBoolean.get();
    }

    @Override
    public int getSaleCountByPlayer(UUID uuid, String market) {
        AtomicInteger count = new AtomicInteger(0);
        storageHandler.connect((preparedStatement) -> {
            try {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, market);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    count.set(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to get sale count.");
            }
        }, "SELECT count(*) FROM " + this.salesTable + " WHERE owner_uuid = ? AND market = ?;");
        return count.get();
    }

    @Override
    public Optional<SaleCache> getSaleItem(String market, String saleId) {
        AtomicReference<SaleCache> reference = new AtomicReference<>(null);
        storageHandler.connect((preparedStatement) -> {
            try {
                preparedStatement.setString(1, saleId);
                preparedStatement.setString(2, market);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    byte[] bytes = Base64.getDecoder().decode(resultSet.getString(7).getBytes(StandardCharsets.UTF_8));
                    FileConfiguration data = new YamlConfiguration();
                    data.loadFromString(new String(bytes, StandardCharsets.UTF_8));
                    ItemStack itemStack = new ItemStack(data.getItemStack("item"));
                    SaleCache saleCache = new SaleCache(
                            saleId,
                            resultSet.getString(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            itemStack,
                            PayType.valueOf(resultSet.getString(4)),
                            resultSet.getString(5),
                            resultSet.getInt(6) * 0.01,
                            resultSet.getTimestamp(8).getTime());
                    reference.set(saleCache);
                }
            } catch (SQLException | InvalidConfigurationException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to check sale item.");
            }
        }, "SELECT market,owner_uuid,owner_name,pay_type,eco_type,price,data,post_time FROM " + salesTable + " WHERE sale_uuid = ? AND market = ?;");
        return Optional.of(reference.get());
    }

    @Override
    public Map<String, SaleCache> getSaleItemsByMarket(String market) {
        Map<String, SaleCache> saleCacheMap = new HashMap<>();
        storageHandler.connect((preparedStatement) -> {
            try {
                preparedStatement.setString(1, market);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    byte[] bytes = Base64.getDecoder().decode(resultSet.getString(7).getBytes(StandardCharsets.UTF_8));
                    FileConfiguration data = new YamlConfiguration();
                    data.loadFromString(new String(bytes, StandardCharsets.UTF_8));
                    ItemStack itemStack = new ItemStack(data.getItemStack("item"));
                    SaleCache saleCache = new SaleCache(
                            resultSet.getString(1),
                            market,
                            resultSet.getString(2),
                            resultSet.getString(3),
                            itemStack,
                            PayType.valueOf(resultSet.getString(4)),
                            resultSet.getString(5),
                            resultSet.getInt(6) * 0.01,
                            resultSet.getTimestamp(8).getTime());
                    saleCacheMap.put(saleCache.getSaleUUID(), saleCache);
                }
            } catch (SQLException | InvalidConfigurationException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to check sale item.");
            }
        }, "SELECT sale_uuid,owner_uuid,owner_name,pay_type,eco_type,price,data,post_time FROM " + salesTable + " WHERE market = ?;");
        return saleCacheMap;
    }

    @Override
    public Optional<SaleCache> removeSaleItem(String market, String saleId) {
        AtomicReference<SaleCache> reference = new AtomicReference<>(null);
        storageHandler.connect((preparedStatement) -> {
            try {
                Optional<SaleCache> saleCache = this.getSaleItem(market, saleId);
                if (saleCache.isPresent()) {
                    preparedStatement.setString(1, saleId);
                    preparedStatement.setString(2, market);
                    if (preparedStatement.executeUpdate() > 0) {
                        reference.set(saleCache.get());
                    }
                }
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to check sale item.");
            }
        }, "DELETE FROM " + salesTable + " WHERE sale_uuid = ? AND market = ?;");
        return Optional.of(reference.get());
    }

    @Override
    public boolean addSale(String market, SaleCache saleItem) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        storageHandler.connect((preparedStatement) -> {
            try {
                FileConfiguration data = new YamlConfiguration();
                data.set("item", saleItem.getSaleItem());
                String dataString = new String(Base64.getEncoder().encode(data.saveToString().getBytes(StandardCharsets.UTF_8)));

                preparedStatement.setString(1, saleItem.getSaleUUID());
                preparedStatement.setString(2, market);
                preparedStatement.setString(3, saleItem.getOwnerName());
                preparedStatement.setString(4, saleItem.getOwnerUUID());
                preparedStatement.setString(5, saleItem.getPayType().name());
                preparedStatement.setString(6, saleItem.getEcoType());
                preparedStatement.setInt(7, (int) (saleItem.getPrice() * 100));
                preparedStatement.setString(8, dataString);
                preparedStatement.setTimestamp(9, new java.sql.Timestamp(saleItem.getPostTime()));
                if (preparedStatement.executeUpdate() > 0) {
                    atomicBoolean.set(true);
                }
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to check sale item.");
            }
        }, "INSERT INTO " + salesTable + " (sale_uuid,market,owner_name,owner_uuid,pay_type,eco_type,price,data,post_time) VALUES (?,?,?,?,?,?,?,?,?);");
        return atomicBoolean.get();
    }

    @Override
    public void addLog(SaleLog log) {
        storageHandler.connect((preparedStatement) -> {
            try {
                FileConfiguration data = new YamlConfiguration();
                data.set("item", log.getSaleCache().getSaleItem());
                String dataString = new String(Base64.getEncoder().encode(data.saveToString().getBytes(StandardCharsets.UTF_8)));

                preparedStatement.setString(1, log.getTriggerPlayerUUID().toString());
                preparedStatement.setString(2, log.getSourceMarket());
                preparedStatement.setString(3, log.getLogType().name());
                preparedStatement.setString(4, dataString);
                preparedStatement.setTimestamp(5, new java.sql.Timestamp(log.getTriggerTime()));
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to check sale item.");
            }
        }, "INSERT INTO " + logsTable + " (trigger_uuid,market,log_type,data,trigger_time) VALUES (?,?,?,?,?);");
    }

    @Override
    public void save(String market, Map<String, SaleCache> map) {
        // don't do anything
    }

    @Override
    public void removeTimeOutItem() {
        DataContainer.MARKET_DATA.forEach((k, v) -> {
            for (Map.Entry<String, SaleCache> entry : this.getSaleItemsByMarket(k).entrySet()) {
                int second = (int) ((System.currentTimeMillis() - entry.getValue().getPostTime()) / 1000L);
                if (second >= v.getEffectiveTime()) {
                    UUID uuid = UUID.fromString(entry.getValue().getOwnerUUID());
                    ServerMarket.getStorageHandler().removeSaleItem(k, entry.getKey()).ifPresent((sale) -> {
                        ServerMarket.getStorageHandler().addItemToStore(uuid, sale.getSaleItem(), "timeout");
                    });
                }
            }
        });
    }

    @Override
    public void saveAll() {
        this.saveAllPlayerData();
    }

    @Override
    public void savePlayerData(UUID uuid, boolean removeCache) {
        Optional<PlayerCache> optional = this.getPlayerDataByCache(uuid);
        optional.ifPresent(playerCache -> this.savePlayerData(playerCache, removeCache));
    }

    @Override
    public void savePlayerData(PlayerCache playerCache, boolean removeCache) {
        if (playerCache == null) {
            return;
        }
        if (playerCache.isNewData()) {
            storageHandler.connect((preparedStatement) -> {
                try {
                    FileConfiguration data = playerCache.saveToConfiguration();
                    String dataString = new String(Base64.getEncoder().encode(data.saveToString().getBytes(StandardCharsets.UTF_8)));

                    preparedStatement.setString(1, playerCache.getOwnerUniqueId().toString());
                    preparedStatement.setString(2, dataString);
                    preparedStatement.setInt(3, 0);
                    if (preparedStatement.executeUpdate() > 0) {
                        playerCache.setNewData(false);
                    }
                } catch (SQLException e) {
                    ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to save player data.");
                }
            }, "INSERT INTO " + playersTable + " (player_uuid,data,locked) VALUES (?,?,?);");
        } else {
            storageHandler.connect((preparedStatement) -> {
                try {
                    FileConfiguration data = playerCache.saveToConfiguration();
                    String dataString = new String(Base64.getEncoder().encode(data.saveToString().getBytes(StandardCharsets.UTF_8)));

                    preparedStatement.setString(1, dataString);
                    preparedStatement.setString(2, playerCache.getOwnerUniqueId().toString());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to save player data.");
                }
            }, "UPDATE " + playersTable + " SET data = ? WHERE player_uuid = ?;");
        }
        if (removeCache) {
            this.removePlyerData(playerCache.getOwnerUniqueId());
        }
    }

    @Override
    public void setLock(UUID uuid, boolean locked) {
        storageHandler.connect((preparedStatement) -> {
            try {
                preparedStatement.setInt(1, locked ? 1 : 0);
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to set player lock.");
            }
        }, "UPDATE " + playersTable + " SET locked = ? WHERE player_uuid = ?;");
    }

    @Override
    public boolean isLocked(UUID uuid) {
        AtomicBoolean result = new AtomicBoolean(false);
        storageHandler.connect((preparedStatement) -> {
            try {
                preparedStatement.setString(1, uuid.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    result.set(resultSet.getInt(1) == 1);
                }
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to check player lock.");
            }
        }, "SELECT locked FROM " + playersTable + " WHERE player_uuid = ?;");
        return result.get();
    }

    @Override
    public PlayerCache getOrLoadPlayerCache(UUID uuid, boolean forceLoad) {
        if (PLAYER_DATA_MAP.containsKey(uuid)) {
            return PLAYER_DATA_MAP.get(uuid);
        }
        if (forceLoad) {
            AtomicReference<PlayerCache> reference = new AtomicReference<>(null);
            storageHandler.connect((preparedStatement) -> {
                try {
                    preparedStatement.setString(1, uuid.toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        byte[] bytes = Base64.getDecoder().decode(resultSet.getString(1).getBytes(StandardCharsets.UTF_8));
                        FileConfiguration data = new YamlConfiguration();
                        data.loadFromString(new String(bytes, StandardCharsets.UTF_8));
                        PlayerCache playerCache = new PlayerCache(uuid, data);
                        PLAYER_DATA_MAP.put(uuid, playerCache);
                        reference.set(playerCache);
                    } else {
                        PlayerCache playerCache = new PlayerCache(uuid, new YamlConfiguration(), true);
                        PLAYER_DATA_MAP.put(uuid, playerCache);
                        reference.set(playerCache);
                    }
                } catch (SQLException | InvalidConfigurationException e) {
                    ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to load player data.");
                }
            }, "SELECT data FROM " + playersTable + " WHERE player_uuid = ?;");
            return reference.get();
        } else {
            Player player = Bukkit.getPlayer(uuid);
            if (this.pluign.getConfig().getBoolean("data-option.pull-notify")
                    && player != null && player.isOnline()) {
                player.sendMessage(I18n.getStrAndHeader("pulling-start"));
            }
            ThreadProcessor.crateTask(this.pluign, new BlankThread(10) {
                private int count;

                @Override
                public void run() {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) {
                        this.cancel();
                        return;
                    }
                    if (!MysqlStorageHandlerImpl.this.isLocked(player.getUniqueId())) {
                        loadData(player);
                        this.cancel();
                    } else {
                        count++;
                        if (count > MysqlStorageHandlerImpl.this.pluign.getConfig().getInt("data-option.time-out")) {
                            loadData(player);
                            this.cancel();
                        }
                    }
                }
            });
        }
        return null;
    }

    private void loadData(Player player) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            this.setLock(player.getUniqueId(), true);
            return true;
        });
        future.exceptionally((e) -> false);
        future.thenAccept((v) -> {
            if (v) {
                if (!player.isOnline()) {
                    return;
                }
                storageHandler.connect((preparedStatement) -> {
                    try {
                        preparedStatement.setString(1, player.getUniqueId().toString());
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            byte[] bytes = Base64.getDecoder().decode(resultSet.getString(1).getBytes(StandardCharsets.UTF_8));
                            FileConfiguration data = new YamlConfiguration();
                            data.loadFromString(new String(bytes, StandardCharsets.UTF_8));
                            PlayerCache playerCache = new PlayerCache(player.getUniqueId(), data);
                            PLAYER_DATA_MAP.put(player.getUniqueId(), playerCache);
                        } else {
                            PlayerCache playerCache = new PlayerCache(player.getUniqueId(), new YamlConfiguration(), true);
                            PLAYER_DATA_MAP.put(player.getUniqueId(), playerCache);
                        }
                    } catch (SQLException | InvalidConfigurationException e) {
                        ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to load player data.");
                    }
                }, "SELECT data FROM " + playersTable + " WHERE player_uuid = ?;");
                if (this.pluign.getConfig().getBoolean("data-option.pull-notify")) {
                    player.sendMessage(I18n.getStrAndHeader("pulling-completed"));
                }
            }
        });
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
        if (optional.isPresent()) {
            optional.get().addStoreItem(itemStack, reason);
        } else {
            PlayerCache playerCache = this.getOrLoadPlayerCache(uuid, true);
            playerCache.addStoreItem(itemStack, reason);
            this.savePlayerData(playerCache, true);
        }
        return true;
    }

    @Override
    public boolean addItemToStore(UUID uuid, SaleCache saleItem, String reason) {
        Optional<PlayerCache> optional = this.getPlayerDataByCache(uuid);
        if (optional.isPresent()) {
            optional.get().addStoreItem(saleItem, reason);
        } else {
            PlayerCache playerCache = this.getOrLoadPlayerCache(uuid, true);
            playerCache.addStoreItem(saleItem, reason);
            this.savePlayerData(playerCache, true);
        }
        return true;
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
        storageHandler.connect((preparedStatement) -> {
            try {
                preparedStatement.setString(1, data.getOwnerUniqueId().toString());
                preparedStatement.setString(2, data.getSourceMarket());
                preparedStatement.setInt(3, (int) (data.getAmount() * 100));
                preparedStatement.setString(4, data.getPayType().name());
                preparedStatement.setString(5, data.getEconomyType());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to add offline transaction.");
            }
        }, "INSERT INTO " + offlineTransactionsTable + " (owner_uuid,market,amount,pay_type,eco_type) VALUES (?,?,?,?,?);");
    }

    @Override
    public boolean removeOfflineTransaction(String key) {
        AtomicBoolean result = new AtomicBoolean(false);
        storageHandler.connect((preparedStatement) -> {
            try {
                preparedStatement.setInt(1, Integer.parseInt(key));
                if (preparedStatement.executeUpdate() > 0) {
                    result.set(true);
                }
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to remove offline transaction.");
            }
        }, "DELETE FROM " + this.offlineTransactionsTable + " WHERE id = ?;");
        return result.get();
    }

    @Override
    public Map<String, OfflineTransactionData> getOfflineTransactionByPlayer(UUID ownerUniqueId) {
        Map<String, OfflineTransactionData> map = new HashMap<>();
        storageHandler.connect((preparedStatement) -> {
            try {
                preparedStatement.setString(1, ownerUniqueId.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    OfflineTransactionData data = new OfflineTransactionData(
                            resultSet.getString(2),
                            ownerUniqueId,
                            PayType.valueOf(resultSet.getString(3)),
                            resultSet.getString(4),
                            resultSet.getInt(5) * 0.01
                    );
                    map.put(String.valueOf(resultSet.getInt(1)), data);
                }
            } catch (SQLException e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to get offline transactions.");
            }
        }, "SELECT id,market,pay_type,eco_type,amount FROM " + offlineTransactionsTable + " WHERE owner_uuid = ?;");
        return map;
    }
}

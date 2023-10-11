package com.blank038.servermarket.data.handler;

import com.blank038.servermarket.data.cache.other.OfflineTransactionData;
import com.blank038.servermarket.data.cache.other.SaleLog;
import com.blank038.servermarket.data.cache.player.PlayerCache;
import com.blank038.servermarket.data.cache.sale.SaleCache;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Blank038
 */
public interface IStorageHandler {

    /**
     * Called when IStorageHandler is initialized.
     */
    void initialize();

    /**
     * Called when IStorageHandler is load/reload.
     */
    void reload();

    /**
     * 存储控制器被载入时, 用于载入本地数据
     *
     * @param market 市场编号
     */
    void load(String market);

    /**
     * 判断商品是否存在
     *
     * @param market 市场编号
     * @param saleId 商品编号
     * @return 是否存在
     */
    boolean hasSale(String market, String saleId);

    /**
     * 获取商品数据
     *
     * @param market 市场编号
     * @param saleId 商品编号
     * @return 商品数据
     */
    Optional<SaleCache> getSaleItem(String market, String saleId);

    /**
     * 获取指定市场所有商品
     *
     * @param market 目标市场
     * @return 市场商品集合
     */
    Map<String, SaleCache> getSaleItemsByMarket(String market);

    /**
     * 从市场中移除商品并返回商品数据, 如果不存在则返回 null
     *
     * @param market 市场编号
     * @param saleId 商品编号
     * @return 商品数据
     */
    Optional<SaleCache> removeSaleItem(String market, String saleId);

    /**
     * 增加商品至市场
     *
     * @param market   市场编号
     * @param saleItem 商品数据
     */
    boolean addSale(String market, SaleCache saleItem);

    /**
     * Add a sale log
     *
     * @param log sale log
     */
    void addLog(SaleLog log);

    /**
     * 保存商品数据, 仅 YAML 模式下生效
     *
     * @param market 市场名
     * @param map    商品数据
     */
    void save(String market, Map<String, SaleCache> map);

    /**
     * 移除超时商品
     */
    void removeTimeOutItem();

    /**
     * 保存所有数据, 仅 YAML 有效
     */
    void saveAll();

    void saveAllPlayerData();

    /**
     * 存储玩家数据
     *
     * @param uuid 目标玩家
     */
    void savePlayerData(UUID uuid, boolean removeCache);

    /**
     * 存储玩家数据
     *
     * @param playerCache 玩家数据
     */
    void savePlayerData(PlayerCache playerCache, boolean removeCache);

    /**
     * 获取玩家缓存数据，如果数据不存在则载入
     *
     * @param uuid 目标玩家
     * @return 玩家数据
     */
    PlayerCache getOrLoadPlayerCache(UUID uuid);

    /**
     * 从缓存中获取玩家数据，如果数据不存在则返回 Optional.empty()
     *
     * @param uuid 目标玩家
     * @return 玩家数据
     */
    Optional<PlayerCache> getPlayerDataByCache(UUID uuid);

    /**
     * 增加物品至玩家暂存箱
     *
     * @param uuid      目标玩家
     * @param itemStack 存储物品
     * @return 是否成功
     */
    boolean addItemToStore(UUID uuid, ItemStack itemStack, String reason);

    /**
     * 增加物品至玩家暂存箱并记录
     *
     * @param uuid     目标玩家
     * @param saleItem 存储商品
     * @return 是否成功
     */
    boolean addItemToStore(UUID uuid, SaleCache saleItem, String reason);

    /**
     * 从玩家暂存库中移除物品
     *
     * @param uuid        目标玩家
     * @param storeItemId 物品编号
     * @return 移除结果
     */
    ItemStack removeStoreItem(UUID uuid, String storeItemId);

    /**
     * Add an offline transaction record
     *
     * @param data offlineTransactionData
     */
    void addOfflineTransaction(OfflineTransactionData data);

    /**
     * Remove an offline transaction record
     *
     * @param key offlineTransactionData key
     * @return remove result
     */
    boolean removeOfflineTransaction(String key);

    /**
     * Get the player's offline transaction record Map
     *
     * @param ownerUniqueId target player uuid
     * @return offlineTransactionData Map
     */
    Map<String, OfflineTransactionData> getOfflineTransactionByPlayer(UUID ownerUniqueId);
}

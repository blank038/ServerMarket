package com.blank038.servermarket.data.handler;

import com.blank038.servermarket.data.cache.sale.SaleItem;

import java.util.Map;

/**
 * @author Blank038
 */
public interface IStorageHandler {

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
    SaleItem getSaleItem(String market, String saleId);

    /**
     * 从市场中移除商品并返回商品数据, 如果不存在则返回 null
     *
     * @param market 市场编号
     * @param saleId 商品编号
     * @return 商品数据
     */
    SaleItem removeSaleItem(String market, String saleId);

    /**
     * 增加商品至市场
     *
     * @param market   市场编号
     * @param saleItem 商品数据
     */
    void addSale(String market, SaleItem saleItem);

    /**
     * 保存商品数据, 仅 YAML 模式下生效
     *
     * @param market 市场名
     * @param map    商品数据
     */
    void save(String market, Map<String, SaleItem> map);

    /**
     * 移除超时商品
     */
    void removeTimeOutItem();

    /**
     * 保存所有数据, 仅 YAML 有效
     */
    void saveAll();
}

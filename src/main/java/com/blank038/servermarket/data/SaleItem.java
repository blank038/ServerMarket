package com.blank038.servermarket.data;

import com.blank038.servermarket.enums.PayType;
import org.bukkit.inventory.ItemStack;

public class SaleItem {
    // 存储物品的 UUID
    private final String saleUUID, ownerUUID, ownerName;
    // 商品对应的物品
    private final ItemStack itemStack;
    // 商品对应的货币
    private final PayType payType;
    // 价格
    private final double price;
    // 发布时间
    private final long postTime;

    /**
     * 全球市场内商品的构建类
     */
    public SaleItem(String saleUUID, String ownerUUID, String ownerName, ItemStack itemStack,
                    PayType payType, double price, long postTime) {
        this.saleUUID = saleUUID;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.itemStack = itemStack.clone();
        this.payType = payType;
        this.price = price;
        this.postTime = postTime;
    }

    public String getSaleUUID() {
        return saleUUID;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getPrice() {
        return price;
    }

    public ItemStack getSafeItem() {
        return itemStack;
    }

    public long getPostTime() {
        return postTime;
    }

    public PayType getPayType() {
        return payType;
    }
}
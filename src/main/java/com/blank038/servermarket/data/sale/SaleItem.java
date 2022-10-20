package com.blank038.servermarket.data.sale;

import com.blank038.servermarket.enums.PayType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * 全球市场内商品的构建类
 *
 * @author Blank038
 */
public class SaleItem {
    /**
     * 存储物品的 UUID
     */
    private final String saleUUID, ownerUUID, ownerName, ecoType;
    /**
     * 商品对应的物品
     */
    private final ItemStack itemStack;
    /**
     * 商品对应的货币
     */
    private final PayType payType;
    /**
     * 价格
     */
    private final double price;
    /**
     * 发布时间
     */
    private final long postTime;

    public SaleItem(ConfigurationSection section) {
        this.saleUUID = section.getString("sale-uuid");
        this.ownerName = section.getString("owner-name");
        this.ownerUUID = section.getString("owner-uuid");
        this.itemStack = section.getItemStack("sale-item");
        this.payType = PayType.valueOf(section.getString("pay-type"));
        this.ecoType = section.getString("eco-type", null);
        this.price = section.getInt("price");
        this.postTime = section.getLong("post-time");
    }

    public SaleItem(String saleUUID, String ownerUUID, String ownerName, ItemStack itemStack,
                    PayType payType, String ecoType, double price, long postTime) {
        this.saleUUID = saleUUID;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.itemStack = itemStack.clone();
        this.payType = payType;
        this.price = price;
        this.postTime = postTime;
        this.ecoType = ecoType;
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

    public String getEcoType() {
        return ecoType;
    }

    public ConfigurationSection toSection() {
        ConfigurationSection section = new YamlConfiguration();
        section.set("owner-uuid", ownerUUID);
        section.set("sale-uuid", getSaleUUID());
        section.set("owner-name", ownerName);
        section.set("sale-item", itemStack);
        section.set("pay-type", payType.name());
        section.set("price", price);
        section.set("post-time", postTime);
        section.set("eco-type", ecoType);
        return section;
    }
}
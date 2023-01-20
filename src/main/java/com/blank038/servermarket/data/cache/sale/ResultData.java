package com.blank038.servermarket.data.cache.sale;

import com.blank038.servermarket.enums.PayType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Blank038
 */
public class ResultData {
    public static final HashMap<String, ResultData> RESULT_DATA = new HashMap<>();

    private final double amount;
    private final PayType payType;
    private final UUID ownerUniqueId;
    private final String ecoType, sourceMarket;

    public ResultData(ConfigurationSection section) {
        this.amount = section.getDouble("amount");
        this.payType = PayType.valueOf(section.getString("pay-type"));
        this.ownerUniqueId = UUID.fromString(section.getString("owner-uuid"));
        this.ecoType = section.getString("eco-type", null);
        this.sourceMarket = section.getString("source-market");
    }

    public PayType getPayType() {
        return this.payType;
    }

    public double getResultAmount() {
        return this.amount;
    }

    public UUID getOwnerUUID() {
        return this.ownerUniqueId;
    }

    public String getEconmyType() {
        return this.ecoType;
    }

    public String getSourceMarket() {
        return this.sourceMarket;
    }

    public ConfigurationSection toSection() {
        ConfigurationSection section = new YamlConfiguration();
        section.set("amount", this.amount);
        section.set("pay-type", this.payType.name());
        section.set("owner-uuid", this.ownerUniqueId.toString());
        section.set("eco-type", this.ecoType);
        section.set("source-market", this.sourceMarket);
        return section;
    }
}

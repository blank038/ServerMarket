package com.blank038.servermarket.data;

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

    private final double RESULT_AMOUNT;
    private final PayType PAY_TYPE;
    private final UUID OWNER_UUID;
    private final String ECO_TYPE, SOURCE_MARKET;

    public ResultData(ConfigurationSection section) {
        this.RESULT_AMOUNT = section.getDouble("amount");
        this.PAY_TYPE = PayType.valueOf(section.getString("pay-type"));
        this.OWNER_UUID = UUID.fromString(section.getString("owner-uuid"));
        this.ECO_TYPE = section.getString("eco-type", null);
        this.SOURCE_MARKET = section.getString("source-market");
    }

    public PayType getPayType() {
        return this.PAY_TYPE;
    }

    public double getResultAmount() {
        return this.RESULT_AMOUNT;
    }

    public UUID getOwnerUUID() {
        return this.OWNER_UUID;
    }

    public String getEconmyType() {
        return this.ECO_TYPE;
    }

    public String getSourceMarket() {
        return this.SOURCE_MARKET;
    }

    public ConfigurationSection toSection() {
        ConfigurationSection section = new YamlConfiguration();
        section.set("amount", this.RESULT_AMOUNT);
        section.set("pay-type", this.PAY_TYPE.name());
        section.set("owner-uuid", this.OWNER_UUID);
        section.set("eco-type", section);
        section.set("source-market", this.SOURCE_MARKET);
        return section;
    }
}

package com.blank038.servermarket.data.cache.other;

import com.blank038.servermarket.enums.PayType;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.UUID;

/**
 * @author Blank038
 */
@Getter
public class OfflineTransactionData {

    private final double amount;
    private final PayType payType;
    private final UUID ownerUniqueId;
    private final String economyType, sourceMarket;

    public OfflineTransactionData(ConfigurationSection section) {
        this.amount = section.getDouble("amount");
        this.payType = PayType.valueOf(section.getString("pay-type"));
        this.ownerUniqueId = UUID.fromString(section.getString("owner-uuid"));
        this.economyType = section.getString("eco-type", null);
        this.sourceMarket = section.getString("source-market");
    }

    public OfflineTransactionData(String sourceMarket, UUID ownerUniqueId, PayType payType, String economyType, double amount) {
        this.sourceMarket = sourceMarket;
        this.ownerUniqueId = ownerUniqueId;
        this.payType = payType;
        this.economyType = economyType;
        this.amount = amount;
    }

    public ConfigurationSection toSection() {
        ConfigurationSection section = new YamlConfiguration();
        section.set("amount", this.amount);
        section.set("pay-type", this.payType.name());
        section.set("owner-uuid", this.ownerUniqueId.toString());
        section.set("eco-type", this.economyType);
        section.set("source-market", this.sourceMarket);
        return section;
    }
}

package com.blank038.servermarket.api.entity;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.api.event.MarketLoadEvent;
import com.blank038.servermarket.api.event.PlayerSaleEvent;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.economy.BaseEconomy;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.internal.enums.MarketStatus;
import com.blank038.servermarket.internal.enums.PayType;
import com.blank038.servermarket.api.handler.filter.FilterHandler;
import com.blank038.servermarket.api.handler.filter.impl.KeyFilterImpl;
import com.blank038.servermarket.api.handler.filter.interfaces.IFilter;
import com.blank038.servermarket.internal.gui.impl.MarketGui;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.internal.util.TextUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Blank038
 * @date 2021/03/05
 */
@SuppressWarnings(value = {"unused"})
@Getter
public class MarketData {

    private final File sourceFile;
    /**
     * 商品列表
     */
    private final Map<String, String> extraMap = new HashMap<>();
    private final String sourceId, marketKey, permission, shortCommand, economyType, displayName, economyName;
    private final IFilter deniedFilter;
    private final List<String> saleTypes;
    private final int min, max, effectiveTime;
    private final PayType paymentType;
    private final ConfigurationSection taxSection, shoutTaxSection, limitCountSection;
    private MarketStatus marketStatus;
    @Setter
    private boolean showSaleInfo, saleBroadcast;
    @Setter
    private String dateFormat, priceFormat;

    public MarketData(File file) {
        FileConfiguration options = YamlConfiguration.loadConfiguration(file);
        this.sourceFile = file;
        this.marketKey = file.getName().replace(".yml", "");
        this.sourceId = options.getString("source_id");
        this.permission = options.getString("permission");
        this.shortCommand = options.getString("short-command");
        this.displayName = TextUtil.formatHexColor(options.getString("display-name"));
        this.economyName = TextUtil.formatHexColor(options.getString("economy-name"));
        this.min = options.getInt("price.min");
        this.max = options.getInt("price.max");
        if (options.contains("extra-price")) {
            for (String key : options.getConfigurationSection("extra-price").getKeys(false)) {
                this.extraMap.put(key, options.getString("extra-price." + key));
            }
        }
        this.effectiveTime = options.getInt("effective_time");
        this.deniedFilter = new KeyFilterImpl()
                .addKeys(options.getStringList("black-list.type"))
                .addKeys(options.getStringList("black-list.lore"));
        this.saleTypes = options.getStringList("types");
        this.taxSection = options.getConfigurationSection("tax");
        this.shoutTaxSection = options.getConfigurationSection("shout-tax");
        this.limitCountSection = options.getConfigurationSection("limit-count");
        this.showSaleInfo = options.getBoolean("show-sale-info");
        this.saleBroadcast = options.getBoolean("sale-broadcast");
        this.dateFormat = options.getString("simple-date-format");
        this.priceFormat = options.getString("price-format", "%.1f");
        switch ((this.economyType = options.getString("vault-type").toLowerCase())) {
            case "vault":
                this.paymentType = PayType.VAULT;
                break;
            case "playerpoints":
                this.paymentType = PayType.PLAYER_POINTS;
                break;
            default:
                this.paymentType = PayType.NY_ECONOMY;
                break;
        }
        if (!BaseEconomy.PAY_TYPES.containsKey(this.paymentType)) {
            this.marketStatus = MarketStatus.ERROR;
            ServerMarket.getInstance().getConsoleLogger().log(false,
                    I18n.getProperties().getProperty("load-market-eco-not-exists").replace("%s", this.displayName));
        } else {
            this.marketStatus = MarketStatus.LOADED;
            try {
                ServerMarket.getStorageHandler().load(this.marketKey);
                ServerMarket.getInstance().getConsoleLogger().log(false,
                        I18n.getProperties().getProperty("load-market-completed").replace("%s", this.displayName));
            } catch (Exception ignored) {
                this.marketStatus = MarketStatus.ERROR;
                ServerMarket.getInstance().getConsoleLogger().log(false,
                        I18n.getProperties().getProperty("load-market-wrong-sale-item").replace("%s", this.displayName));
            }
        }
        DataContainer.MARKET_DATA.put(this.getMarketKey(), this);
        // Call the event MarketLoadEvent
        MarketLoadEvent event = new MarketLoadEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * 获取玩家在权限节点上的值
     *
     * @param section target section
     * @param player  target player
     * @param compareBig is bigger than
     * @return 最终值
     */
    public double getPermsValueForPlayer(ConfigurationSection section, Player player, boolean compareBig) {
        String header = section.getString("header");
        double tax = section.getDouble("node.default");
        for (String key : section.getConfigurationSection("node").getKeys(false)) {
            double tempTax = section.getDouble("node." + key);
            if (!player.hasPermission(header + "." + key)) {
                continue;
            }
            if ((!compareBig && tempTax < tax) || (compareBig && tempTax > tax)) {
                tax = tempTax;
            }
        }
        return tax;
    }

    public void tryBuySale(Player buyer, String uuid, boolean shift, int page, FilterHandler filter) {
        // 判断商品是否存在
        if (!ServerMarket.getStorageHandler().hasSale(this.sourceId, uuid)) {
            buyer.sendMessage(I18n.getStrAndHeader("error-sale"));
            return;
        }
        Optional<SaleCache> optionalSaleItem = ServerMarket.getStorageHandler().getSaleItem(this.sourceId, uuid);
        if (optionalSaleItem.isPresent()) {
            SaleCache saleItem = optionalSaleItem.get();
            if (saleItem.getOwnerUUID().equals(buyer.getUniqueId().toString())) {
                if (shift) {
                    ServerMarket.getStorageHandler().removeSaleItem(this.sourceId, uuid)
                            .ifPresent((sale) -> {
                                ServerMarket.getStorageHandler().addItemToStore(buyer.getUniqueId(), sale.getSaleItem(), "unsale");
                                buyer.sendMessage(I18n.getStrAndHeader("unsale"));
                                new MarketGui(this.marketKey, page, filter).openGui(buyer);
                            });
                } else {
                    buyer.sendMessage(I18n.getStrAndHeader("shift-unsale"));
                }
                return;
            }
            if (shift) {
                if (saleItem.getPrice() == 0) {
                    buyer.sendMessage(I18n.getStrAndHeader("error-sale"));
                    return;
                }
                if (BaseEconomy.getEconomyBridge(this.paymentType).balance(buyer, this.economyType) < saleItem.getPrice()) {
                    buyer.sendMessage(I18n.getStrAndHeader("lack-money")
                            .replace("%economy%", this.economyName));
                    return;
                }
                Optional<SaleCache> optional = ServerMarket.getStorageHandler().removeSaleItem(this.sourceId, uuid);
                if (optional.isPresent()) {
                    saleItem = optional.get();
                    BaseEconomy.getEconomyBridge(this.paymentType).take(buyer, this.economyType, saleItem.getPrice());
                    Player seller = Bukkit.getPlayer(UUID.fromString(saleItem.getOwnerUUID()));
                    if (seller != null && seller.isOnline()) {
                        double tax = saleItem.getPrice() * this.getPermsValueForPlayer(this.getTaxSection(), seller, false);
                        double last = saleItem.getPrice() - tax;
                        DecimalFormat df = new DecimalFormat("#0.00");
                        BaseEconomy.getEconomyBridge(this.paymentType).give(seller, this.economyType, last);
                        seller.sendMessage(I18n.getStrAndHeader("sale-sell")
                                .replace("%economy%", this.economyName)
                                .replace("%money%", df.format(saleItem.getPrice()))
                                .replace("%last%", df.format(last)));
                        // send taxes
                        ServerMarketApi.sendTaxes(paymentType, this.economyType, tax);
                    } else {
                        ServerMarketApi.addOfflineTransaction(saleItem.getOwnerUUID(), this.paymentType, this.economyType, saleItem.getPrice(), this.marketKey);
                    }
                    // 给予购买者物品
                    ServerMarket.getStorageHandler().addItemToStore(buyer.getUniqueId(), saleItem, "buy");
                    // call PlayerSaleEvent.Buy
                    PlayerSaleEvent.Buy event = new PlayerSaleEvent.Buy(buyer, this, saleItem);
                    Bukkit.getPluginManager().callEvent(event);
                    // 发送购买消息至购买者
                    buyer.sendMessage(I18n.getStrAndHeader("buy-item"));
                    new MarketGui(this.marketKey, page, filter).openGui(buyer);
                } else {
                    buyer.sendMessage(I18n.getStrAndHeader("error-sale"));
                }
            } else {
                buyer.sendMessage(I18n.getStrAndHeader("shift-buy"));
            }
        } else {
            buyer.sendMessage(I18n.getStrAndHeader("error-sale"));
        }
    }
}

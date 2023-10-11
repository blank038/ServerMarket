package com.blank038.servermarket.data.cache.market;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.api.event.MarketLoadEvent;
import com.blank038.servermarket.economy.BaseEconomy;
import com.blank038.servermarket.data.cache.sale.SaleItem;
import com.blank038.servermarket.enums.MarketStatus;
import com.blank038.servermarket.enums.PayType;
import com.blank038.servermarket.filter.FilterBuilder;
import com.blank038.servermarket.filter.impl.KeyFilterImpl;
import com.blank038.servermarket.gui.impl.MarketGui;
import com.blank038.servermarket.i18n.I18n;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    public static final HashMap<String, MarketData> MARKET_DATA = new HashMap<>();

    private final File sourceFile;
    /**
     * 商品列表
     */
    private final Map<String, String> extraMap = new HashMap<>();
    private final String sourceId, marketKey, permission, shortCommand, ecoType, displayName, economyName;
    private final List<String> loreBlackList, typeBlackList, saleTypes;
    private final int min, max, effectiveTime;
    private final PayType paytype;
    private final ConfigurationSection taxSection, shoutTaxSection;
    private MarketStatus marketStatus;
    private boolean showSaleInfo, saleBroadcast;
    private String dateFormat;

    public MarketData(File file) {
        FileConfiguration options = YamlConfiguration.loadConfiguration(file);
        this.sourceFile = file;
        this.marketKey = file.getName().replace(".yml", "");
        this.sourceId = options.getString("source_id");
        this.permission = options.getString("permission");
        this.shortCommand = options.getString("short-command");
        this.displayName = ChatColor.translateAlternateColorCodes('&', options.getString("display-name"));
        this.economyName = ChatColor.translateAlternateColorCodes('&', options.getString("economy-name"));
        this.min = options.getInt("price.min");
        this.max = options.getInt("price.max");
        if (options.contains("extra-price")) {
            for (String key : options.getConfigurationSection("extra-price").getKeys(false)) {
                this.extraMap.put(key, options.getString("extra-price." + key));
            }
        }
        this.effectiveTime = options.getInt("effective_time");
        this.typeBlackList = options.getStringList("black-list.type");
        this.loreBlackList = options.getStringList("black-list.lore");
        this.saleTypes = options.getStringList("types");
        this.taxSection = options.getConfigurationSection("tax");
        this.shoutTaxSection = options.getConfigurationSection("shout-tax");
        this.showSaleInfo = options.getBoolean("show-sale-info");
        this.saleBroadcast = options.getBoolean("sale-broadcast");
        this.dateFormat = options.getString("simple-date-format");
        switch ((this.ecoType = options.getString("vault-type").toLowerCase())) {
            case "vault":
                this.paytype = PayType.VAULT;
                break;
            case "playerpoints":
                this.paytype = PayType.PLAYER_POINTS;
                break;
            default:
                this.paytype = PayType.NY_ECONOMY;
                break;
        }
        if (!BaseEconomy.PAY_TYPES.containsKey(this.paytype)) {
            this.marketStatus = MarketStatus.ERROR;
            ServerMarket.getInstance().getConsoleLogger().log(false, "&6 * &f读取市场 &e" + this.displayName + " &f异常, 货币不存在");
        } else {
            this.marketStatus = MarketStatus.LOADED;
            try {
                ServerMarket.getStorageHandler().load(this.marketKey);
                ServerMarket.getInstance().getConsoleLogger().log(false, "&6 * &f市场 &e" + this.displayName + " &f加载成功");
            } catch (Exception ignored) {
                this.marketStatus = MarketStatus.ERROR;
                ServerMarket.getInstance().getConsoleLogger().log(false, "&6 * &f读取市场 &e" + this.displayName + " &f物品异常");
            }
        }
        MarketData.MARKET_DATA.put(this.getMarketKey(), this);
        // Call the event MarketLoadEvent
        MarketLoadEvent event = new MarketLoadEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * 获取货币类型
     *
     * @return 货币类型枚举
     */
    public PayType getPayType() {
        return this.paytype;
    }

    /**
     * 获取扣税后的价格
     *
     * @param player 目标玩家
     * @param money  初始金币
     * @return 扣税后金币
     */
    public double getLastMoney(ConfigurationSection section, Player player, double money) {
        String header = section.getString("header");
        double tax = section.getDouble("node.default");
        for (String key : section.getConfigurationSection("node").getKeys(false)) {
            double tempTax = section.getDouble("node." + key);
            if (player.hasPermission(header + "." + key) && tempTax < tax) {
                tax = tempTax;
            }
        }
        return money - money * tax;
    }

    public double getTax(ConfigurationSection section, Player player) {
        String header = section.getString("header");
        double tax = section.getDouble("node.default");
        for (String key : section.getConfigurationSection("node").getKeys(false)) {
            double tempTax = section.getDouble("node." + key);
            if (player.hasPermission(header + "." + key) && tempTax < tax) {
                tax = tempTax;
            }
        }
        return tax;
    }

    public void setShowSaleInfo(boolean show) {
        this.showSaleInfo = show;
    }

    public boolean isSaleBroadcastStatus() {
        return saleBroadcast;
    }

    public void setSaleBroadcastStatus(boolean status) {
        this.saleBroadcast = status;
    }

    public void setDateFormat(String format) {
        this.dateFormat = format;
    }

    public void tryBuySale(Player buyer, String uuid, boolean shift, int page, FilterBuilder filter) {
        // 判断商品是否存在
        if (!ServerMarket.getStorageHandler().hasSale(this.sourceId, uuid)) {
            buyer.sendMessage(I18n.getString("error-sale", true));
            return;
        }
        Optional<SaleItem> optionalSaleItem = ServerMarket.getStorageHandler().getSaleItem(this.sourceId, uuid);
        if (optionalSaleItem.isPresent()) {
            SaleItem saleItem = optionalSaleItem.get();
            if (saleItem.getOwnerUUID().equals(buyer.getUniqueId().toString())) {
                if (shift) {
                    ServerMarket.getStorageHandler().removeSaleItem(this.sourceId, uuid)
                            .ifPresent((sale) -> {
                                buyer.getInventory().addItem(sale.getSaleItem().clone());
                                buyer.sendMessage(I18n.getString("unsale", true));
                                new MarketGui(this.marketKey, page, filter).openGui(buyer);
                            });
                } else {
                    buyer.sendMessage(I18n.getString("shift-unsale", true));
                }
                return;
            }
            if (shift) {
                if (saleItem.getPrice() == 0) {
                    buyer.sendMessage(I18n.getString("error-sale", true));
                    return;
                }
                if (BaseEconomy.getEconomyBridge(this.paytype).balance(buyer, this.ecoType) < saleItem.getPrice()) {
                    buyer.sendMessage(I18n.getString("lack-money", true)
                            .replace("%economy%", this.economyName));
                    return;
                }
                ServerMarket.getStorageHandler().removeSaleItem(this.sourceId, uuid);
                BaseEconomy.getEconomyBridge(this.paytype).take(buyer, this.ecoType, saleItem.getPrice());
                Player seller = Bukkit.getPlayer(UUID.fromString(saleItem.getOwnerUUID()));
                if (seller != null && seller.isOnline()) {
                    double last = this.getLastMoney(this.getTaxSection(), seller, saleItem.getPrice());
                    DecimalFormat df = new DecimalFormat("#0.00");
                    BaseEconomy.getEconomyBridge(this.paytype).give(seller, this.ecoType, last);
                    seller.sendMessage(I18n.getString("sale-sell", true)
                            .replace("%economy%", this.economyName)
                            .replace("%money%", df.format(saleItem.getPrice()))
                            .replace("%last%", df.format(last)));
                } else {
                    ServerMarket.getApi().addOfflineTransaction(saleItem.getOwnerUUID(), this.paytype, this.ecoType, saleItem.getPrice(), this.marketKey);
                }
                // 给予购买者物品
                ServerMarket.getStorageHandler().addItemToStore(buyer.getUniqueId(), saleItem);
                // 发送购买消息至购买者
                buyer.sendMessage(I18n.getString("buy-item", true));
                new MarketGui(this.marketKey, page, filter).openGui(buyer);
            } else {
                buyer.sendMessage(I18n.getString("shift-buy", true));
            }
        } else {
            buyer.sendMessage(I18n.getString("error-sale", true));
        }
    }

    /**
     * 玩家出售物品
     *
     * @param player  命令执行者
     * @param message 命令
     * @return 执行结果, 为 true 时取消事件
     */
    public boolean performSellCommand(Player player, String message) {
        String[] split = message.split(" ");
        String command = split[0].substring(1);
        if (!command.equals(this.shortCommand)) {
            return false;
        }
        if (this.permission != null && !this.permission.isEmpty() && !player.hasPermission(this.permission)) {
            player.sendMessage(I18n.getString("no-permission", true));
            return true;
        }
        if (split.length == 1) {
            new MarketGui(this.marketKey, 1, null).openGui(player);
            return true;
        }
        if (split.length == 2) {
            player.sendMessage(I18n.getString("price-null", true));
            return true;
        }
        ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            player.sendMessage(I18n.getString("hand-air", true));
            return true;
        }
        boolean denied = false;
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
            for (String l : itemStack.getItemMeta().getLore()) {
                if (loreBlackList.contains(l.replace("§", "&"))) {
                    denied = true;
                    break;
                }
            }
        }
        if (typeBlackList.contains(itemStack.getType().name()) || denied) {
            player.sendMessage(I18n.getString("deny-item", true));
            return true;
        }
        int price;
        try {
            price = Integer.parseInt(split[2]);
        } catch (Exception e) {
            player.sendMessage(I18n.getString("wrong-number", true));
            return true;
        }
        if (price < this.min) {
            player.sendMessage(I18n.getString("min-price", true)
                    .replace("%min%", String.valueOf(this.min)));
            return true;
        }
        if (price > this.max) {
            player.sendMessage(I18n.getString("max-price", true)
                    .replace("%max%", String.valueOf(this.max)));
            return true;
        }
        String extraPrice = this.extraMap.entrySet().stream()
                .filter((s) -> new FilterBuilder().addKeyFilter(new KeyFilterImpl(s.getKey())).check(itemStack))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
        if (extraPrice != null && price < Integer.parseInt(extraPrice.split("-")[0])) {
            player.sendMessage(I18n.getString("min-price", true)
                    .replace("%min%", extraPrice.split("-")[0]));
            return true;
        }
        if (extraPrice != null && price > Integer.parseInt(extraPrice.split("-")[1])) {
            player.sendMessage(I18n.getString("max-price", true)
                    .replace("%max%", extraPrice.split("-")[1]));
            return true;
        }
        double tax = this.getTax(this.getShoutTaxSection(), player);
        if (BaseEconomy.getEconomyBridge(this.paytype).balance(player, this.ecoType) < tax) {
            player.sendMessage(I18n.getString("shout-tax", true)
                    .replace("%economy%", this.economyName));
            return true;
        }
        // 扣除费率
        if (tax > 0) {
            BaseEconomy.getEconomyBridge(this.paytype).take(player, this.ecoType, tax);
        }
        // 设置玩家手中物品为空
        player.getInventory().setItemInMainHand(null);
        // 上架物品
        SaleItem saleItem = new SaleItem(UUID.randomUUID().toString(), player.getUniqueId().toString(), player.getName(),
                itemStack, PayType.VAULT, this.ecoType, price, System.currentTimeMillis());
        ServerMarket.getStorageHandler().addSale(this.marketKey, saleItem);
        player.sendMessage(I18n.getString("sell", true));
        // 判断是否公告
        if (this.saleBroadcast) {
            String displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                    itemStack.getItemMeta().getDisplayName() : itemStack.getType().name();
            Bukkit.getServer().broadcastMessage(I18n.getString("broadcast", true)
                    .replace("%item%", displayName)
                    .replace("%market_name%", this.displayName)
                    .replace("%amount%", String.valueOf(itemStack.getAmount()))
                    .replace("%player%", player.getName()));
        }
        return true;
    }
}

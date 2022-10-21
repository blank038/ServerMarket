package com.blank038.servermarket.data.storage;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.api.event.MarketLoadEvent;
import com.blank038.servermarket.bridge.BaseBridge;
import com.blank038.servermarket.filter.FilterBuilder;
import com.blank038.servermarket.gui.MarketGui;
import com.blank038.servermarket.i18n.I18n;
import com.blank038.servermarket.data.sale.SaleItem;
import com.blank038.servermarket.enums.MarketStatus;
import com.blank038.servermarket.enums.PayType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Blank038
 * @date 2021/03/05
 */
@SuppressWarnings(value = {"unused"})
public class MarketData {
    public static final HashMap<String, MarketData> MARKET_DATA = new HashMap<>();

    private final File sourceFile;
    /**
     * 商品列表
     */
    private final HashMap<String, SaleItem> saleMap = new HashMap<>();
    private final String sourceId, marketKey, permission, shortCommand, ecoType, displayName, economyName;
    private final List<String> loreBlackList, typeBlackList, saleTypes;
    private final int min, max, effectiveTime;
    private final PayType paytype;
    private final ConfigurationSection taxSection, shoutTaxSection;
    private MarketStatus marketStatus;
    private boolean showSaleInfo, saleBroadcast;
    private String dateFormat;

    public MarketData(File file) {
        this.sourceFile = file;
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        this.marketKey = file.getName().replace(".yml", "");
        this.sourceId = data.getString("source_id");
        this.permission = data.getString("permission");
        this.shortCommand = data.getString("short-command");
        this.displayName = ChatColor.translateAlternateColorCodes('&', data.getString("display-name"));
        this.economyName = ChatColor.translateAlternateColorCodes('&', data.getString("economy-name"));
        this.min = data.getInt("price.min");
        this.max = data.getInt("price.max");
        this.effectiveTime = data.getInt("effective_time");
        this.typeBlackList = data.getStringList("black-list.type");
        this.loreBlackList = data.getStringList("black-list.lore");
        this.saleTypes = data.getStringList("types");
        this.taxSection = data.getConfigurationSection("tax");
        this.shoutTaxSection = data.getConfigurationSection("shout-tax");
        this.showSaleInfo = data.getBoolean("show-sale-info");
        this.saleBroadcast = data.getBoolean("sale-broadcast");
        this.dateFormat = data.getString("simple-date-format");
        switch ((this.ecoType = data.getString("vault-type").toLowerCase())) {
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
        if (!BaseBridge.PAY_TYPES.containsKey(this.paytype)) {
            this.marketStatus = MarketStatus.ERROR;
            ServerMarket.getInstance().log("&6 * &f读取市场 &e" + this.displayName + " &f异常, 货币不存在");
        } else {
            this.marketStatus = MarketStatus.LOADED;
            try {
                this.loadSaleData();
                ServerMarket.getInstance().log("&6 * &f市场 &e" + this.displayName + " &f加载成功");
            } catch (Exception ignored) {
                this.marketStatus = MarketStatus.ERROR;
                ServerMarket.getInstance().log("&6 * &f读取市场 &e" + this.displayName + " &f物品异常");
            }
        }
        MarketData.MARKET_DATA.put(this.getMarketKey(), this);
        // 唤起加载事件
        MarketLoadEvent event = new MarketLoadEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public File getSourceFile() {
        return this.sourceFile;
    }

    public HashMap<String, SaleItem> getSales() {
        return saleMap;
    }

    public SaleItem getSaleItem(String key) {
        return this.saleMap.getOrDefault(key, null);
    }

    public List<String> getSaleTypes() {
        return this.saleTypes;
    }

    /**
     * 获取市场源id
     *
     * @return 市场源id
     */
    public String getSourceId() {
        return this.sourceId;
    }

    /**
     * 获取市场编号
     *
     * @return 市场编号
     */
    public String getMarketKey() {
        return this.marketKey;
    }

    /**
     * 获取市场权限
     *
     * @return 市场权限
     */
    public String getPermission() {
        return this.permission;
    }

    /**
     * 获取货币名
     *
     * @return 货币名
     */
    public String getEcoType() {
        return this.ecoType;
    }

    /**
     * 获取市场展示名
     *
     * @return 市场展示名
     */
    public String getDisplayName() {
        return this.displayName;
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
     * 获取市场加载状态
     *
     * @return 市场加载状态
     */
    public MarketStatus getMarketStatus() {
        return this.marketStatus;
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

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public int getEffectiveTime() {
        return this.effectiveTime;
    }

    public boolean isShowSaleInfo() {
        return showSaleInfo;
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

    public String getDateFormat() {
        return this.dateFormat;
    }

    public void setDateFormat(String format) {
        this.dateFormat = format;
    }

    public ConfigurationSection getTaxSection() {
        return this.taxSection;
    }

    public ConfigurationSection getShoutTaxSection() {
        return this.shoutTaxSection;
    }

    public void buySaleItem(Player buyer, String uuid, boolean shift, int page, FilterBuilder filter) {
        // 判断商品是否存在
        if (!saleMap.containsKey(uuid)) {
            buyer.sendMessage(I18n.getString("error-sale", true));
            return;
        }
        SaleItem saleItem = saleMap.get(uuid);
        if (saleItem.getOwnerUUID().equals(buyer.getUniqueId().toString())) {
            if (shift) {
                buyer.getInventory().addItem(saleMap.remove(uuid).getSafeItem().clone());
                buyer.sendMessage(I18n.getString("unsale", true));
                new MarketGui(this.marketKey, page, filter).openGui(buyer);
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
            if (ServerMarket.getInstance().getEconomyBridge(this.paytype).balance(buyer, this.ecoType) < saleItem.getPrice()) {
                buyer.sendMessage(I18n.getString("lack-money", true).replace("%economy%", this.economyName));
                return;
            }
            // 先移除, 确保不被重复购买
            saleMap.remove(uuid);
            // 先给玩家钱扣了！
            ServerMarket.getInstance().getEconomyBridge(this.paytype).take(buyer, this.ecoType, saleItem.getPrice());
            // 再把钱给出售者
            Player seller = Bukkit.getPlayer(UUID.fromString(saleItem.getOwnerUUID()));
            if (seller != null && seller.isOnline()) {
                double last = this.getLastMoney(this.getTaxSection(), seller, saleItem.getPrice());
                DecimalFormat df = new DecimalFormat("#0.00");
                ServerMarket.getInstance().getEconomyBridge(this.paytype).give(seller, this.ecoType, last);
                seller.sendMessage(I18n.getString("sale-sell", true).replace("%economy%", this.economyName)
                        .replace("%money%", df.format(saleItem.getPrice())).replace("%last%", df.format(last)));
            } else {
                ServerMarket.getInstance().addMoney(saleItem.getOwnerUUID(), this.paytype, this.ecoType, saleItem.getPrice(), this.marketKey);
            }
            // 再给购买者物品
            ServerMarket.getApi().addItem(buyer.getUniqueId(), saleItem);
            // 给购买者发送消息
            buyer.sendMessage(I18n.getString("buy-item", true));
            new MarketGui(this.marketKey, page, filter).openGui(buyer);
        } else {
            buyer.sendMessage(I18n.getString("shift-buy", true));
        }
    }

    /**
     * 玩家出售物品
     *
     * @param player  命令执行者
     * @param message 命令
     * @return 执行结果
     */
    public boolean performSellCommand(Player player, String message) {
        String[] split = message.split(" ");
        String command = split[0].substring(1);
        if (!command.equals(this.shortCommand)) {
            return false;
        }
        if (this.permission != null && !"".equals(this.permission) && !player.hasPermission(this.permission)) {
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
        boolean has = false;
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
            for (String l : itemStack.getItemMeta().getLore()) {
                if (loreBlackList.contains(l.replace("§", "&"))) {
                    has = true;
                    break;
                }
            }
        }
        if (typeBlackList.contains(itemStack.getType().name()) || has) {
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
            player.sendMessage(I18n.getString("min-price", true).replace("%min%", String.valueOf(this.min)));
            return true;
        }
        if (price > this.max) {
            player.sendMessage(I18n.getString("max-price", true).replace("%max%", String.valueOf(this.max)));
            return true;
        }
        double tax = this.getTax(this.getShoutTaxSection(), player);
        if (ServerMarket.getInstance().getEconomyBridge(this.paytype).balance(player, this.ecoType) < tax) {
            player.sendMessage(I18n.getString("shout-tax", true).replace("%economy%", this.economyName));
            return true;
        }
        // 扣除费率
        if (tax > 0) {
            ServerMarket.getInstance().getEconomyBridge(this.paytype).take(player, this.ecoType, tax);
        }
        // 设置玩家手中物品为空
        player.getInventory().setItemInMainHand(null);
        // 上架物品
        String saleUUID = UUID.randomUUID().toString();
        SaleItem saleItem = new SaleItem(saleUUID, player.getUniqueId().toString(), player.getName(),
                itemStack, PayType.VAULT, this.ecoType, price, System.currentTimeMillis());
        this.saleMap.put(saleUUID, saleItem);
        player.sendMessage(I18n.getString("sell", true));
        // 判断是否公告
        if (this.saleBroadcast) {
            String displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                    itemStack.getItemMeta().getDisplayName() : itemStack.getType().name();
            Bukkit.getServer().broadcastMessage(I18n.getString("broadcast", true).replace("%item%", displayName)
                    .replace("%market_name%", this.displayName).replace("%amount%", String.valueOf(itemStack.getAmount())).replace("%player%", player.getName()));
        }
        return true;
    }

    public void loadSaleData() {
        if (!this.saleMap.isEmpty()) {
            this.saveSaleData();
        }
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/saleData/", sourceId + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            SaleItem saleItem = new SaleItem(data.getConfigurationSection(key));
            this.saleMap.put(saleItem.getSaleUUID(), saleItem);
        }
    }

    public void saveSaleData() {
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/saleData/", sourceId + ".yml");
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, SaleItem> entry : saleMap.entrySet()) {
            data.set(entry.getKey(), entry.getValue().toSection());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeTimeOutItem() {
        MarketData.MARKET_DATA.forEach((key, value) -> {
            // 开始计算
            Iterator<Map.Entry<String, SaleItem>> iterator = value.saleMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SaleItem> entry = iterator.next();
                int second = (int) ((System.currentTimeMillis() - entry.getValue().getPostTime()) / 1000L);
                if (second >= value.getEffectiveTime()) {
                    // 移除
                    iterator.remove();
                    // 返回玩家仓库
                    UUID uuid = UUID.fromString(entry.getValue().getOwnerUUID());
                    ServerMarket.getApi().addItem(uuid, entry.getValue().getSafeItem());
                }
            }
        });
    }
}

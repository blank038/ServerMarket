package com.blank038.servermarket.data.storage;

import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.api.event.MarketLoadEvent;
import com.blank038.servermarket.bridge.BaseBridge;
import com.blank038.servermarket.i18n.I18n;
import com.blank038.servermarket.data.sale.SaleItem;
import com.blank038.servermarket.enums.MarketStatus;
import com.blank038.servermarket.enums.PayType;
import com.blank038.servermarket.util.CommonUtil;
import com.blank038.servermarket.util.ItemUtil;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

    public HashMap<String, SaleItem> getSales() {
        return saleMap;
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

    /**
     * 获得市场展示物品
     *
     * @param saleItem 市场商品信息
     * @return 展示物品
     */
    public ItemStack getShowItem(SaleItem saleItem, FileConfiguration data) {
        ItemStack itemStack = saleItem.getSafeItem().clone();
        if (this.showSaleInfo) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : Lists.newArrayList();
            // 设置物品格式
            Date date = new Date(saleItem.getPostTime());
            SimpleDateFormat sdf = new SimpleDateFormat(this.dateFormat);
            // 设置额外信息
            for (String i : data.getStringList("sale-info")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', i).replace("%seller%", saleItem.getOwnerName())
                        .replace("%price%", String.valueOf(saleItem.getPrice())).replace("%time%", sdf.format(date)));
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
        return ServerMarket.getNMSControl().addNbt(itemStack, "SaleUUID", saleItem.getSaleUUID());
    }

    /**
     * 打开市场面板
     *
     * @param player      目标玩家
     * @param currentPage 页码
     */
    public void openGui(Player player, int currentPage, String filter) {
        if (this.marketStatus == MarketStatus.ERROR) {
            player.sendMessage(I18n.getString("market-error", true));
            return;
        }
        if (this.permission != null && !"".equals(this.permission) && !player.hasPermission(this.permission)) {
            player.sendMessage(I18n.getString("no-permission", true));
            return;
        }
        // 读取配置文件
        FileConfiguration data = YamlConfiguration.loadConfiguration(this.sourceFile);
        GuiModel guiModel = new GuiModel(data.getString("title"), data.getInt("size"));
        guiModel.registerListener(ServerMarket.getInstance());
        guiModel.setCloseRemove(true);
        // 设置界面物品
        HashMap<Integer, ItemStack> items = new HashMap<>();
        if (data.contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection section = data.getConfigurationSection("items." + key);
                ItemStack itemStack = new ItemStack(Material.valueOf(section.getString("type").toUpperCase()),
                        section.getInt("amount"), (short) section.getInt("data"));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));
                // 开始遍历设置Lore
                List<String> list = new ArrayList<>();
                for (String lore : section.getStringList("lore")) {
                    list.add(ChatColor.translateAlternateColorCodes('&', lore));
                }
                itemMeta.setLore(list);
                itemStack.setItemMeta(itemMeta);
                // 开始判断是否有交互操作
                if (section.contains("action")) {
                    itemStack = ServerMarket.getNMSControl().addNbt(itemStack, "MarketAction", section.getString("action"));
                }
                for (int i : CommonUtil.formatSlots(section.getString("slot"))) {
                    items.put(i, itemStack);
                }
            }
        }
        // 开始获取全球市场物品
        Integer[] slots = CommonUtil.formatSlots(data.getString("sale-item-slots"));
        String[] keys = saleMap.keySet().toArray(new String[0]);
        // 计算下标
        int maxPage = saleMap.size() / slots.length;
        maxPage += (saleMap.size() % slots.length) == 0 ? 0 : 1;
        // 判断页面是否超标, 如果是的话就设置为第一页
        if (currentPage > maxPage) {
            currentPage = 1;
        }
        // 获得额外增加的信息
        int start = slots.length * (currentPage - 1), end = slots.length * currentPage;
        for (int i = start, index = 0; i < end; i++, index++) {
            if (index >= slots.length || i >= keys.length) {
                break;
            }
            // 开始设置物品
            SaleItem saleItem = saleMap.getOrDefault(keys[i], null);
            if (saleItem == null || (filter != null && !ItemUtil.isSimilar(saleItem.getSafeItem(), filter))) {
                --index;
                continue;
            }
            items.put(slots[index], this.getShowItem(saleItem, data));
        }
        guiModel.setItem(items);
        final int lastPage = currentPage, finalMaxPage = maxPage;
        guiModel.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                ItemStack itemStack = e.getCurrentItem();
                String key = ServerMarket.getNMSControl().getValue(itemStack, "SaleUUID"),
                        action = ServerMarket.getNMSControl().getValue(itemStack, "MarketAction");
                // 强转玩家
                Player clicker = (Player) e.getWhoClicked();
                if (key != null) {
                    // 购买商品
                    this.buySaleItem(clicker, key, e.isShiftClick(), lastPage, filter);
                } else if (action != null) {
                    // 判断交互方式
                    switch (action) {
                        case "up":
                            if (lastPage == 1) {
                                clicker.sendMessage(I18n.getString("no-previous-page", true));
                            } else {
                                this.openGui(player, lastPage - 1, filter);
                            }
                            break;
                        case "down":
                            if (lastPage >= finalMaxPage) {
                                clicker.sendMessage(I18n.getString("no-next-page", true));
                            } else {
                                this.openGui(player, lastPage + 1, filter);
                            }
                            break;
                        case "store":
                            new StoreContainer(clicker, lastPage, this.marketKey).open(1);
                            break;
                        default:
                            if (action.contains(":")) {
                                String[] split = action.split(":");
                                if (split.length < 2) {
                                    return;
                                }
                                if ("player".equalsIgnoreCase(split[0])) {
                                    Bukkit.getServer().dispatchCommand(player, split[1].replace("%player%", player.getName()));
                                } else if ("console".equalsIgnoreCase(split[0])) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), split[1].replace("%player%", player.getName()));
                                }
                            }
                            break;
                    }
                }
            }
        });
        // 打开界面
        guiModel.openInventory(player);
    }

    public void buySaleItem(Player buyer, String uuid, boolean shift, int page, String filter) {
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
                this.openGui(buyer, 1, filter);
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
            this.openGui(buyer, page, filter);
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
            this.openGui(player, 1, null);
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

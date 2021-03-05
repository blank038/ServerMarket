package com.blank038.servermarket.data;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.config.LangConfiguration;
import com.blank038.servermarket.data.gui.SaleItem;
import com.blank038.servermarket.data.gui.StoreContainer;
import com.blank038.servermarket.enums.PayType;
import com.blank038.servermarket.util.CommonUtil;
import com.google.common.collect.Lists;
import com.mc9y.blank038api.util.inventory.GuiModel;
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

    private final File TARGET_FILE;
    /**
     * 商品列表
     */
    private final HashMap<String, SaleItem> SALE_MAP = new HashMap<>();
    private final String SOURCE_ID, MARKET_KEY, PERMISSION, SHORT_COMMAND, ECO_TYPE;
    private final PayType PAY_TYPE;

    public MarketData(File file) {
        this.TARGET_FILE = file;
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        this.MARKET_KEY = file.getName().replace(".yml", "");
        this.SOURCE_ID = data.getString("source_id");
        this.PERMISSION = data.getString("permission");
        this.SHORT_COMMAND = data.getString("short-command");
        switch ((ECO_TYPE = data.getString("vault-type").toLowerCase())) {
            case "vault":
                this.PAY_TYPE = PayType.VAULT;
                break;
            case "playerpoints":
                this.PAY_TYPE = PayType.PLAYER_POINTS;
                break;
            default:
                this.PAY_TYPE = PayType.NY_ECONOMY;
                break;
        }
    }

    public HashMap<String, SaleItem> getSales() {
        return SALE_MAP;
    }

    /**
     * 获取市场源id
     *
     * @return 市场源id
     */
    public String getSourceId() {
        return this.SOURCE_ID;
    }

    /**
     * 获取市场编号
     *
     * @return 市场编号
     */
    public String getMarketKey() {
        return this.MARKET_KEY;
    }

    /**
     * 获取市场权限
     *
     * @return 市场权限
     */
    public String getPermission() {
        return this.PERMISSION;
    }

    /**
     * 获取货币类型
     *
     * @return 货币类型枚举
     */
    public PayType getPayType() {
        return this.PAY_TYPE;
    }

    /**
     * 获取货币名
     *
     * @return 货币名
     */
    public String getEcoType() {
        return this.ECO_TYPE;
    }

    /**
     * 获得市场展示物品
     *
     * @param saleItem 市场商品信息
     * @param infoLore 额外增加信息
     * @return 展示物品
     */
    public ItemStack getShowItem(SaleItem saleItem, List<String> infoLore) {
        ItemStack itemStack = saleItem.getSafeItem().clone();
        if (ServerMarket.getInstance().getConfig().getBoolean("sale-info")) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : Lists.newArrayList();
            // 设置物品格式
            Date date = new Date(saleItem.getPostTime());
            SimpleDateFormat sdf = new SimpleDateFormat(ServerMarket.getInstance().getConfig().getString("simple-date-format"));
            // 设置额外信息
            for (String i : infoLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', i).replace("%seller%", saleItem.getOwnerName())
                        .replace("%price%", String.valueOf(saleItem.getPrice())).replace("%time%", sdf.format(date)));
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
        return ServerMarket.getInstance().getNBTBase().addTag(itemStack, "SaleUUID", saleItem.getSaleUUID());
    }

    public void openGui(Player player, int currentPage) {
        // 读取配置文件
        FileConfiguration data = YamlConfiguration.loadConfiguration(this.TARGET_FILE);
        GuiModel guiModel = new GuiModel(data.getString("title"), data.getInt("size"));
        guiModel.registerListener(ServerMarket.getInstance());
        guiModel.setCloseRemove(true);
        // 设置界面物品
        HashMap<Integer, ItemStack> items = new HashMap<>();
        if (data.contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection section = data.getConfigurationSection("items." + key);
                ItemStack itemStack = new ItemStack(Material.valueOf(section.getString("type").toUpperCase()), section.getInt("amount"));
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
                    itemStack = ServerMarket.getInstance().getNBTBase().addTag(itemStack, "MarketAction", section.getString("action"));
                }
                for (int i : CommonUtil.formatSlots(section.getString("slot"))) {
                    items.put(i, itemStack);
                }
            }
        }
        // 开始获取全球市场物品
        Integer[] slots = CommonUtil.formatSlots(data.getString("sale-item-slots"));
        String[] keys = SALE_MAP.keySet().toArray(new String[0]);
        // 计算下标
        int maxPage = SALE_MAP.size() / slots.length;
        maxPage += (SALE_MAP.size() % slots.length) == 0 ? 0 : 1;
        // 判断页面是否超标, 如果是的话就设置为第一页
        if (currentPage > maxPage) {
            currentPage = 1;
        }
        // 获得额外增加的信息
        List<String> extrasLore = data.getStringList("sale-info");
        int start = slots.length * (currentPage - 1), end = slots.length * currentPage;
        for (int i = start, index = 0; i < end; i++, index++) {
            if (index >= slots.length || i >= keys.length) {
                break;
            }
            // 开始设置物品
            SaleItem saleItem = SALE_MAP.getOrDefault(keys[i], null);
            if (saleItem == null) {
                i -= 1;
                continue;
            }
            items.put(slots[index], getShowItem(saleItem, extrasLore));
        }
        guiModel.setItem(items);
        final int lastPage = currentPage, finalMaxPage = maxPage;
        guiModel.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                ItemStack itemStack = e.getCurrentItem();
                String key = ServerMarket.getInstance().getNBTBase().get(itemStack, "SaleUUID"),
                        action = ServerMarket.getInstance().getNBTBase().get(itemStack, "MarketAction");
                // 强转玩家
                Player clicker = (Player) e.getWhoClicked();
                if (key != null) {
                    // 购买商品
                    this.buySaleItem(clicker, key, e.isShiftClick());
                } else if (action != null) {
                    // 判断交互方式
                    switch (action) {
                        case "up":
                            if (lastPage == 1) {
                                clicker.sendMessage(LangConfiguration.getString("no-previous-page", true));
                            } else {
                                this.openGui(player, lastPage - 1);
                            }
                            break;
                        case "down":
                            if (lastPage >= finalMaxPage) {
                                clicker.sendMessage(LangConfiguration.getString("no-next-page", true));
                            } else {
                                this.openGui(player, lastPage + 1);
                            }
                            break;
                        case "store":
                            new StoreContainer(clicker, lastPage).open(1);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        // 打开界面
        guiModel.openInventory(player);
    }

    public void buySaleItem(Player buyer, String uuid, boolean shift) {
        // 判断商品是否存在
        if (!SALE_MAP.containsKey(uuid)) {
            buyer.sendMessage(LangConfiguration.getString("error-sale", true));
            return;
        }
        SaleItem saleItem = SALE_MAP.get(uuid);
        if (saleItem.getOwnerName().equals(buyer.getName())) {
            if (shift) {
                buyer.getInventory().addItem(SALE_MAP.remove(uuid).getSafeItem().clone());
                buyer.sendMessage(LangConfiguration.getString("unsale", true));
                this.openGui(buyer, 1);
            } else {
                buyer.sendMessage(LangConfiguration.getString("shift-unsale", true));
            }
            return;
        }
        if (shift) {
            if (ServerMarket.getInstance().getEconomyBridge().balance(buyer) < saleItem.getPrice()) {
                buyer.sendMessage(LangConfiguration.getString("lack-money", true));
                return;
            }
            // 判断货币类型, 不要问我为什么要判断！后续新增货币扩展延伸性好！
            if (saleItem.getPayType() != PayType.VAULT) {
                return;
            }
            // 先移除, 确保不被重复购买
            SALE_MAP.remove(uuid);
            // 先给玩家钱扣了！
            ServerMarket.getInstance().getEconomyBridge().take(buyer, saleItem.getPrice());
            // 再把钱给出售者
            Player seller = Bukkit.getPlayer(UUID.fromString(saleItem.getOwnerUUID()));
            if (seller != null && seller.isOnline()) {
                double last = ServerMarket.getInstance().getApi().getLastMoney(seller, saleItem.getPrice());
                DecimalFormat df = new DecimalFormat("#.00");
                ServerMarket.getInstance().getEconomyBridge().give(seller, last);
                seller.sendMessage(LangConfiguration.getString("sale-sell", true).replace("%money%", df.format(saleItem.getPrice()))
                        .replace("%last%", df.format(last)));
            } else {
                ServerMarket.getInstance().addMoney(saleItem.getOwnerName(), saleItem.getPrice());
            }
            // 再给购买者物品
            ServerMarket.getInstance().getApi().addItem(buyer.getName(), saleItem);
        } else {
            buyer.sendMessage(LangConfiguration.getString("shift-buy", true));
        }
    }

    /**
     * 玩家出售物品
     *
     * @param player  命令执行者
     * @param message 命令
     */
    public boolean performSellCommand(Player player, String message) {
        String[] split = message.split(" ");
        String command = split[0].substring(1);
        if (!command.equals(this.SHORT_COMMAND)) {
            return false;
        }
        if (split.length == 2) {
            player.sendMessage(LangConfiguration.getString("price-null", true));
            return true;
        }
        ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            player.sendMessage(LangConfiguration.getString("hand-air", true));
            return true;
        }
        boolean has = false;
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
            for (String l : itemStack.getItemMeta().getLore()) {
                if (ServerMarket.getInstance().getConfig().getStringList("black-list.lore").contains(l.replace("§", "&"))) {
                    has = true;
                    break;
                }
            }
        }
        if (ServerMarket.getInstance().getConfig().getStringList("black-list.type").contains(itemStack.getType().name()) || has) {
            player.sendMessage(LangConfiguration.getString("deny-item", true));
            return true;
        }
        int price;
        try {
            price = Integer.parseInt(split[2]);
        } catch (Exception e) {
            player.sendMessage(LangConfiguration.getString("wrong-number", true));
            return true;
        }
        int min = ServerMarket.getInstance().getConfig().getInt("price.min"),
                max = ServerMarket.getInstance().getConfig().getInt("price.max");
        if (price < min) {
            player.sendMessage(LangConfiguration.getString("min-price", true).replace("%min%", String.valueOf(min)));
            return true;
        }
        if (price > max) {
            player.sendMessage(LangConfiguration.getString("max-price", true).replace("%max%", String.valueOf(max)));
            return true;
        }
        // 设置玩家手中物品为空
        player.getInventory().setItemInMainHand(null);
        // 上架物品
        String saleUUID = UUID.randomUUID().toString();
        SaleItem saleItem = new SaleItem(saleUUID, player.getUniqueId().toString(), player.getName(),
                itemStack, PayType.VAULT, price, System.currentTimeMillis());
        this.SALE_MAP.put(saleUUID, saleItem);
        player.sendMessage(LangConfiguration.getString("sell", true));
        // 判断是否公告
        if (ServerMarket.getInstance().getConfig().getBoolean("sale-broadcast")) {
            String displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                    itemStack.getItemMeta().getDisplayName() : itemStack.getType().name();
            player.sendMessage(LangConfiguration.getString("broadcast", true).replace("%item%", displayName)
                    .replace("%amount%", String.valueOf(itemStack.getAmount())).replace("%player%", player.getName()));
        }
        return true;
    }

    public void saveSaleData() {
        File file = new File(ServerMarket.getInstance().getDataFolder() + "/saleData/", SOURCE_ID + ".yml");
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, SaleItem> entry : SALE_MAP.entrySet()) {
            data.set(entry.getKey(), entry.getValue().toSection());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

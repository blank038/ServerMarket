package com.blank038.servermarket.gui;

import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.DataContainer;
import com.blank038.servermarket.data.sale.SaleItem;
import com.blank038.servermarket.data.storage.MarketData;
import com.blank038.servermarket.data.storage.StoreContainer;
import com.blank038.servermarket.enums.MarketStatus;
import com.blank038.servermarket.filter.FilterBuilder;
import com.blank038.servermarket.filter.impl.TypeFilterImpl;
import com.blank038.servermarket.i18n.I18n;
import com.blank038.servermarket.util.CommonUtil;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Blank038
 */
public class MarketGui {
    private final String sourceMarketKey;
    private FilterBuilder filter;
    private int currentPage;
    private String currentType = "all";

    public MarketGui(String sourceMarketKey, int currentPage, FilterBuilder filter) {
        this.sourceMarketKey = sourceMarketKey;
        this.currentPage = currentPage;
        this.filter = filter;
        if (this.filter == null) {
            this.filter = new FilterBuilder();
        }
        if (this.filter.getTypeFilter() == null) {
            this.filter.setTypeFilter(new TypeFilterImpl(Lists.newArrayList(this.currentType)));
        } else {
            this.currentType = this.filter.getTypeFilter().getTypes().get(0);
        }
    }

    /**
     * 打开市场面板
     *
     * @param player 目标玩家
     */
    public void openGui(Player player) {
        MarketData marketData = MarketData.MARKET_DATA.get(this.sourceMarketKey);
        if (marketData == null || marketData.getMarketStatus() == MarketStatus.ERROR) {
            player.sendMessage(I18n.getString("market-error", true));
            return;
        }
        if (marketData.getPermission() != null && !"".equals(marketData.getPermission()) && !player.hasPermission(marketData.getPermission())) {
            player.sendMessage(I18n.getString("no-permission", true));
            return;
        }
        // 读取配置文件
        FileConfiguration data = YamlConfiguration.loadConfiguration(marketData.getSourceFile());
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
                    list.add(ChatColor.translateAlternateColorCodes('&', lore).replace("%saleType%", this.getCurrentTypeDisplayName()));
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
        String[] keys = marketData.getSales().keySet().toArray(new String[0]);
        // 计算下标
        int size = marketData.getSales().size();
        int maxPage = size / slots.length;
        maxPage += (size % slots.length) == 0 ? 0 : 1;
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
            SaleItem saleItem = marketData.getSaleItem(keys[i]);
            if (saleItem == null || (filter != null && !filter.check(saleItem))) {
                --index;
                continue;
            }
            items.put(slots[index], this.getShowItem(marketData, saleItem, data));
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
                if (key != null && !key.isEmpty()) {
                    // 购买商品
                    MarketData.MARKET_DATA.get(this.sourceMarketKey).buySaleItem(clicker, key, e.isShiftClick(), lastPage, filter);
                } else if (action != null && !action.isEmpty()) {
                    // 判断交互方式
                    switch (action) {
                        case "up":
                            if (lastPage == 1) {
                                clicker.sendMessage(I18n.getString("no-previous-page", true));
                            } else {
                                this.currentPage -= 1;
                                this.openGui(player);
                            }
                            break;
                        case "down":
                            if (lastPage >= finalMaxPage) {
                                clicker.sendMessage(I18n.getString("no-next-page", true));
                            } else {
                                this.currentPage += 1;
                                this.openGui(player);
                            }
                            break;
                        case "type":
                            List<String> types = marketData.getSaleTypes();
                            if (types.size() <= 1) {
                                return;
                            }
                            int index = types.indexOf(currentType);
                            if (index == -1 || index == types.size() - 1) {
                                this.currentType = types.get(0);
                            } else {
                                this.currentType = types.get(index + 1);
                            }
                            this.filter.setTypeFilter(new TypeFilterImpl(Lists.newArrayList(this.currentType)));
                            this.openGui(clicker);
                            clicker.sendMessage(I18n.getString("changeSaleType", true).replace("%type%", this.getCurrentTypeDisplayName()));
                            break;
                        case "store":
                            new StoreContainer(clicker, lastPage, this.sourceMarketKey, this.filter).open(1);
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

    private String getCurrentTypeDisplayName() {
        return DataContainer.SALE_TYPE_DISPLAY_NAME.getOrDefault(this.currentType, this.currentType);
    }

    /**
     * 获得市场展示物品
     *
     * @param saleItem 市场商品信息
     * @return 展示物品
     */
    private ItemStack getShowItem(MarketData marketData, SaleItem saleItem, FileConfiguration data) {
        ItemStack itemStack = saleItem.getSafeItem().clone();
        if (marketData.isShowSaleInfo()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : Lists.newArrayList();
            // 设置物品格式
            Date date = new Date(saleItem.getPostTime());
            SimpleDateFormat sdf = new SimpleDateFormat(marketData.getDateFormat());
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
}

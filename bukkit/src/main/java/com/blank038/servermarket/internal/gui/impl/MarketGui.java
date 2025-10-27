package com.blank038.servermarket.internal.gui.impl;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.blank038.servermarket.internal.gui.context.GuiContext;
import com.blank038.servermarket.internal.handler.CacheHandler;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.internal.enums.MarketStatus;
import com.blank038.servermarket.api.handler.filter.FilterHandler;
import com.blank038.servermarket.api.handler.filter.impl.TypeFilterImpl;
import com.blank038.servermarket.internal.gui.AbstractGui;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.internal.provider.ActionProvider;
import com.blank038.servermarket.internal.util.ItemUtil;
import com.blank038.servermarket.internal.util.TextUtil;
import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Blank038
 */
public class MarketGui extends AbstractGui {

    public MarketGui(GuiContext context) {
        super(context);
        this.initFilter();
    }

    public MarketGui(String sourceMarketKey, int currentPage, FilterHandler filter) {
        super(null);
    }

    private void initFilter() {
        if (this.context.getFilter() == null) {
            this.context.setFilter(new FilterHandler());
        }
        if (this.context.getFilter().getTypeFilter() == null) {
            this.context.getFilter().setTypeFilter(new TypeFilterImpl(Lists.newArrayList(this.context.getType())));
        } else {
            this.context.setType(((TypeFilterImpl) this.context.getFilter().getTypeFilter()).getTypes().get(0));
        }
    }

    /**
     * 打开市场面板
     *
     * @param player 目标玩家
     */
    public void openGui(Player player) {
        MarketData marketData = DataContainer.MARKET_DATA.get(this.context.getMarketId());
        if (marketData == null || marketData.getMarketStatus() == MarketStatus.ERROR) {
            player.sendMessage(I18n.getStrAndHeader("market-error"));
            return;
        }
        if (marketData.getPermission() != null && !marketData.getPermission().isEmpty() && !player.hasPermission(marketData.getPermission())) {
            player.sendMessage(I18n.getStrAndHeader("no-permission"));
            return;
        }
        if (!ServerMarket.getStorageHandler().getPlayerDataByCache(player.getUniqueId()).isPresent()) {
            return;
        }
        // 读取配置文件
        FileConfiguration data = YamlConfiguration.loadConfiguration(marketData.getSourceFile());
        GuiModel model = new GuiModel(data.getString("title"), data.getInt("size"));
        model.registerListener(ServerMarket.getInstance());
        model.setCloseRemove(true);
        // 设置界面物品
        this.initializeDisplayItem(model, data);
        // Get sale list
        Integer[] slots = CommonUtil.formatSlots(data.getString("sale-item-slots"));
        List<SaleCache> saleList = CacheHandler.querySales(this.context.getMarketId())
                .values().stream()
                .filter((entry) -> (this.context.getFilter() == null || this.context.getFilter().check(entry)))
                .sorted(DataContainer.SORT_HANDLER_MAP.get(this.context.getSort()))
                .collect(Collectors.toList());
        // 计算下标
        int maxPage = saleList.size() / slots.length;
        maxPage += (saleList.size() % slots.length) == 0 ? 0 : 1;
        // 判断页面是否超标, 如果是的话就设置为第一页
        if (this.context.getPage() > maxPage) {
            this.context.setPage(1);
        }
        int page = this.context.getPage();
        // 获得额外增加的信息
        int start = slots.length * (page - 1), end = slots.length * page;
        for (int i = start, index = 0; i < end; i++, index++) {
            if (index >= slots.length || i >= saleList.size()) {
                break;
            }
            SaleCache saleItem = saleList.get(i);
            if (saleItem == null || (this.context.getFilter() != null && !this.context.getFilter().check(saleItem))) {
                --index;
                continue;
            }
            model.setItem(slots[index], this.getShowItem(marketData, saleItem, data));
        }
        final int finalMaxPage = maxPage;
        model.onClick((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                ItemStack itemStack = e.getCurrentItem();
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    return;
                }
                Player clicker = (Player) e.getWhoClicked();
                if (this.isCooldown(clicker.getUniqueId())) {
                    clicker.sendMessage(I18n.getStrAndHeader("cooldown"));
                    return;
                }
                NBTItem nbtItem = new NBTItem(itemStack);
                String key = nbtItem.getString("SaleUUID"), action = nbtItem.getString("MarketAction");
                if (key != null && !key.isEmpty()) {
                    this.onAction(clicker, key, e.getClick());
                } else if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case "up":
                            if (page == 1) {
                                clicker.sendMessage(I18n.getStrAndHeader("no-previous-page"));
                            } else {
                                this.context.setPage(this.context.getPage() - 1);
                                this.openGui(clicker);
                            }
                            break;
                        case "down":
                            if (page >= finalMaxPage) {
                                clicker.sendMessage(I18n.getStrAndHeader("no-next-page"));
                            } else {
                                this.context.setPage(this.context.getPage() + 1);
                                this.openGui(clicker);
                            }
                            break;
                        case "type":
                            this.nextType(marketData, clicker);
                            break;
                        case "sort":
                            this.nextSort(clicker);
                            break;
                        case "store":
                            new StoreContainerGui(clicker, this.context).open(1);
                            break;
                        case "refresh":
                            new MarketGui(this.context).openGui(clicker);
                            break;
                        default:
                            if (action.contains(":")) {
                                String[] commands = action.split("\n");
                                for (String command : commands) {
                                    String[] split = command.split(":");
                                    if (split.length < 2) {
                                        continue;
                                    }
                                    if ("player".equalsIgnoreCase(split[0])) {
                                        Bukkit.getServer().dispatchCommand(clicker, split[1].replace("%player%", clicker.getName()));
                                    } else if ("console".equalsIgnoreCase(split[0])) {
                                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), split[1].replace("%player%", clicker.getName()));
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        });
        // 打开界面
        model.openInventory(player);
    }

    private void initializeDisplayItem(GuiModel model, FileConfiguration data) {
        if (data.contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection section = data.getConfigurationSection("items." + key);
                ItemStack itemStack = ItemUtil.generateItem(section.getString("type"),
                        section.getInt("amount"),
                        (short) section.getInt("data"),
                        section.getInt("customModel", -1));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(TextUtil.formatHexColor(section.getString("name")));
                // 开始遍历设置Lore
                List<String> list = new ArrayList<>(section.getStringList("lore"));
                list.replaceAll((s) -> TextUtil.formatHexColor(s)
                        .replace("%saleType%", this.getCurrentTypeDisplayName())
                        .replace("%sortType%", this.getCurrentSortDisplayName()));
                itemMeta.setLore(list);
                itemStack.setItemMeta(itemMeta);
                // 开始判断是否有交互操作
                if (section.contains("action")) {
                    NBTItem nbtItem = new NBTItem(itemStack);
                    nbtItem.setString("MarketAction", section.getString("action"));
                    itemStack = nbtItem.getItem();
                }
                for (int i : CommonUtil.formatSlots(section.getString("slot"))) {
                    model.setItem(i, itemStack);
                }
            }
        }
    }

    private String getCurrentTypeDisplayName() {
        return DataContainer.SALE_TYPE_DISPLAY_NAME.getOrDefault(this.context.getType(), this.context.getType());
    }

    private String getCurrentSortDisplayName() {
        return DataContainer.SORT_TYPE_DISPLAY_NAME.getOrDefault(this.context.getSort(), this.context.getSort());
    }

    private void nextType(MarketData marketData, Player clicker) {
        List<String> types = marketData.getSaleTypes();
        if (types.size() <= 1) {
            return;
        }
        int index = types.indexOf(this.context.getType());
        this.context.setType((index == -1 || index == types.size() - 1) ? types.get(0) : types.get(index + 1));
        this.context.getFilter().setTypeFilter(new TypeFilterImpl(Lists.newArrayList(this.context.getType())));
        this.openGui(clicker);
        clicker.sendMessage(I18n.getStrAndHeader("changeSaleType").replace("%type%", this.getCurrentTypeDisplayName()));
    }

    private void nextSort(Player clicker) {
        List<String> sorts = new ArrayList<>(DataContainer.SORT_HANDLER_MAP.keySet());
        if (sorts.size() <= 1) {
            return;
        }
        int index = sorts.indexOf(this.context.getSort());
        this.context.setSort((index == -1 || index == sorts.size() - 1) ? sorts.get(0) : sorts.get(index + 1));
        this.openGui(clicker);
        clicker.sendMessage(I18n.getStrAndHeader("changeSortType").replace("%type%", this.getCurrentSortDisplayName()));
    }

    /**
     * 获得市场展示物品
     *
     * @param saleItem 市场商品信息
     * @return 展示物品
     */
    private ItemStack getShowItem(MarketData marketData, SaleCache saleItem, FileConfiguration data) {
        ItemStack itemStack = saleItem.getSaleItem();
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : itemStack.getType().name();
        if (data.contains("sale-name")) {
            String finalName = data.getString("sale-name").replace("%name%", displayName);
            itemMeta.setDisplayName(TextUtil.formatHexColor(finalName));
        }
        if (marketData.isShowSaleInfo()) {
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : Lists.newArrayList();
            // 设置物品格式
            Date date = new Date(saleItem.getPostTime());
            SimpleDateFormat sdf = new SimpleDateFormat(marketData.getDateFormat());
            // 设置额外信息
            for (String i : data.getStringList("sale-info")) {
                lore.add(TextUtil.formatHexColor(i)
                        .replace("%seller%", saleItem.getOwnerName())
                        .replace("%price%", String.format(marketData.getPriceFormat(), saleItem.getPrice()))
                        .replace("%time%", sdf.format(date))
                        .replace("%name%", displayName));
            }
            itemMeta.setLore(lore);
        }
        itemStack.setItemMeta(itemMeta);
        // Add uuid to the sale nbt.
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("SaleUUID", saleItem.getSaleUUID());
        return nbtItem.getItem();
    }

    /**
     *
     * Execute when a player attempts to interact with an item.
     *
     * @param player    action player
     * @param uuid      sale unique id
     * @param clickType click type
     */
    private void onAction(Player player, String uuid, ClickType clickType) {
        MarketData marketData = DataContainer.MARKET_DATA.get(this.context.getMarketId());
        if (marketData == null) {
            player.sendMessage(I18n.getStrAndHeader("error-sale"));
            return;
        }
        if (!ServerMarket.getStorageHandler().hasSale(this.context.getMarketId(), uuid)) {
            player.sendMessage(I18n.getStrAndHeader("error-sale"));
            return;
        }
        Optional<SaleCache> optionalSaleItem = ServerMarket.getStorageHandler().getSaleItem(this.context.getMarketId(), uuid);
        if (optionalSaleItem.isPresent()) {
            ActionProvider.runAction(marketData, player, uuid, optionalSaleItem.get(), clickType, this.context);
        } else {
            player.sendMessage(I18n.getStrAndHeader("error-sale"));
        }
    }
}

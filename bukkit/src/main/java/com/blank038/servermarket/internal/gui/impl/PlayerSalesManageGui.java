package com.blank038.servermarket.internal.gui.impl;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.enums.MarketStatus;
import com.blank038.servermarket.internal.gui.AbstractGui;
import com.blank038.servermarket.internal.gui.context.GuiContext;
import com.blank038.servermarket.internal.handler.CacheHandler;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.util.ItemUtil;
import com.blank038.servermarket.internal.util.TextUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Blank038
 */
public class PlayerSalesManageGui extends AbstractGui {

    public PlayerSalesManageGui(GuiContext context) {
        super(context);
    }

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
        File file = new File(ServerMarket.getInstance().getDataFolder(), "gui/player_sales.yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        GuiModel model = new GuiModel(data.getString("title"), data.getInt("size"));
        model.registerListener(ServerMarket.getInstance());
        this.preventInventoryDrag(model);
        model.setCloseRemove(true);

        this.initializeDisplayItem(model, data);

        Integer[] slots = CommonUtil.formatSlots(data.getString("sale-item-slots"));
        String playerUUID = player.getUniqueId().toString();
        List<SaleCache> saleList = CacheHandler.querySales(this.context.getMarketId())
                .values().stream()
                .filter((saleCache) -> playerUUID.equals(saleCache.getOwnerUUID()))
                .sorted(DataContainer.SORT_HANDLER_MAP.get(this.context.getSort()))
                .collect(Collectors.toList());

        int maxPage = saleList.size() / slots.length;
        maxPage += (saleList.size() % slots.length) == 0 ? 0 : 1;
        if (this.context.getPage() > maxPage) {
            this.context.setPage(1);
        }
        int page = this.context.getPage();
        int start = slots.length * (page - 1), end = slots.length * page;
        for (int i = start, index = 0; i < end; i++, index++) {
            if (index >= slots.length || i >= saleList.size()) {
                break;
            }
            model.setItem(slots[index], this.getShowItem(marketData, saleList.get(i), data));
        }

        final int finalMaxPage = maxPage;
        model.onClick((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getInventory()) {
                return;
            }
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
            String saleId = nbtItem.getString("SaleUUID"), action = nbtItem.getString("ManageAction");
            if (saleId != null && !saleId.isEmpty()) {
                this.unsale(clicker, marketData, saleId);
                return;
            }
            if (action == null || action.isEmpty()) {
                return;
            }
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
                case "market":
                    ServerMarketApi.openMarket(clicker, GuiContext.normal(this.context.getMarketId()));
                    break;
                case "refresh":
                    this.openGui(clicker);
                    break;
                default:
                    break;
            }
        });
        model.openInventory(player);
    }

    private void initializeDisplayItem(GuiModel model, FileConfiguration data) {
        if (!data.contains("items")) {
            return;
        }
        for (String key : data.getConfigurationSection("items").getKeys(false)) {
            ConfigurationSection section = data.getConfigurationSection("items." + key);
            ItemStack itemStack = ItemUtil.generateItem(section.getString("type"),
                    section.getInt("amount"),
                    (short) section.getInt("data"),
                    section.getInt("customModel", -1));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(TextUtil.formatHexColor(section.getString("name")));
            List<String> list = new ArrayList<>(section.getStringList("lore"));
            list.replaceAll(TextUtil::formatHexColor);
            itemMeta.setLore(list);
            itemStack.setItemMeta(itemMeta);
            if (section.contains("action")) {
                NBTItem nbtItem = new NBTItem(itemStack);
                nbtItem.setString("ManageAction", section.getString("action"));
                itemStack = nbtItem.getItem();
            }
            for (int i : CommonUtil.formatSlots(section.getString("slot"))) {
                model.setItem(i, itemStack);
            }
        }
    }

    private ItemStack getShowItem(MarketData marketData, SaleCache saleItem, FileConfiguration data) {
        ItemStack itemStack = saleItem.getSaleItem();
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : itemStack.getType().name();
        if (data.contains("sale-name")) {
            itemMeta.setDisplayName(TextUtil.formatHexColor(data.getString("sale-name").replace("%name%", displayName)));
        }
        if (data.contains("sale-info")) {
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat(marketData.getDateFormat());
            for (String line : data.getStringList("sale-info")) {
                lore.add(TextUtil.formatHexColor(line)
                        .replace("%seller%", saleItem.getOwnerName())
                        .replace("%price%", String.format(marketData.getPriceFormat(), saleItem.getPrice()))
                        .replace("%time%", sdf.format(new Date(saleItem.getPostTime())))
                        .replace("%name%", displayName));
            }
            itemMeta.setLore(lore);
        }
        itemStack.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("SaleUUID", saleItem.getSaleUUID());
        return nbtItem.getItem();
    }

    private void unsale(Player player, MarketData marketData, String saleId) {
        Optional<SaleCache> optionalSale = ServerMarket.getStorageHandler().getSaleItem(marketData.getMarketKey(), saleId);
        if (!optionalSale.isPresent()) {
            player.sendMessage(I18n.getStrAndHeader("error-sale"));
            return;
        }
        SaleCache saleCache = optionalSale.get();
        if (!saleCache.getOwnerUUID().equals(player.getUniqueId().toString())) {
            player.sendMessage(I18n.getStrAndHeader("not-owner"));
            return;
        }
        Optional<SaleCache> removedSale = ServerMarket.getStorageHandler().removeSaleItem(marketData.getMarketKey(), saleId);
        if (!removedSale.isPresent()) {
            player.sendMessage(I18n.getStrAndHeader("error-sale"));
            return;
        }
        CacheHandler.removeSaleCache(marketData.getMarketKey(), saleId);
        ServerMarket.getStorageHandler().addItemToStore(player.getUniqueId(), removedSale.get().getSaleItem(), "unsale");
        player.sendMessage(I18n.getStrAndHeader("unsale"));
        this.openGui(player);
    }
}

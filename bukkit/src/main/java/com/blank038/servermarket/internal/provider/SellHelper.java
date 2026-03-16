package com.blank038.servermarket.internal.provider;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.api.event.PlayerSaleEvent;
import com.blank038.servermarket.api.handler.filter.FilterHandler;
import com.blank038.servermarket.api.handler.filter.impl.KeyFilterImpl;
import com.blank038.servermarket.internal.cache.other.NotifyCache;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.internal.economy.BaseEconomy;
import com.blank038.servermarket.internal.enums.PayType;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.service.notify.NotifyCenter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SellHelper {

    public static void performSell(Player player, MarketData marketData, String priceInput) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            player.sendMessage(I18n.getStrAndHeader("hand-air"));
            return;
        }
        ItemStack cloneItem = itemStack.clone();
        if (marketData.getDeniedFilter().check(cloneItem)) {
            player.sendMessage(I18n.getStrAndHeader("deny-item"));
            return;
        }
        int price;
        try {
            price = Integer.parseInt(priceInput);
        } catch (Exception e) {
            player.sendMessage(I18n.getStrAndHeader("wrong-number"));
            return;
        }
        if (price < marketData.getMin()) {
            player.sendMessage(I18n.getStrAndHeader("min-price")
                    .replace("%min%", String.valueOf(marketData.getMin())));
            return;
        }
        if (price > marketData.getMax()) {
            player.sendMessage(I18n.getStrAndHeader("max-price")
                    .replace("%max%", String.valueOf(marketData.getMax())));
            return;
        }
        String extraPrice = marketData.getExtraMap().entrySet().stream()
                .filter((s) -> new FilterHandler().addKeyFilter(new KeyFilterImpl(s.getKey())).check(cloneItem))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
        if (extraPrice != null && price < Integer.parseInt(extraPrice.split("-")[0])) {
            player.sendMessage(I18n.getStrAndHeader("min-price")
                    .replace("%min%", extraPrice.split("-")[0]));
            return;
        }
        if (extraPrice != null && price > Integer.parseInt(extraPrice.split("-")[1])) {
            player.sendMessage(I18n.getStrAndHeader("max-price")
                    .replace("%max%", extraPrice.split("-")[1]));
            return;
        }
        int currentCount = ServerMarket.getStorageHandler().getSaleCountByPlayer(player.getUniqueId(), marketData.getMarketKey());
        if (currentCount >= marketData.getPermsValueForPlayer(marketData.getLimitCountSection(), player, true)) {
            player.sendMessage(I18n.getStrAndHeader("maximum-sale"));
            return;
        }
        double tax = price * marketData.getPermsValueForPlayer(marketData.getShoutTaxSection(), player, false);
        if (BaseEconomy.getEconomyBridge(marketData.getPaymentType()).balance(player, marketData.getEconomyType()) < tax) {
            player.sendMessage(I18n.getStrAndHeader("shout-tax")
                    .replace("%economy%", marketData.getEconomyName()));
            return;
        }
        if (tax > 0 && !BaseEconomy.getEconomyBridge(marketData.getPaymentType()).take(player, marketData.getEconomyType(), tax)) {
            player.sendMessage(I18n.getStrAndHeader("shout-tax")
                    .replace("%economy%", marketData.getEconomyName()));
            return;
        }
        SaleCache saleItem = new SaleCache(UUID.randomUUID().toString(), marketData.getMarketKey(), player.getUniqueId().toString(),
                player.getName(), cloneItem, PayType.VAULT, marketData.getEconomyType(), price, System.currentTimeMillis());
        PlayerSaleEvent.Sell.Pre sellPreEvent = new PlayerSaleEvent.Sell.Pre(player, marketData, saleItem);
        Bukkit.getPluginManager().callEvent(sellPreEvent);
        if (sellPreEvent.isCancelled()) {
            return;
        }
        ServerMarketApi.sendTaxes(marketData.getPaymentType(), marketData.getEconomyType(), tax);
        player.getInventory().setItemInMainHand(null);
        CompletableFuture.supplyAsync(() -> {
            return ServerMarket.getStorageHandler().addSale(marketData.getMarketKey(), saleItem);
        }).exceptionally((e) -> {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Please contact the author at https://github.com/blank038/ServerMarket/issues");
            player.getInventory().addItem(cloneItem);
            player.sendMessage(I18n.getStrAndHeader("sale-failed"));
            return false;
        }).thenAccept((result) -> {
            Bukkit.getScheduler().runTask(ServerMarket.getInstance(), () -> {
                if (result) {
                    PlayerSaleEvent.Sell.Post sellPostEvent = new PlayerSaleEvent.Sell.Post(player, marketData, saleItem);
                    Bukkit.getPluginManager().callEvent(sellPostEvent);
                    player.sendMessage(I18n.getStrAndHeader("sell"));
                    if (marketData.isSaleBroadcast()) {
                        String displayName = CustomNameProvider.getCustomName(cloneItem);
                        NotifyCache notify = new NotifyCache();
                        notify.message = I18n.getStrAndHeader("broadcast")
                                .replace("%item%", displayName)
                                .replace("%market_name%", marketData.getDisplayName())
                                .replace("%amount%", String.valueOf(cloneItem.getAmount()))
                                .replace("%player%", player.getName())
                                .replace("%price%", String.valueOf(price))
                                .replace("%economy%", marketData.getEconomyName());
                        notify.time = System.currentTimeMillis();
                        NotifyCenter.pushNotify(notify);
                    }
                } else {
                    player.getInventory().addItem(cloneItem);
                    player.sendMessage(I18n.getStrAndHeader("sale-denied"));
                }
            });
        });
    }
}

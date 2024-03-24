package com.blank038.servermarket.api.handler.filter.impl;

import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.api.handler.filter.interfaces.IFilter;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Blank038
 */
public class KeyFilterImpl implements IFilter {
    private final List<String> keys = new ArrayList<>();

    public KeyFilterImpl(String... keys) {
        this.keys.addAll(Arrays.asList(keys));
    }

    public KeyFilterImpl(List<String> list) {
        this.keys.addAll(list);
    }

    @Override
    public boolean check(SaleCache saleItem) {
        return this.check(saleItem.getSaleItem());
    }

    public KeyFilterImpl addKeys(String... keys) {
        this.keys.addAll(Arrays.asList(keys));
        this.keys.replaceAll((s) -> ChatColor.translateAlternateColorCodes('&', s));
        return this;
    }

    public KeyFilterImpl addKeys(List<String> list) {
        this.keys.addAll(list);
        this.keys.replaceAll((s) -> ChatColor.translateAlternateColorCodes('&', s));
        return this;
    }

    @Override
    public boolean check(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        BiFunction<String, String, Boolean> func = (text, require) -> {
            if (require.startsWith("regex:")) {
                return text.matches(require.substring(6));
            }
            return text.toLowerCase().contains(require.toLowerCase());
        };
        return this.keys.stream().anyMatch((s) -> {
            if (itemStack.getType().name().toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
            if (!itemStack.hasItemMeta()) {
                return false;
            }
            if (itemStack.getItemMeta().hasDisplayName() && func.apply(itemStack.getItemMeta().getDisplayName(), s)) {
                return true;
            }
            return itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore().stream().anyMatch((i) -> func.apply(i, s));
        });
    }
}

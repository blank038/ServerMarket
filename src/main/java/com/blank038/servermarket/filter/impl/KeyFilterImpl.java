package com.blank038.servermarket.filter.impl;

import com.blank038.servermarket.data.sale.SaleItem;
import com.blank038.servermarket.filter.interfaces.ISaleFilter;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * @author Blank038
 */
public class KeyFilterImpl implements ISaleFilter {
    private final String[] keys;

    public KeyFilterImpl(String... keys) {
        this.keys = keys;
    }

    public KeyFilterImpl(List<String> keys) {
        this.keys = keys.toArray(new String[0]);
    }

    @Override
    public boolean check(SaleItem saleItem) {
        return this.check(saleItem.getSafeItem());
    }

    @Override
    public boolean check(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        return Arrays.stream(keys).anyMatch((s) -> {
            if (itemStack.getType().name().toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
            if (!itemStack.hasItemMeta()) {
                return false;
            }
            if (itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
            return itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore().stream().anyMatch((i) -> i.toLowerCase().contains(s.toLowerCase()));
        });
    }
}

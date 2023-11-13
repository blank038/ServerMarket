package com.blank038.servermarket.filter.impl;

import com.blank038.servermarket.data.cache.sale.SaleCache;
import com.blank038.servermarket.filter.interfaces.IFilter;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
@Getter
public class TypeFilterImpl implements IFilter {
    private final List<String> types = new ArrayList<>();

    public TypeFilterImpl(List<String> keys) {
        this.types.addAll(keys);
    }

    @Override
    public boolean check(SaleCache saleItem) {
        if (this.types.contains("none")) {
            return false;
        }
        if (this.types.contains("all")) {
            return true;
        }
        return this.types.stream().anyMatch((s) -> saleItem.getSaleTypes().contains(s));
    }

    @Override
    public boolean check(ItemStack itemStack) {
        return false;
    }
}
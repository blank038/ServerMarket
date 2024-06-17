package com.blank038.servermarket.api.handler.filter.impl;

import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.api.handler.filter.interfaces.IFilter;
import lombok.Getter;
import org.bukkit.Material;
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
        return this.types.stream().anyMatch((s) -> {
            if (s.startsWith("is:") && this.isType(saleItem, s.substring(3))) {
                return true;
            }
            return saleItem.getSaleTypes().contains(s);
        });
    }

    private boolean isType(SaleCache saleCache, String type) {
        Material material = saleCache.getSaleItem().getType();
        switch (type.toLowerCase()) {
            case "block":
                return material.isBlock();
            case "edible":
                return material.isEdible();
            case "item":
                return material.isItem();
            case "burnable":
                return material.isBurnable();
            default:
                return false;
        }
    }

    @Override
    public boolean check(ItemStack itemStack) {
        return false;
    }
}
package com.blank038.servermarket.filter.impl;

import com.blank038.servermarket.data.sale.SaleItem;
import com.blank038.servermarket.filter.interfaces.ISaleFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
public class TypeFilterImpl implements ISaleFilter {
    private final List<String> types = new ArrayList<>();

    public TypeFilterImpl(List<String> keys) {
        this.types.addAll(keys);
    }

    @Override
    public boolean check(SaleItem saleItem) {
        return this.types.stream().anyMatch((s) -> saleItem.getSaleTypes().contains(s));
    }
}
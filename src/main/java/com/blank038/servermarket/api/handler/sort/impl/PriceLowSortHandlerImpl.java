package com.blank038.servermarket.api.handler.sort.impl;

import com.blank038.servermarket.api.handler.sort.AbstractSortHandler;
import com.blank038.servermarket.internal.cache.sale.SaleCache;

/**
 * @author Blank038
 */
public class PriceLowSortHandlerImpl extends AbstractSortHandler {

    public PriceLowSortHandlerImpl() {
        super("price_low");
    }

    @Override
    public int compare(SaleCache o1, SaleCache o2) {
        if (o1.getPrice() == o2.getPrice()) {
            return 0;
        }
        return o1.getPrice() < o2.getPrice() ? -1 : 1;
    }
}

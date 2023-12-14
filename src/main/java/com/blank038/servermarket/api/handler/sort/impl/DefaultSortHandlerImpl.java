package com.blank038.servermarket.api.handler.sort.impl;

import com.blank038.servermarket.api.handler.sort.AbstractSortHandler;
import com.blank038.servermarket.internal.cache.sale.SaleCache;

/**
 * @author Blank038
 */
public class DefaultSortHandlerImpl extends AbstractSortHandler {

    public DefaultSortHandlerImpl() {
        super("default");
    }

    @Override
    public int compare(SaleCache o1, SaleCache o2) {
        return 0;
    }
}

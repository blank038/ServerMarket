package com.blank038.servermarket.api.handler.sort;

import com.blank038.servermarket.api.handler.sort.impl.DefaultSortHandlerImpl;
import com.blank038.servermarket.api.handler.sort.impl.PriceHighSortHandlerImpl;
import com.blank038.servermarket.api.handler.sort.impl.PriceLowSortHandlerImpl;
import com.blank038.servermarket.internal.data.DataContainer;

/**
 * @author Blank038
 */
public abstract class AbstractSortHandler implements SortHandler {
    protected final String sortKey;

    public AbstractSortHandler(String key) {
        this.sortKey = key;
    }

    public void register() {
        DataContainer.SORT_HANDLER_MAP.putIfAbsent(this.sortKey, this);
    }

    public static void registerDefaults() {
        new DefaultSortHandlerImpl().register();
        new PriceHighSortHandlerImpl().register();
        new PriceLowSortHandlerImpl().register();
    }
}

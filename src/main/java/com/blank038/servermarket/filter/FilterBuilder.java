package com.blank038.servermarket.filter;

import com.blank038.servermarket.data.sale.SaleItem;
import com.blank038.servermarket.filter.impl.TypeFilterImpl;
import com.blank038.servermarket.filter.interfaces.ISaleFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
public class FilterBuilder {
    private final List<ISaleFilter> saleFilters = new ArrayList<>();
    private TypeFilterImpl typeFilter;

    public FilterBuilder addKeyFilter(ISaleFilter saleFilter) {
        this.saleFilters.add(saleFilter);
        return this;
    }

    public FilterBuilder setTypeFilter(TypeFilterImpl typeFilter) {
        this.typeFilter = typeFilter;
        return this;
    }

    public boolean check(SaleItem saleItem) {
        if (this.typeFilter.check(saleItem)) {
            return true;
        }
        return this.saleFilters.stream().anyMatch((saleFilter) -> saleFilter.check(saleItem));
    }
}

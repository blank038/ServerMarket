package com.blank038.servermarket.api.handler.filter;

import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.api.handler.filter.interfaces.IFilter;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
@Getter
public class FilterHandler {
    private final List<IFilter> saleFilters = new ArrayList<>();
    private IFilter typeFilter;

    public FilterHandler addKeyFilter(IFilter saleFilter) {
        this.saleFilters.add(saleFilter);
        return this;
    }

    public FilterHandler setTypeFilter(IFilter typeFilter) {
        this.typeFilter = typeFilter;
        return this;
    }

    public boolean check(SaleCache saleItem) {
        if (this.typeFilter != null && this.typeFilter.check(saleItem)) {
            return true;
        }
        return this.saleFilters.stream().anyMatch((saleFilter) -> saleFilter.check(saleItem));
    }

    public boolean check(ItemStack itemStack) {
        return this.saleFilters.stream().anyMatch((saleFilter) -> saleFilter.check(itemStack));
    }
}

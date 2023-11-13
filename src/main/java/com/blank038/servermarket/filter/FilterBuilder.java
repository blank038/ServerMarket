package com.blank038.servermarket.filter;

import com.blank038.servermarket.data.cache.sale.SaleCache;
import com.blank038.servermarket.filter.impl.TypeFilterImpl;
import com.blank038.servermarket.filter.interfaces.IFilter;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
@Getter
public class FilterBuilder {
    private final List<IFilter> saleFilters = new ArrayList<>();
    private TypeFilterImpl typeFilter;

    public FilterBuilder addKeyFilter(IFilter saleFilter) {
        this.saleFilters.add(saleFilter);
        return this;
    }

    public FilterBuilder setTypeFilter(TypeFilterImpl typeFilter) {
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

package com.blank038.servermarket.filter.interfaces;

import com.blank038.servermarket.data.cache.sale.SaleCache;
import org.bukkit.inventory.ItemStack;

public interface IFilter {

    boolean check(SaleCache saleItem);

    boolean check(ItemStack itemStack);
}

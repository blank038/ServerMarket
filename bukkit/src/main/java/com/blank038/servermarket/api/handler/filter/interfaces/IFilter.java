package com.blank038.servermarket.api.handler.filter.interfaces;

import com.blank038.servermarket.internal.cache.sale.SaleCache;
import org.bukkit.inventory.ItemStack;

public interface IFilter {

    boolean check(SaleCache saleItem);

    boolean check(ItemStack itemStack);
}

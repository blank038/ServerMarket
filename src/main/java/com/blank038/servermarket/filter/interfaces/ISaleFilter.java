package com.blank038.servermarket.filter.interfaces;

import com.blank038.servermarket.data.cache.sale.SaleItem;
import org.bukkit.inventory.ItemStack;

public interface ISaleFilter {

    boolean check(SaleItem saleItem);

    boolean check(ItemStack itemStack);
}

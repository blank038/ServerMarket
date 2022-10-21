package com.blank038.servermarket.filter.interfaces;

import com.blank038.servermarket.data.sale.SaleItem;

public interface ISaleFilter {

    boolean check(SaleItem itemStack);
}

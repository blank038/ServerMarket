package com.blank038.servermarket.data.cache.other;

import com.blank038.servermarket.data.cache.sale.SaleCache;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * @author Blank038
 */
@Getter
@Builder
public class SaleLog {
    private UUID triggerPlayerUUID;
    private SaleCache saleCache;
    private long triggerTime;
}

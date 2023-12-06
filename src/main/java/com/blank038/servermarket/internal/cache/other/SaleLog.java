package com.blank038.servermarket.internal.cache.other;

import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.internal.enums.LogType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * @author Blank038
 */
@Getter
@Builder
public class SaleLog {
    private final LogType logType;
    private UUID triggerPlayerUUID;
    private SaleCache saleCache;
    private String sourceMarket;
    private long triggerTime;
}

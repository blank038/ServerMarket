package com.blank038.servermarket.internal.provider;

import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.gui.context.GuiContext;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * @author Blank038
 */
public class ActionProvider {

    public static void runAction(MarketData marketData, Player player, String uuid, SaleCache saleCache, ClickType clickType, GuiContext context) {
        if (!DataContainer.ACTION_TYPE_MAP.containsKey(clickType)) {
            return;
        }
        DataContainer.ACTION_TYPE_MAP.get(clickType).run(marketData, player, uuid, saleCache, context);
    }
}

package com.blank038.servermarket.internal.gui.impl;

import com.blank038.servermarket.internal.cache.player.PlayerCache;
import com.blank038.servermarket.internal.listen.AbstractListener;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TakeOffListener extends AbstractListener {

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        if(!(whoClicked instanceof Player)) return;
        Player player = (Player) whoClicked;
        Inventory inventory = event.getInventory();
        if(!(inventory.getHolder() instanceof MarketGui)) return;
        int slot = event.getSlot();
        if(slot < 0 || slot > 44) return;
        ItemStack currentItem = event.getCurrentItem();
        if(currentItem == null || currentItem.getType() == Material.AIR) return;
        ClickType click = event.getClick();
        if(click != ClickType.SHIFT_RIGHT) return;
        NBTItem nbtItem = new NBTItem(currentItem);
        UUID uuid = player.getUniqueId();
        if(!uuid.toString().equals(nbtItem.getString("SaleUUID"))) return;
        PlayerCache playerCache = ServerMarket.getStorageHandler().getOrLoadPlayerCache(uuid, true);
        playerCache.addStoreItem(currentItem, "Take-Off");

    }
}

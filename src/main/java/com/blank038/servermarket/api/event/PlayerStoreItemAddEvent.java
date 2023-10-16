package com.blank038.servermarket.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author Blank038
 */
@Getter
@Setter
public class PlayerStoreItemAddEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID ownerUniqueId;
    /**
     * When you modify reason, please do not delete the original reason.
     * Correct should be: "buy" -> "buy(modify[MyPlugin]: new reason)"
     */
    private String reason;
    private ItemStack itemStack;

    public PlayerStoreItemAddEvent(UUID uuid, ItemStack itemStack, String reason) {
        super(true);
        this.ownerUniqueId = uuid;
        this.itemStack = itemStack;
        this.reason = reason;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

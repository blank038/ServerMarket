package com.blank038.servermarket.api.event;

import com.blank038.servermarket.dto.IStorageHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Blank038
 */
@Getter
@Setter
public class InitializeStorageHandlerEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();


    private IStorageHandler storageHandler;

    public InitializeStorageHandlerEvent(IStorageHandler handler) {
        this.storageHandler = handler;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

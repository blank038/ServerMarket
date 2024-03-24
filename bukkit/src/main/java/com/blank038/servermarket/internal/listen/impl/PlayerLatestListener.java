package com.blank038.servermarket.internal.listen.impl;

import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.listen.AbstractListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandSendEvent;

/**
 * @author Blank038
 */
public class PlayerLatestListener extends AbstractListener {

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().addAll(DataContainer.REGISTERED_COMMAND);
    }
}

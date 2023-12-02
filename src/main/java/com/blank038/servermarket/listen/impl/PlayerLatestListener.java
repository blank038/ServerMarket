package com.blank038.servermarket.listen.impl;

import com.blank038.servermarket.data.DataContainer;
import com.blank038.servermarket.listen.AbstractListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

/**
 * @author Blank038
 */
public class PlayerLatestListener extends AbstractListener
        implements Listener {

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().addAll(DataContainer.REGISTERED_COMMAND);
    }
}

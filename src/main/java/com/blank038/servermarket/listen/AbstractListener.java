package com.blank038.servermarket.listen;

import com.blank038.servermarket.ServerMarket;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * @author Blank038
 */
public abstract class AbstractListener implements Listener {

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, ServerMarket.getInstance());
    }
}

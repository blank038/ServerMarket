package com.blank038.servermarket.internal.platform;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.api.platform.IPlatformApi;
import com.blank038.servermarket.internal.platform.bukkit.BukkitPlatformApi;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.util.CoreUtil;

import java.util.logging.Level;

/**
 * @author Blank038
 */
public class PlatformHandler {

    public static void initPlatform() {
        if (CoreUtil.isFolia()) {
            try {
                Class<? extends IPlatformApi> classes = (Class<? extends IPlatformApi>) Class.forName("com.blank038.servermarket.internal.platform.folia.FoliaPlatformApi");
                setPlatform(classes.newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                ServerMarket.getInstance().getLogger().log(Level.SEVERE, "Failed to initialize platform for Folia");
            }
        } else {
            setPlatform(new BukkitPlatformApi());
        }
    }

    public static void setPlatform(IPlatformApi platform) {
        ServerMarketApi.setPlatformApi(platform);
    }
}

package com.blank038.servermarket.internal.util;

/**
 * @author Blank038
 */
public class CoreUtil {

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

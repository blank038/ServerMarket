package com.blank038.servermarket.i18n;

import com.blank038.servermarket.ServerMarket;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

/**
 * 语言读取类
 *
 * @author Blank038
 */
public class I18n {
    private static FileConfiguration data;

    public I18n() {
        reload();
    }

    public void reload() {
        File file = new File(ServerMarket.getInstance().getDataFolder(), "lang.yml");
        if (!file.exists()) {
            ServerMarket.getInstance().saveResource("lang.yml", true);
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public static String getString(String key, boolean prefix) {
        return ChatColor.translateAlternateColorCodes('&',
                ((prefix ? data.getString("prefix", "") : "") + data.getString(key, "")));
    }

    public static List<String> getStringList(String key) {
        return data.getStringList(key);
    }
}
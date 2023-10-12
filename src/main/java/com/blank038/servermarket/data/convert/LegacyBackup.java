package com.blank038.servermarket.data.convert;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.i18n.I18n;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class LegacyBackup {

    public static void check() {
        if (ServerMarket.getInstance().getDataFolder().exists()) {
            File file = new File(ServerMarket.getInstance().getDataFolder(), "history.yml");
            if (!file.exists()) {
                File backupFolder = new File("./backups");
                backupFolder.mkdir();

                ServerMarket.getInstance().getConsoleLogger().log(false, I18n.getProperties().getProperty("backup-start"));
                // backups data
                try {
                    String fileName = "ServerMarket-" + System.currentTimeMillis();
                    FileUtils.copyDirectory(ServerMarket.getInstance().getDataFolder(), new File(backupFolder, fileName));
                    ServerMarket.getInstance().saveResource("history.yml", "history.yml");
                    ServerMarket.getInstance().getConsoleLogger().log(false,
                            I18n.getProperties().getProperty("backup-completed").replace("%s", fileName));
                } catch (IOException e) {
                    ServerMarket.getInstance().getLogger().log(Level.SEVERE, e, () -> "Failed to backup data.");
                    Bukkit.getPluginManager().disablePlugin(ServerMarket.getInstance());
                }
            }
        }
    }
}

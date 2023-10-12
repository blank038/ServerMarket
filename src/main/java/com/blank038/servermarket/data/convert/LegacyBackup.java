package com.blank038.servermarket.data.convert;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.i18n.I18n;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                    Files.createDirectories(Paths.get("./backups", fileName));
                    LegacyBackup.copyFile(ServerMarket.getInstance().getDataFolder(), new File(backupFolder, fileName));
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

    private static boolean copyFile(File in, File out) throws IOException {
        if (in.exists()) {
            if (in.isDirectory()) {
                out.mkdir();
                for (File i : in.listFiles()) {
                    LegacyBackup.copyFile(new File(in, i.getName()), new File(out, i.getName()));
                }
            } else if (in.isFile()) {
                InputStreamReader isr = new InputStreamReader(Files.newInputStream(in.toPath()), StandardCharsets.UTF_8);
                OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(out.toPath()),StandardCharsets.UTF_8);
                char[] bytes = new char[1024];
                int len;
                while ((len = isr.read(bytes)) > 0) {
                    osw.write(bytes, 0, len);
                }
                isr.close();
                osw.flush();
                osw.close();
            }
            return true;
        }
        return false;
    }
}

package com.blank038.servermarket.command;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.config.LangConfiguration;
import com.blank038.servermarket.data.MarketContainer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {
    private final ServerMarket main;

    public MainCommand(ServerMarket serverMarket) {
        main = serverMarket;
    }

    /**
     * 命令执行器
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
        } else {
            switch (args[0]) {
                case "open":
                    openServerMarket(sender);
                    break;
                case "help":
                    sendHelp(sender);
                    break;
                default:
                    if (main.getConfig().getBoolean("short-command")) {
                        // 打开全球市场
                        openServerMarket(sender);
                    } else {
                        sendHelp(sender);
                    }
                    break;
            }
        }
        return true;
    }

    /**
     * 打开全球市场
     */
    private void openServerMarket(CommandSender sender) {
        if (sender instanceof Player) {
            new MarketContainer((Player) sender);
        }
    }

    /**
     * 发送命令帮助
     */
    private void sendHelp(CommandSender sender) {
        for (String text : LangConfiguration.getStringList("help." +
                (sender.hasPermission("servermarket.admin") ? "admin" : "default"))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
        }
    }
}

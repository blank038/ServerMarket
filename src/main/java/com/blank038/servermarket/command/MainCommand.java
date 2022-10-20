package com.blank038.servermarket.command;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.i18n.I18n;
import com.blank038.servermarket.data.storage.MarketData;
import com.blank038.servermarket.data.storage.StoreContainer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @author Blank038
 */
public class MainCommand implements CommandExecutor {
    private final ServerMarket INSTANCE;

    public MainCommand(ServerMarket serverMarket) {
        INSTANCE = serverMarket;
    }

    /**
     * 命令执行器
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (INSTANCE.getConfig().getBoolean("command-help")) {
                this.sendHelp(sender, label);
            } else {
                // 打开全球市场
                if (sender instanceof Player) {
                    this.openServerMarket(sender, null);
                }
            }
        } else {
            switch (args[0]) {
                case "open":
                    this.openServerMarket(sender, args.length == 1 ? null : args[1]);
                    break;
                case "search":
                    this.searchItemsAndOpenMarket(sender, args);
                    break;
                case "show":
                    show(sender);
                    break;
                case "box":
                    if (sender instanceof Player) {
                        new StoreContainer((Player) sender, 1, null).open(1);
                    }
                    break;
                case "reload":
                    if (sender.hasPermission("servermarket.admin")) {
                        INSTANCE.loadConfig();
                        sender.sendMessage(I18n.getString("reload", true));
                    }
                    break;
                default:
                    this.sendHelp(sender, label);
                    break;
            }
        }
        return true;
    }

    /**
     * 打开全球市场
     */
    private void openServerMarket(CommandSender sender, String key) {
        if (!(sender instanceof Player)) {
            return;
        }
        INSTANCE.getApi().openMarket((Player) sender, key, 1, null);
    }

    /**
     * 搜索全球市场并打开市场
     */
    private void searchItemsAndOpenMarket(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(I18n.getString("wrong-market", true));
            return;
        }
        if (args.length == 2) {
            sender.sendMessage(I18n.getString("wrong-key", true));
            return;
        }
        INSTANCE.getApi().openMarket((Player) sender, args[1], 1, args[2]);
    }

    /**
     * 发送市场状态
     */
    private void show(CommandSender sender) {
        for (String line : I18n.getStringList("show")) {
            String last = line;
            for (Map.Entry<String, MarketData> entry : MarketData.MARKET_DATA.entrySet()) {
                String value = "%" + entry.getValue().getMarketKey() + "%";
                if (last.contains(value)) {
                    // 开始设置变量
                    String permission = entry.getValue().getPermission();
                    if (permission != null && !"".equals(permission) && !sender.hasPermission(permission)) {
                        last = last.replace(value, INSTANCE.getConfig().getString("status-text.no-permission"));
                        continue;
                    }
                    last = last.replace(value, INSTANCE.getConfig().getString("status-text." + entry.getValue().getMarketStatus().name().toLowerCase()));
                }
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', last));
        }
    }

    /**
     * 发送命令帮助
     */
    private void sendHelp(CommandSender sender, String label) {
        for (String text : I18n.getStringList("help." +
                (sender.hasPermission("servermarket.admin") ? "admin" : "default"))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', text).replace("%c", label));
        }
    }
}
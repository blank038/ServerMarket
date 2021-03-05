package com.blank038.servermarket.command;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.config.LangConfiguration;
import com.blank038.servermarket.data.gui.SaleItem;
import com.blank038.servermarket.data.gui.StoreContainer;
import com.blank038.servermarket.enums.PayType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

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
                case "show":

                    break;
                case "box":
                    if (sender instanceof Player) {
                        new StoreContainer((Player) sender, 1).open(1);
                    }
                    break;
                case "reload":
                    if (sender.hasPermission("servermarket.admin")) {
                        INSTANCE.loadConfig();
                        sender.sendMessage(LangConfiguration.getString("reload", true));
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
        INSTANCE.getApi().openMarket((Player) sender, key);
    }

    /**
     * 发送命令帮助
     */
    private void sendHelp(CommandSender sender, String label) {
        for (String text : LangConfiguration.getStringList("help." +
                (sender.hasPermission("servermarket.admin") ? "admin" : "default"))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', text).replace("%c", label));
        }
    }
}
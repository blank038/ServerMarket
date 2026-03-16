package com.blank038.servermarket.internal.command.virtual;

import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.internal.gui.context.GuiContext;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.internal.gui.impl.MarketGui;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.internal.provider.SellHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Blank038
 */
public class VirtualMarketCommand extends Command {
    private final MarketData marketData;

    public VirtualMarketCommand(MarketData marketData) {
        super(marketData.getShortCommand());
        this.marketData = marketData;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            this.performSellCommand(player, strings);
        }
        return true;
    }

    private void performSellCommand(Player player, String[] args) {
        if (this.marketData.getPermission() != null && !this.marketData.getShortCommand().isEmpty()
                && !player.hasPermission(this.marketData.getPermission())) {
            player.sendMessage(I18n.getStrAndHeader("no-permission"));
            return;
        }
        if (args.length == 0) {
            // Initialize context for the first time, reading settings from market configuration
            GuiContext context = GuiContext.normal(this.marketData.getMarketKey());
            if (!this.marketData.getSorts().isEmpty()) {
                context.setSort(this.marketData.getSorts().get(0));
            }
            if (!this.marketData.getSaleTypes().isEmpty()) {
                context.setType(this.marketData.getSaleTypes().get(0));
            }
            new MarketGui(context).openGui(player);
            return;
        }
        if (args.length == 1) {
            player.sendMessage(I18n.getStrAndHeader("price-null"));
            return;
        }
        SellHelper.performSell(player, this.marketData, args[1]);
    }
}

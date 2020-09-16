package com.blank038.servermarket.data;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.config.LangConfiguration;
import com.mc9y.blank038api.util.inventory.GuiModel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;

public class MarketContainer {
    private final Player player;

    public MarketContainer(Player player) {
        this.player = player;
    }

    public void openGui(int currentPage) {
        // 指向文件
        File file = new File(ServerMarket.getInstance().getDataFolder(), "gui.yml");
        // 读取配置文件
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        GuiModel guiModel = new GuiModel(data.getString("title"), data.getInt("size"));
        // 设置界面状态
        guiModel.setListener(true);
        guiModel.setCloseRemove(true);
        // 设置界面物品
        HashMap<Integer, ItemStack> items = new HashMap<>();

        guiModel.setItem(items);
        guiModel.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                ItemStack itemStack = e.getCurrentItem();
                String key = ServerMarket.getInstance().getNBTBase().get(itemStack, "SaleUUID"),
                        action = ServerMarket.getInstance().getNBTBase().get(itemStack, "MarketAction");
                if (key != null) {
                    // 购买商品
                    buySaleItem((Player) e.getWhoClicked(), key);
                } else if (action != null) {
                    // 判断交互方式
                    switch (action) {
                        case "up":
                            break;
                        case "down":
                            break;
                        case "":
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        // 打开界面
        guiModel.openInventory(player);
    }

    public void buySaleItem(Player buyer, String uuid) {
        // 判断商品是否存在
        if (!ServerMarket.getInstance().sales.containsKey(uuid)) {
            buyer.sendMessage(LangConfiguration.getString("error-sale", true));
            return;
        }
        SaleItem saleItem = ServerMarket.getInstance().sales.get(uuid);
        if(ServerMarket.getInstance().getEconomyBridge().balance(buyer) < saleItem.getPrice()) {
            buyer.sendMessage(LangConfiguration.getString("lack-money", true));
            return;
        }
        // 继续判断

    }
}

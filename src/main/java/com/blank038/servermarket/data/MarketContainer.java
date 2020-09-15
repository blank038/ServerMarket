package com.blank038.servermarket.data;

import com.blank038.servermarket.ServerMarket;
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
        // 打开界面
        guiModel.openInventory(player);
    }
}

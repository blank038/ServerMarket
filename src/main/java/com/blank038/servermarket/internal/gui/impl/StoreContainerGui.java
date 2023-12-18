package com.blank038.servermarket.internal.gui.impl;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.blank038.servermarket.api.ServerMarketApi;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.cache.player.PlayerCache;
import com.blank038.servermarket.api.handler.filter.FilterHandler;
import com.blank038.servermarket.internal.gui.AbstractGui;
import com.blank038.servermarket.internal.i18n.I18n;
import com.blank038.servermarket.internal.util.ItemUtil;
import com.blank038.servermarket.internal.util.TextUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Blank038
 */
public class StoreContainerGui extends AbstractGui {
    private final Player target;
    private final int marketPage;
    private final String oldMarket;
    private final FilterHandler filterHandler;

    public StoreContainerGui(Player player, int marketPage, String oldMarket, FilterHandler filterHandler) {
        this.target = player;
        this.marketPage = marketPage;
        this.oldMarket = oldMarket;
        this.filterHandler = filterHandler;
    }

    public void open(int currentPage) {
        Optional<PlayerCache> playerData = ServerMarket.getStorageHandler().getPlayerDataByCache(target.getUniqueId());
        if (playerData.isPresent()) {
            // 指向文件
            File file = new File(ServerMarket.getInstance().getDataFolder(), "gui/store.yml");
            // 读取配置文件
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            GuiModel guiModel = new GuiModel(data.getString("title"), data.getInt("size"));
            // 设置界面状态
            guiModel.registerListener(ServerMarket.getInstance());
            guiModel.setCloseRemove(true);
            // 开始遍历设置物品
            if (data.contains("items")) {
                for (String key : data.getConfigurationSection("items").getKeys(false)) {
                    ConfigurationSection section = data.getConfigurationSection("items." + key);
                    ItemStack itemStack = ItemUtil.generateItem(section.getString("type"),
                            section.getInt("amount"),
                            (short) section.getInt("data"),
                            section.getInt("customModel", -1));
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(TextUtil.formatHexColor(section.getString("name")));
                    // 开始遍历设置Lore
                    List<String> list = new ArrayList<>(section.getStringList("lore"));
                    list.replaceAll(TextUtil::formatHexColor);
                    itemMeta.setLore(list);
                    itemStack.setItemMeta(itemMeta);
                    // 开始判断是否有交互操作
                    if (section.contains("action")) {
                        NBTItem nbtItem = new NBTItem(itemStack);
                        nbtItem.setString("action", section.getString("action"));
                        itemStack = nbtItem.getItem();
                    }
                    // 开始判断槽位方向
                    for (int i : CommonUtil.formatSlots(section.getString("slot"))) {
                        guiModel.setItem(i, itemStack);
                    }
                }
            }
            // 开始获取玩家仓库
            Integer[] slots = CommonUtil.formatSlots(data.getString("store-item-slots"));
            Map<String, ItemStack> storeItems = playerData.get().getStoreItems();
            String[] keys = storeItems.keySet().toArray(new String[0]);
            // 计算下标
            int start = slots.length * (currentPage - 1), end = slots.length * currentPage;
            for (int i = start, index = 0; i < end; i++, index++) {
                if (index >= slots.length || i >= keys.length) {
                    break;
                }
                NBTItem nbtItem = new NBTItem(storeItems.get(keys[i]));
                nbtItem.setString("StoreID", keys[i]);
                guiModel.setItem(slots[index], nbtItem.getItem());
            }
            guiModel.execute((e) -> {
                e.setCancelled(true);
                if (e.getClickedInventory() == e.getInventory()) {
                    // 获取点击的玩家
                    Player clicker = (Player) e.getWhoClicked();
                    // 获取点击的物品和物品的Tag
                    ItemStack itemStack = e.getCurrentItem();
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        return;
                    }
                    if (this.isCooldown(clicker.getUniqueId())) {
                        clicker.sendMessage(I18n.getStrAndHeader("cooldown"));
                        return;
                    }
                    NBTItem nbtItem = new NBTItem(itemStack);
                    String storeId = nbtItem.getString("StoreID"), action = nbtItem.getString("action");
                    if ("market".equalsIgnoreCase(action)) {
                        ServerMarketApi.openMarket(clicker, this.oldMarket, this.marketPage, this.filterHandler);
                    } else if (storeId != null && !storeId.isEmpty()) {
                        this.getItem(clicker, storeId, currentPage);
                    }
                }
            });
            guiModel.openInventory(target);
        }
    }

    public void getItem(Player player, String uuid, int currentPage) {
        ServerMarket.getStorageHandler().getPlayerDataByCache(player.getUniqueId()).ifPresent((data) -> {
            if (data.hasStoretem(uuid)) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(I18n.getStrAndHeader("inventory-full"));
                    return;
                }
                // 给予玩家物品
                ItemStack itemStack = ServerMarket.getStorageHandler().removeStoreItem(player.getUniqueId(), uuid);
                if (itemStack != null) {
                    String displayMmae = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                            itemStack.getItemMeta().getDisplayName() : itemStack.getType().name();
                    player.getInventory().addItem(itemStack);
                    player.sendMessage(I18n.getStrAndHeader("get-store-item")
                            .replace("%item%", displayMmae)
                            .replace("%amount%", String.valueOf(itemStack.getAmount())));
                    // 刷新玩家界面
                    this.open(currentPage);
                } else {
                    player.sendMessage(I18n.getStrAndHeader("error-store"));
                }
            } else {
                player.sendMessage(I18n.getStrAndHeader("error-store"));
            }
        });
    }
}

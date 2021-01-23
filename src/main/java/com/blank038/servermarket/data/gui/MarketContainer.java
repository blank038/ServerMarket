package com.blank038.servermarket.data.gui;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.config.LangConfiguration;
import com.blank038.servermarket.enums.PayType;
import com.google.common.collect.Lists;
import com.mc9y.blank038api.util.inventory.GuiModel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Blank038
 */
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
        // 开始遍历设置物品
        if (data.contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection section = data.getConfigurationSection("items." + key);
                ItemStack itemStack = new ItemStack(Material.valueOf(section.getString("type").toUpperCase()), section.getInt("amount"));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));
                // 开始遍历设置Lore
                List<String> list = new ArrayList<>();
                for (String lore : section.getStringList("lore")) {
                    list.add(ChatColor.translateAlternateColorCodes('&', lore));
                }
                itemMeta.setLore(list);
                itemStack.setItemMeta(itemMeta);
                // 开始判断是否有交互操作
                if (section.contains("action")) {
                    itemStack = ServerMarket.getInstance().getNBTBase().addTag(itemStack, "MarketAction", section.getString("action"));
                }
                // 开始判断槽位方向
                if (section.getStringList("slot").isEmpty()) {
                    items.put(section.getInt("slot"), itemStack);
                } else {
                    for (int i : ServerMarket.getInstance().getApi().getInt(section.getStringList("slot"))) {
                        items.put(i, itemStack);
                    }
                }
            }
        }
        // 开始获取全球市场物品
        Integer[] slots = ServerMarket.getInstance().getApi().getInt(data.getStringList("sale-item-slots")).toArray(new Integer[0]);
        String[] keys = ServerMarket.getInstance().sales.keySet().toArray(new String[0]);
        // 计算下标
        int maxPage = ServerMarket.getInstance().sales.size() / slots.length;
        maxPage += (ServerMarket.getInstance().sales.size() % slots.length) == 0 ? 0 : 1;
        // 判断页面是否超标, 如果是的话就设置为第一页
        if (currentPage > maxPage) {
            currentPage = 1;
        }
        // 获得额外增加的信息
        List<String> extrasLore = data.getStringList("sale-info");
        int start = slots.length * (currentPage - 1), end = slots.length * currentPage;
        for (int i = start, index = 0; i < end; i++, index++) {
            if (index >= slots.length || i >= keys.length) {
                break;
            }
            // 开始设置物品
            SaleItem saleItem = ServerMarket.getInstance().sales.getOrDefault(keys[i], null);
            if (saleItem == null) {
                i -= 1;
                continue;
            }
            items.put(slots[index], getShowItem(saleItem, extrasLore));
        }
        guiModel.setItem(items);
        final int lastPage = currentPage, finalMaxPage = maxPage;
        guiModel.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                ItemStack itemStack = e.getCurrentItem();
                String key = ServerMarket.getInstance().getNBTBase().get(itemStack, "SaleUUID"),
                        action = ServerMarket.getInstance().getNBTBase().get(itemStack, "MarketAction");
                // 强转玩家
                Player clicker = (Player) e.getWhoClicked();
                if (key != null) {
                    // 购买商品
                    buySaleItem(clicker, key, e.isShiftClick());
                } else if (action != null) {
                    // 判断交互方式
                    switch (action) {
                        case "up":
                            if (lastPage == 1) {
                                clicker.sendMessage(LangConfiguration.getString("no-previous-page", true));
                            } else {
                                openGui(lastPage - 1);
                            }
                            break;
                        case "down":
                            if (lastPage >= finalMaxPage) {
                                clicker.sendMessage(LangConfiguration.getString("no-next-page", true));
                            } else {
                                openGui(lastPage + 1);
                            }
                            break;
                        case "store":
                            new StoreContainer(clicker, lastPage).open(1);
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

    public void buySaleItem(Player buyer, String uuid, boolean shift) {
        // 判断商品是否存在
        if (!ServerMarket.getInstance().sales.containsKey(uuid)) {
            buyer.sendMessage(LangConfiguration.getString("error-sale", true));
            return;
        }
        SaleItem saleItem = ServerMarket.getInstance().sales.get(uuid);
        if (saleItem.getOwnerName().equals(buyer.getName())) {
            if (shift) {
                buyer.getInventory().addItem(ServerMarket.getInstance().sales.remove(uuid).getSafeItem().clone());
                buyer.sendMessage(LangConfiguration.getString("unsale", true));
                new MarketContainer(buyer).openGui(1);
            } else {
                buyer.sendMessage(LangConfiguration.getString("shift-unsale", true));
            }
            return;
        }
        if (shift) {
            if (ServerMarket.getInstance().getEconomyBridge().balance(buyer) < saleItem.getPrice()) {
                buyer.sendMessage(LangConfiguration.getString("lack-money", true));
                return;
            }
            // 判断货币类型, 不要问我为什么要判断！后续新增货币扩展延伸性好！
            if (saleItem.getPayType() != PayType.VAULT) {
                return;
            }
            // 先移除, 确保不被重复购买
            ServerMarket.getInstance().sales.remove(uuid);
            // 先给玩家钱扣了！
            ServerMarket.getInstance().getEconomyBridge().take(player, saleItem.getPrice());
            // 再把钱给出售者
            Player seller = Bukkit.getPlayer(UUID.fromString(saleItem.getOwnerUUID()));
            if (seller != null && seller.isOnline()) {
                double last = ServerMarket.getInstance().getApi().getLastMoney(seller, saleItem.getPrice());
                DecimalFormat df = new DecimalFormat("#.00");
                ServerMarket.getInstance().getEconomyBridge().give(seller, last);
                seller.sendMessage(LangConfiguration.getString("sale-sell", true).replace("%money%", df.format(saleItem.getPrice()))
                        .replace("%last%", df.format(last)));
            } else {
                ServerMarket.getInstance().addMoney(saleItem.getOwnerName(), saleItem.getPrice());
            }
            // 再给购买者物品
            ServerMarket.getInstance().getApi().addItem(buyer.getName(), saleItem);
        } else {
            buyer.sendMessage(LangConfiguration.getString("shift-buy", true));
        }
    }

    /**
     * 获得市场展示物品
     *
     * @param saleItem 市场商品信息
     * @param infoLore 额外增加信息
     * @return 展示物品
     */
    public ItemStack getShowItem(SaleItem saleItem, List<String> infoLore) {
        ItemStack itemStack = saleItem.getSafeItem().clone();
        if (ServerMarket.getInstance().getConfig().getBoolean("sale-info")) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : Lists.newArrayList();
            // 设置物品格式
            Date date = new Date(saleItem.getPostTime());
            SimpleDateFormat sdf = new SimpleDateFormat(ServerMarket.getInstance().getConfig().getString("simple-date-format"));
            // 设置额外信息
            for (String i : infoLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', i).replace("%seller%", saleItem.getOwnerName())
                        .replace("%price%", String.valueOf(saleItem.getPrice())).replace("%time%", sdf.format(date)));
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
        return ServerMarket.getInstance().getNBTBase().addTag(itemStack, "SaleUUID", saleItem.getSaleUUID());
    }
}

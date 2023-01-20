package com.blank038.servermarket.data;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.cache.sale.SaleItem;
import com.blank038.servermarket.filter.FilterBuilder;
import com.blank038.servermarket.filter.impl.KeyFilterImpl;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Blank038
 */
public class DataContainer {
    public static final Map<String, List<String>> SALE_TYPES = new HashMap<>();
    public static final Map<String, String> SALE_TYPE_DISPLAY_NAME = new HashMap<>();

    public static void loadData() {
        ServerMarket.getInstance().saveResource("types.yml", "types.yml", false, (file) -> {
            DataContainer.SALE_TYPES.clear();
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            if (data.contains("types")) {
                for (String key : data.getConfigurationSection("types").getKeys(false)) {
                    DataContainer.SALE_TYPES.put(key, data.getStringList("types." + key));
                }
            }
            if (data.contains("default")) {
                for (String key : data.getConfigurationSection("default").getKeys(false)) {
                    DataContainer.SALE_TYPE_DISPLAY_NAME.put(key, ChatColor.translateAlternateColorCodes('&', data.getString("default." + key)));
                }
            }
        });
    }

    public static void setSaleTypes(SaleItem saleItem) {
        List<String> types = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : DataContainer.SALE_TYPES.entrySet()) {
            FilterBuilder builder = new FilterBuilder().addKeyFilter(new KeyFilterImpl(entry.getValue()));
            if (builder.check(saleItem)) {
                types.add(entry.getKey());
            }
        }
        if (!types.isEmpty()) {
            saleItem.setSaleTypes(types);
        }
    }
}

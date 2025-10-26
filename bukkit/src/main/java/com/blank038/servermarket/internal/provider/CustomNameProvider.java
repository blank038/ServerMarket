package com.blank038.servermarket.internal.provider;

import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.util.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CustomNameProvider {
    private static final Map<String, String> MATERIALS = new HashMap<>();
    private static final Function<ItemStack, String> customNameFunc = (item) -> {
        return MATERIALS.getOrDefault(item.getType().name(), item.getType().name());
    };
    private static boolean customName;

    public static void pretreatment() {
        String res = "customDisplayNames.yml";
        ServerMarket.getInstance().saveResource(res, res, false, (file) -> {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);

            MATERIALS.clear();

            customName = data.getBoolean("customName");
            ConfigurationSection materialSection = data.getConfigurationSection("materials");
            for (String key : materialSection.getKeys(false)) {
                MATERIALS.put(key.toUpperCase(), TextUtil.formatHexColor(materialSection.getString(key)));
            }
        });
    }

    public static String getCustomName(ItemStack itemStack) {
        String type = itemStack.getType().name();

        // Check if metadata exists; return directly if absent.
        if (!itemStack.hasItemMeta()) {
            return MATERIALS.getOrDefault(type, type);
        }

        // Check if custom name takes priority.
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasDisplayName()) {
            String displayName = itemMeta.getDisplayName();
            return customName ? MATERIALS.getOrDefault(type, displayName) : displayName;
        }

        return MATERIALS.getOrDefault(type, type);
    }
}

package com.blank038.servermarket.internal.provider;

import com.blank038.servermarket.internal.data.DataContainer;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class CustomNameProvider {
    private static final Map<String, String> MATERIALS = new HashMap<>();
    private static boolean customName;

    public static void pretreatment() {
        String res = "customDisplayNames.yml";
        ServerMarket.getInstance().saveResource(res, res, false, (file) -> {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);

            MATERIALS.clear();

            customName = data.getBoolean("customName");
            ConfigurationSection materialSection = data.getConfigurationSection("materials");

            if (materialSection != null) {
                for (String key : materialSection.getKeys(false)) {
                    MATERIALS.put(key.toUpperCase(), TextUtil.formatHexColor(materialSection.getString(key)));
                }
            }
        });
    }

    public static String getCustomName(ItemStack itemStack) {
        String type = itemStack.getType().name();
        String clientName = getClientName(itemStack);
        String materialName = MATERIALS.getOrDefault(type, clientName == null ? type : clientName);

        // Check if metadata exists; return directly if absent.
        if (!itemStack.hasItemMeta()) {
            return materialName;
        }

        // Check if custom name takes priority.
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && itemMeta.hasDisplayName()) {
            String displayName = itemMeta.getDisplayName();

            if (customName && MATERIALS.containsKey(type)) {
                return MATERIALS.get(type);
            }

            return displayName;
        }

        return materialName;
    }

    private static String getClientName(ItemStack itemStack) {
        Material material = itemStack.getType();
        NamespacedKey namespacedKey = material.getKey();
        String type = material.isBlock() ? "block" : "item";
        String finalKey = type + "." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
        return DataContainer.CLIENT_LANGUAGE.get(finalKey.toLowerCase());
    }
}

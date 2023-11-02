package com.blank038.servermarket.data;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.command.virtual.VirtualMarketCommand;
import com.blank038.servermarket.data.cache.market.MarketData;
import com.blank038.servermarket.data.cache.sale.SaleCache;
import com.blank038.servermarket.filter.FilterBuilder;
import com.blank038.servermarket.filter.impl.KeyFilterImpl;
import com.blank038.servermarket.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class DataContainer {
    public static final List<String> REGISTERED_COMMAND = new ArrayList<>();
    public static final Map<String, List<String>> SALE_TYPES = new HashMap<>();
    public static final Map<String, String> SALE_TYPE_DISPLAY_NAME = new HashMap<>();
    public static final HashMap<String, MarketData> MARKET_DATA = new HashMap<>();

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
                    DataContainer.SALE_TYPE_DISPLAY_NAME.put(key, TextUtil.formatHexColor(data.getString("default." + key)));
                }
            }
        });
        // Save all data
        if (!MARKET_DATA.isEmpty() && ServerMarket.getStorageHandler() != null) {
            ServerMarket.getStorageHandler().saveAll();
        }
        // Load market data
        File file = new File(ServerMarket.getInstance().getDataFolder(), "market");
        if (!file.exists()) {
            file.mkdir();
            // 输出
            ServerMarket.getInstance().saveResource("market/example.yml", "market/example.yml");
        }
        MARKET_DATA.clear();
        Arrays.stream(file.listFiles()).iterator().forEachRemaining(MarketData::new);
        // Register virtual command
        DataContainer.registerVirtualMarketCommands();
    }

    private static void registerVirtualMarketCommands() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                SimplePluginManager manager = (SimplePluginManager) Bukkit.getPluginManager();
                // get commandMap field and object
                Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(manager);
                // get knownCommands field and object
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
                // unregister current virtual commands
                new ArrayList<>(commandMap.getCommands()).forEach((s) -> {
                    if (!REGISTERED_COMMAND.contains(s.getLabel())) {
                        return;
                    }
                    if (s.unregister(commandMap)) {
                        knownCommands.remove(s.getLabel().toLowerCase());
                        knownCommands.remove((ServerMarket.getInstance().getDescription().getName() + ":" + s.getLabel()).toLowerCase());
                        REGISTERED_COMMAND.remove(s.getLabel());
                    }
                });
                // register new virtual commands.
                DataContainer.MARKET_DATA.forEach((k, v) -> {
                    VirtualMarketCommand command = new VirtualMarketCommand(v);
                    if (commandMap.register(ServerMarket.getInstance().getDescription().getName(), command)) {
                        REGISTERED_COMMAND.add(v.getShortCommand());
                    }
                });
                // update online player commands
                Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to register virtual command.");
        }
    }

    public static void setSaleTypes(SaleCache saleItem) {
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

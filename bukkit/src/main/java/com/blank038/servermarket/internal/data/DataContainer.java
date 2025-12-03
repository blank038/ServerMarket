package com.blank038.servermarket.internal.data;

import com.blank038.servermarket.api.handler.sort.SortHandler;
import com.blank038.servermarket.internal.enums.ActionType;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.command.virtual.VirtualMarketCommand;
import com.blank038.servermarket.api.entity.MarketData;
import com.blank038.servermarket.internal.cache.sale.SaleCache;
import com.blank038.servermarket.api.handler.filter.FilterHandler;
import com.blank038.servermarket.api.handler.filter.impl.KeyFilterImpl;
import com.blank038.servermarket.internal.i18n.client.ClientLanguageParser;
import com.blank038.servermarket.internal.util.TextUtil;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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
    public static final Map<String, String> SALE_TYPE_DISPLAY_NAME = new HashMap<>(),
            SORT_TYPE_DISPLAY_NAME = new HashMap<>();
    public static final Map<String, MarketData> MARKET_DATA = new HashMap<>();
    public static final Map<String, SortHandler> SORT_HANDLER_MAP = new HashMap<>();
    public static final Map<ClickType, ActionType> ACTION_TYPE_MAP = new HashMap<>();
    public static final Map<String, String> CLIENT_LANGUAGE = new HashMap<>();
    public static String defaultMarket;

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
        ServerMarket.getInstance().saveResource("sorts.yml", "sorts.yml", false, (file) -> {
            DataContainer.SORT_TYPE_DISPLAY_NAME.clear();
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            for (String key : data.getKeys(false)) {
                SORT_TYPE_DISPLAY_NAME.put(key, data.getString(key));
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
            // output default market files
            String sourceFile = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1) ? "market/example.yml" : "market/exampleLegacy.yml";
            ServerMarket.getInstance().saveResource(sourceFile, "market/example.yml");
        }
        MARKET_DATA.clear();
        Arrays.stream(file.listFiles()).iterator().forEachRemaining(MarketData::new);
        // Register virtual command
        DataContainer.registerVirtualMarketCommands();
        // load actions
        DataContainer.loadActions();
        // load client language
        DataContainer.loadClientLanguage();
        // load default market
        defaultMarket = ServerMarket.getInstance().getConfig().getString("default-market");
    }

    /**
     * Load the market actions.
     */
    private static void loadActions() {
        ServerMarket.getInstance().saveResource("action/market.yml", "action/market.yml", false, (file) -> {
            ACTION_TYPE_MAP.clear();
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            data.getKeys(false).forEach((key) -> {
                try {
                    ClickType type = ClickType.valueOf(key.toUpperCase());
                    ActionType action = ActionType.valueOf(data.getString(key).toUpperCase());
                    ACTION_TYPE_MAP.put(type, action);
                } catch (Exception e) {
                    ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to load market action: " + key);
                }
            });
        });
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
                if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
                    Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to register virtual command.");
        }
    }

    public static void setSaleTypes(SaleCache saleItem) {
        List<String> types = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : DataContainer.SALE_TYPES.entrySet()) {
            FilterHandler builder = new FilterHandler().addKeyFilter(new KeyFilterImpl(entry.getValue()));
            if (builder.check(saleItem)) {
                types.add(entry.getKey());
            }
        }
        if (!types.isEmpty()) {
            saleItem.setSaleTypes(types);
        }
    }

    public static boolean isLegacy() {
        return !MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1);
    }

    /**
     * Load client language files from the clientLanguage directory.
     * Only loads keys starting with "item." and "block."
     */
    private static void loadClientLanguage() {
        CLIENT_LANGUAGE.clear();

        File clientLanguageDir = new File(ServerMarket.getInstance().getDataFolder(), "clientLanguage");
        if (!clientLanguageDir.exists()) {
            ServerMarket.getInstance().saveResource("clientLanguage/example.json", "clientLanguage/example.json");
        }

        if (!clientLanguageDir.isDirectory()) {
            return;
        }

        File[] jsonFiles = clientLanguageDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles == null) {
            return;
        }

        for (File jsonFile : jsonFiles) {
            try {
                ClientLanguageParser parser = new ClientLanguageParser(jsonFile);
                Map<String, String> parsedData = parser.parse();
                CLIENT_LANGUAGE.putAll(parsedData);
            } catch (Exception e) {
                ServerMarket.getInstance().getLogger().log(Level.WARNING, "Failed to load client language file: " + jsonFile.getName(), e);
            }
        }
    }
}

package com.blank038.servermarket.internal.i18n;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.blank038.servermarket.internal.util.TextUtil;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * 语言读取类
 *
 * @author Blank038
 */
public class I18n {
    private static final String[] LANGUAGES = {"zh_CN.yml", "en_US.yml"};
    @Getter
    private static I18n instance;
    @Getter
    private static Properties properties;

    private final Map<String, String> stringOptions = new HashMap<>();
    private final Map<String, List<String>> arrayOptions = new HashMap<>();
    @Getter
    private String language, header;


    public I18n(String language) {
        this.init(language);
    }

    public void init(String language) {
        instance = this;
        this.language = language;
        this.stringOptions.clear();
        this.arrayOptions.clear();
        File folder = new File(ServerMarket.getInstance().getDataFolder(), "language");
        if (!folder.exists()) {
            folder.mkdir();
            for (String lang : LANGUAGES) {
                File tar = new File(folder, lang);
                CommonUtil.outputFileTool(ServerMarket.getInstance().getResource("language/" + lang), tar);
            }
        }
        // 读取语言配置文件
        File file = new File(folder, language + ".yml");
        if (!file.exists()) {
            file = new File(folder, "zh_CN.yml");
        }
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(true)) {
            if (data.isString(key)) {
                this.stringOptions.put(key, TextUtil.formatHexColor(data.getString(key)));
            } else if (data.isList(key)) {
                this.arrayOptions.put(key, data.getStringList(key).stream()
                        .map(TextUtil::formatHexColor)
                        .collect(Collectors.toList()));
            }
        }
        this.header = "prefix";
        // initialize properties
        properties = new Properties();
        try (InputStream resourceInputStream = ServerMarket.getInstance().getResource("properties/" + language + ".properties")) {
            properties.load(new InputStreamReader(resourceInputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to load properties file.");
        }
    }

    public static String getStrAndHeader(String key) {
        if (instance.header == null) {
            return I18n.getOption(key);
        }
        return I18n.getOption(instance.header, key);
    }

    public static String getOption(String header, String key) {
        return instance.stringOptions.getOrDefault(header, "") + instance.stringOptions.getOrDefault(key, "");
    }

    public static String getOption(String key) {
        if (instance.stringOptions.containsKey(key)) {
            return instance.stringOptions.get(key);
        }
        return "";
    }

    public static List<String> getArrayOption(String key) {
        if (instance.arrayOptions.containsKey(key)) {
            return new ArrayList<>(instance.arrayOptions.get(key));
        }
        return Lists.newArrayList();
    }
}
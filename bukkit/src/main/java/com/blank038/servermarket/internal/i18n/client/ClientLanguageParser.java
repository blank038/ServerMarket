package com.blank038.servermarket.internal.i18n.client;

import com.blank038.servermarket.internal.plugin.ServerMarket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ClientLanguageParser {
    private final Map<String, String> languageMap = new HashMap<>();
    private final File languageFile;
    
    public ClientLanguageParser(File languageFile) {
        this.languageFile = languageFile;
    }
    
    public Map<String, String> parse() {
        if (!languageFile.exists() || !languageFile.isFile()) {
            ServerMarket.getInstance().getLogger().log(Level.WARNING, "Client language file not found: " + languageFile.getName());
            return languageMap;
        }
        
        try (FileReader reader = new FileReader(languageFile)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
            
            for (Map.Entry<String, com.google.gson.JsonElement> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("item.") || key.startsWith("block.")) {
                    String value = entry.getValue().getAsString();
                    languageMap.put(key, value);
                }
            }
            
            ServerMarket.getInstance().getLogger().log(Level.INFO, "Loaded " + languageMap.size() + " client language entries from " + languageFile.getName());
            
        } catch (IOException e) {
            ServerMarket.getInstance().getLogger().log(Level.SEVERE, "Failed to parse client language file: " + languageFile.getName(), e);
        }
        
        return languageMap;
    }
}
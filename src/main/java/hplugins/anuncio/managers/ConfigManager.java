package hplugins.anuncio.managers;

import hplugins.anuncio.hAnuncio;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final hAnuncio plugin;
    private final Map<String, FileConfiguration> configFiles;
    
    public ConfigManager(hAnuncio plugin) {
        this.plugin = plugin;
        this.configFiles = new HashMap<>();
    }
    
    public void loadAllConfigs() {
        
        saveDefaultConfigs();
        
        
        loadConfig("config.yml");
        loadConfig("commands.yml");
        loadConfig("menu.yml");
        loadConfig("economy.yml");
        loadConfig("chat.yml");
        loadConfig("notificacao.yml");
        loadConfig("efeitos.yml");
        loadConfig("permissions.yml");
        loadConfig("categorias.yml");
    }
    
    private void saveDefaultConfigs() {
        String[] configFiles = {
                "config.yml",
                "commands.yml",
                "menu.yml",
                "economy.yml",
                "chat.yml",
                "notificacao.yml",
                "efeitos.yml",
                "permissions.yml",
                "categorias.yml"
        };
        
        for (String fileName : configFiles) {
            if (!new File(plugin.getDataFolder(), fileName).exists()) {
                plugin.saveResource(fileName, false);
            }
        }
    }
    
    public void loadConfig(String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        
        InputStream defaultConfigStream = plugin.getResource(fileName);
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
        
        configFiles.put(fileName, config);
    }
    
    public void saveConfig(String fileName) {
        if (configFiles.containsKey(fileName)) {
            try {
                configFiles.get(fileName).save(new File(plugin.getDataFolder(), fileName));
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save config file: " + fileName);
                e.printStackTrace();
            }
        }
    }
    
    public FileConfiguration getConfig() {
        return configFiles.getOrDefault("config.yml", plugin.getConfig());
    }
    
    public FileConfiguration getCommands() {
        return configFiles.getOrDefault("commands.yml", new YamlConfiguration());
    }
    
    public FileConfiguration getMenu() {
        return configFiles.getOrDefault("menu.yml", new YamlConfiguration());
    }
    
    public FileConfiguration getEconomy() {
        return configFiles.getOrDefault("economy.yml", new YamlConfiguration());
    }
    
    public FileConfiguration getChat() {
        return configFiles.getOrDefault("chat.yml", new YamlConfiguration());
    }
    
    public FileConfiguration getNotificacao() {
        return configFiles.getOrDefault("notificacao.yml", new YamlConfiguration());
    }
    
    public FileConfiguration getEfeitos() {
        return configFiles.getOrDefault("efeitos.yml", new YamlConfiguration());
    }
    
    public FileConfiguration getPermissions() {
        return configFiles.getOrDefault("permissions.yml", new YamlConfiguration());
    }
    
    public FileConfiguration getCategorias() {
        return configFiles.getOrDefault("categorias.yml", new YamlConfiguration());
    }
}

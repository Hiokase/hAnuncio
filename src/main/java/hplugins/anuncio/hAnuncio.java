package hplugins.anuncio;

import hplugins.anuncio.commands.AdminCommand;
import hplugins.anuncio.commands.AnunciarCommand;
import hplugins.anuncio.gui.MenuManager;
import hplugins.anuncio.listeners.MenuListener;
import hplugins.anuncio.managers.*;
import hplugins.anuncio.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class hAnuncio extends JavaPlugin {
    
    private static hAnuncio instance;
    private ConfigManager configManager;
    private EconomyManager economyManager;
    private CategoryManager categoryManager;
    private AnnouncementManager announcementManager;
    private PlayerManager playerManager;
    private MenuManager menuManager;
    private MenuListener menuListener;
    private PermissionManager permissionManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        
        VersionUtils.initialize(this);
        
        
        configManager = new ConfigManager(this);
        configManager.loadAllConfigs();
        
        
        permissionManager = new PermissionManager(this);
        
        
        economyManager = new EconomyManager(this);
        if (!economyManager.isEconomyEnabled()) {
            getLogger().warning("No economy plugin found! Disabling economy features...");
        }
        
        
        categoryManager = new CategoryManager(this);
        announcementManager = new AnnouncementManager(this);
        playerManager = new PlayerManager(this);
        
        
        getServer().getPluginManager().registerEvents(playerManager, this);
        
        
        menuListener = new MenuListener(this);
        getServer().getPluginManager().registerEvents(menuListener, this);
        menuManager = new MenuManager(this);
        
        
        getCommand("anunciar").setExecutor(new AnunciarCommand(this));
        getCommand("hac").setExecutor(new AdminCommand(this));
        
        getLogger().info("hAnuncio v" + getDescription().getVersion() + " enabled successfully!");
        getLogger().info("Running on MinecraFt " + VersionUtils.getMinecraftVersion());
    }
    
    @Override
    public void onDisable() {
        
        if (categoryManager != null) {
            
        }
        
        getLogger().info("hAnuncio disabled successfully!");
    }
    
    public static hAnuncio getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public CategoryManager getCategoryManager() {
        return categoryManager;
    }
    
    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public MenuManager getMenuManager() {
        return menuManager;
    }
    
    public MenuListener getMenuListener() {
        return menuListener;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}

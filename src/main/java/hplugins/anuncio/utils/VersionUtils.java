package hplugins.anuncio.utils;

import hplugins.anuncio.hAnuncio;
import org.bukkit.Bukkit;

public class VersionUtils {
    
    private static String minecraftVersion;
    private static int majorVersion;
    private static int minorVersion;
    
    /**
     * Initialize the version utilities
     * @param plugin The plugin instance
     */
    public static void initialize(hAnuncio plugin) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        minecraftVersion = version;
        
        
        if (version.contains("v")) {
            version = version.substring(1); 
            String[] parts = version.split("_");
            
            if (parts.length >= 2) {
                try {
                    majorVersion = Integer.parseInt(parts[0]);
                    minorVersion = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Failed to parse Minecraft version: " + version);
                    majorVersion = 1;
                    minorVersion = 7; 
                }
            }
        }
        
        plugin.getLogger().info("Server version detected: " + majorVersion + "." + minorVersion);
    }
    
    /**
     * Check if the server version is at least the specified version
     * @param major The major version
     * @param minor The minor version
     * @return True if the server version is at least the specified version
     */
    public static boolean isVersionAbove(int major, int minor) {
        return majorVersion > major || (majorVersion == major && minorVersion >= minor);
    }
    
    /**
     * Get the Minecraft version string
     * @return The Minecraft version string
     */
    public static String getMinecraftVersion() {
        return minecraftVersion;
    }
    
    /**
     * Get the major version number
     * @return The major version number
     */
    public static int getMajorVersion() {
        return majorVersion;
    }
    
    /**
     * Get the minor version number
     * @return The minor version number
     */
    public static int getMinorVersion() {
        return minorVersion;
    }
}

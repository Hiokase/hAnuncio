package hplugins.anuncio.managers;

import hplugins.anuncio.hAnuncio;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Gerencia permissões para o plugin
 */
public class PermissionManager {
    
    private final hAnuncio plugin;
    
    public PermissionManager(hAnuncio plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Verifica se um jogador tem uma permissão específica
     * 
     * @param playerName Nome do jogador
     * @param permission Permissão a verificar
     * @return true se o jogador tem a permissão, false caso contrário
     */
    public boolean hasPermission(String playerName, String permission) {
        Player player = Bukkit.getPlayerExact(playerName);
        
        if (player == null) {
            return false;
        }
        
        return hasPermission(player, permission);
    }
    
    /**
     * Verifica se um jogador tem uma permissão específica
     * 
     * @param player O jogador
     * @param permission Permissão a verificar
     * @return true se o jogador tem a permissão, false caso contrário
     */
    public boolean hasPermission(Player player, String permission) {
        if (player == null) {
            return false;
        }
        
        
        if (player.isOp()) {
            return true;
        }
        
        
        if (!permission.contains(".")) {
            permission = "hanuncio." + permission;
        }
        
        
        return player.hasPermission(permission) || 
               player.hasPermission("hanuncio.*") ||
               player.hasPermission("*");
    }
    
    /**
     * Obtém o prefixo de permissão para um comando específico
     * 
     * @param commandName Nome do comando
     * @return Prefixo de permissão
     */
    public String getCommandPermission(String commandName) {
        return plugin.getConfigManager().getPermissions().getString("command." + commandName, "hanuncio.command." + commandName);
    }
    
    /**
     * Obtém o prefixo de permissão para uma ação administrativa
     * 
     * @param actionName Nome da ação
     * @return Prefixo de permissão
     */
    public String getAdminPermission(String actionName) {
        return plugin.getConfigManager().getPermissions().getString("admin." + actionName, "hanuncio.admin." + actionName);
    }
}
package hplugins.anuncio.listeners;

import hplugins.anuncio.gui.Menu;
import hplugins.anuncio.hAnuncio;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener para eventos relacionados aos menus GUI
 */
public class MenuListener implements Listener {
    
    private final hAnuncio plugin;
    
    
    private final Map<UUID, String> playerMenus = new HashMap<>();
    
    public MenuListener(hAnuncio plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Registra que um jogador abriu um menu específico
     * 
     * @param player O jogador
     * @param menuName Nome do menu
     */
    public void registerMenuOpen(Player player, String menuName) {
        playerMenus.put(player.getUniqueId(), menuName);
    }
    
    /**
     * Remove o registro do menu de um jogador quando ele fecha o inventário
     * 
     * @param player O jogador
     */
    public void unregisterMenu(Player player) {
        playerMenus.remove(player.getUniqueId());
    }
    
    /**
     * Obtém o nome do menu que o jogador está visualizando
     * 
     * @param player O jogador
     * @return Nome do menu ou null se não estiver em nenhum menu
     */
    public String getOpenMenu(Player player) {
        return playerMenus.get(player.getUniqueId());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getView().getTopInventory();
        
        
        for (Menu menu : plugin.getMenuManager().getRegisteredMenus()) {
            if (menu.isThisMenu(inventory)) {
                event.setCancelled(true); 
                menu.handleClick(event); 
                return;
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        unregisterMenu(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        unregisterMenu(player);
    }
}
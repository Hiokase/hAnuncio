package hplugins.anuncio.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Interface que define o comportamento básico de um menu no sistema de GUI
 */
public interface Menu {
    
    /**
     * Abre o menu para um jogador
     * 
     * @param player O jogador para quem abrir o menu
     * @param args Argumentos adicionais que podem ser necessários para construir o menu
     */
    void open(Player player, Object... args);
    
    /**
     * Manipula um evento de clique no inventário
     * 
     * @param event O evento de clique
     * @return true se o evento foi manipulado, false caso contrário
     */
    boolean handleClick(InventoryClickEvent event);
    
    /**
     * Obtém o inventário do menu
     * 
     * @param player O jogador para quem o inventário será mostrado
     * @param args Argumentos adicionais que podem ser necessários para construir o inventário
     * @return O inventário construído
     */
    Inventory getInventory(Player player, Object... args);
    
    /**
     * Verifica se um inventário é deste tipo de menu
     * 
     * @param inventory O inventário a ser verificado
     * @return true se o inventário for deste menu, false caso contrário
     */
    boolean isThisMenu(Inventory inventory);
    
    /**
     * Obtém o título base deste menu
     * 
     * @return O título do menu
     */
    String getTitle();
}
package hplugins.anuncio.utils;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Classe utilitária para operações relacionadas a inventários
 * Fornece métodos compatíveis com diferentes versões do Minecraft
 */
public class InventoryUtils {
    
    /**
     * Obtém o título de um inventário de forma compatível com todas as versões
     * 
     * @param inventory O inventário
     * @return O título do inventário ou uma string vazia se não for possível obter
     */
    public static String getInventoryTitle(Inventory inventory) {
        try {
            
            if (inventory.getViewers().size() > 0) {
                HumanEntity viewer = inventory.getViewers().get(0);
                InventoryView view = viewer.getOpenInventory();
                
                
                if (VersionUtils.isVersionAbove(1, 14)) {
                    return view.getTitle();
                } else {
                    
                    try {
                        
                        return view.getTitle();
                    } catch (NoSuchMethodError e) {
                        
                        return getLegacyTitle(view);
                    }
                }
            }
        } catch (Exception e) {
            
        }
        
        return ""; 
    }
    
    /**
     * Obtém o título de um inventário usando métodos compatíveis com versões antigas
     * 
     * @param view A view do inventário
     * @return O título ou uma string vazia em caso de falha
     */
    private static String getLegacyTitle(InventoryView view) {
        try {
            
            java.lang.reflect.Method method = view.getClass().getMethod("getTitle");
            if (method != null) {
                return (String) method.invoke(view);
            }
        } catch (Exception e) {
            try {
                
                java.lang.reflect.Field field = view.getClass().getDeclaredField("title");
                field.setAccessible(true);
                return (String) field.get(view);
            } catch (Exception ex) {
                
            }
        }
        
        return "";
    }
    
    /**
     * Verifica se um inventário tem um título específico
     * 
     * @param inventory O inventário a verificar
     * @param title O título esperado
     * @return true se os títulos correspondem
     */
    public static boolean hasTitleMatching(Inventory inventory, String title) {
        String invTitle = getInventoryTitle(inventory);
        return invTitle.equals(title);
    }
    
    /**
     * Verifica se o título de um inventário começa com um prefixo específico
     * 
     * @param inventory O inventário a verificar
     * @param prefix O prefixo esperado
     * @return true se o título começa com o prefixo
     */
    public static boolean titleStartsWith(Inventory inventory, String prefix) {
        String invTitle = getInventoryTitle(inventory);
        return invTitle.startsWith(prefix);
    }
}
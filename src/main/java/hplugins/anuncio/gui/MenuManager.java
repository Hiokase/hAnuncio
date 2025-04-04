package hplugins.anuncio.gui;

import com.cryptomorin.xseries.XMaterial;
import hplugins.anuncio.hAnuncio;
import hplugins.anuncio.utils.GuiUtils;
import hplugins.anuncio.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Gerencia todos os menus GUI do plugin
 */
public class MenuManager {
    
    private final hAnuncio plugin;
    private final Map<String, Menu> registeredMenus = new HashMap<>();
    
    
    private static final XMaterial BORDER_MATERIAL = XMaterial.GRAY_STAINED_GLASS_PANE;
    private static final XMaterial SEPARATOR_MATERIAL = XMaterial.BLACK_STAINED_GLASS_PANE;
    private static final XMaterial HIGHLIGHT_MATERIAL = XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE;
    
    public MenuManager(hAnuncio plugin) {
        this.plugin = plugin;
        
        
        registerMenu("categories", new CategoryMenu(plugin));
        registerMenu("categoryEditor", new CategoryEditorMenu(plugin));
        
        
        registerMenu("announcement", new AnnouncementMenu(plugin));
        registerMenu("categorySelector", new CategorySelectorMenu(plugin));
    }
    
    /**
     * Registra um menu no sistema
     * 
     * @param name Nome único do menu
     * @param menu A instância do menu
     */
    public void registerMenu(String name, Menu menu) {
        registeredMenus.put(name.toLowerCase(), menu);
    }
    
    /**
     * Abre um menu para um jogador
     * 
     * @param player O jogador
     * @param menuName Nome do menu a ser aberto
     * @param args Argumentos adicionais para o menu
     */
    public void openMenu(Player player, String menuName, Object... args) {
        Menu menu = registeredMenus.get(menuName.toLowerCase());
        
        if (menu != null) {
            menu.open(player, args);
            
            
            if (plugin.getMenuListener() != null) {
                plugin.getMenuListener().registerMenuOpen(player, menuName.toLowerCase());
            }
        } else {
            player.sendMessage(MessageUtils.colorize("&cMenu não encontrado: " + menuName));
        }
    }
    
    /**
     * Obtém a lista de todos os menus registrados
     * 
     * @return Uma coleção contendo todos os menus
     */
    public Collection<Menu> getRegisteredMenus() {
        return registeredMenus.values();
    }
    
    /**
     * Obtém um menu pelo nome
     * 
     * @param name Nome do menu
     * @return O menu, ou null se não existir
     */
    public Menu getMenu(String name) {
        return registeredMenus.get(name.toLowerCase());
    }
    
    /**
     * Cria um inventário com o título colorido
     * 
     * @param title Título do inventário (pode conter códigos de cor)
     * @param size Tamanho do inventário (deve ser múltiplo de 9)
     * @return O inventário criado
     */
    public static Inventory createInventory(String title, int size) {
        return Bukkit.createInventory(null, size, MessageUtils.colorize(title));
    }
    
    /**
     * Cria um inventário com o título colorido e adiciona uma borda decorativa
     * 
     * @param title Título do inventário (pode conter códigos de cor)
     * @param size Tamanho do inventário (deve ser múltiplo de 9)
     * @param borderType Tipo de borda (0: sem borda, 1: completa, 2: só cantos, 3: superior e inferior)
     * @return O inventário criado com borda
     */
    public static Inventory createInventoryWithBorder(String title, int size, int borderType) {
        Inventory inventory = Bukkit.createInventory(null, size, MessageUtils.colorize(title));
        if (borderType > 0) {
            GuiUtils.createBorder(inventory, BORDER_MATERIAL, borderType);
        }
        return inventory;
    }
    
    /**
     * Cria um inventário com título colorido e design avançado
     * 
     * @param title Título do inventário
     * @param size Tamanho do inventário
     * @param style Estilo visual (1: borda padrão, 2: cantos destacados, 3: design alternado)
     * @return O inventário criado com design avançado
     */
    public static Inventory createStyledInventory(String title, int size, int style) {
        Inventory inventory = Bukkit.createInventory(null, size, MessageUtils.colorize(title));
        
        switch (style) {
            case 1: 
                GuiUtils.createBorder(inventory, BORDER_MATERIAL, 1);
                break;
                
            case 2: 
                GuiUtils.createBorder(inventory, BORDER_MATERIAL, 3);
                GuiUtils.fillSlots(inventory, HIGHLIGHT_MATERIAL, 0, 8, size - 9, size - 1);
                break;
                
            case 3: 
                
                GuiUtils.createColoredRow(inventory, 0, HIGHLIGHT_MATERIAL, BORDER_MATERIAL);
                GuiUtils.createColoredRow(inventory, size / 9 - 1, HIGHLIGHT_MATERIAL, BORDER_MATERIAL);
                
                
                for (int row = 1; row < size / 9 - 1; row++) {
                    GuiUtils.fillSlots(inventory, BORDER_MATERIAL, row * 9, row * 9 + 8);
                }
                break;
                
            default:
                
                break;
        }
        
        return inventory;
    }
    
    /**
     * Adiciona uma linha decorativa de separação no inventário
     * 
     * @param inventory O inventário
     * @param row A linha onde a separação será adicionada (0-5)
     */
    public static void addSeparator(Inventory inventory, int row) {
        int startSlot = row * 9;
        for (int i = 0; i < 9; i++) {
            inventory.setItem(startSlot + i, GuiUtils.createItem(SEPARATOR_MATERIAL, " "));
        }
    }
    
    /**
     * Cria uma moldura visual para destacar um item central
     * 
     * @param inventory O inventário
     * @param centerSlot O slot central
     * @param material Material da moldura
     */
    public static void createFrame(Inventory inventory, int centerSlot, XMaterial material) {
        int size = inventory.getSize();
        int row = centerSlot / 9;
        int col = centerSlot % 9;
        
        
        if (row > 0 && row < size / 9 - 1 && col > 0 && col < 8) {
            int[] frameSlots = {
                centerSlot - 10, centerSlot - 9, centerSlot - 8,
                centerSlot - 1,                  centerSlot + 1,
                centerSlot + 8,  centerSlot + 9, centerSlot + 10
            };
            
            for (int slot : frameSlots) {
                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, GuiUtils.createItem(material, " "));
                }
            }
        }
    }
    
    /**
     * Cria um item para o menu com nome e descrição coloridos
     * 
     * @param material Material do item
     * @param name Nome do item (pode conter códigos de cor)
     * @param lore Descrição do item (pode conter códigos de cor)
     * @return O ItemStack criado
     */
    public static ItemStack createMenuItem(XMaterial material, String name, String... lore) {
        return GuiUtils.createItem(material, name, lore);
    }
    
    /**
     * Cria um item para o menu com nome e descrição coloridos
     * 
     * @param material Material do item
     * @param name Nome do item (pode conter códigos de cor)
     * @param lore Lista de linhas da descrição (pode conter códigos de cor)
     * @return O ItemStack criado
     */
    public static ItemStack createMenuItem(XMaterial material, String name, List<String> lore) {
        return GuiUtils.createItem(material, name, lore);
    }
}
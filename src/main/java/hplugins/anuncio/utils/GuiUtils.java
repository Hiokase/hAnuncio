package hplugins.anuncio.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilitários para criação e manipulação de interfaces gráficas
 */
public class GuiUtils {

    /**
     * Cria um item para menu com nome e descrição coloridos
     *
     * @param material Material do item
     * @param name Nome do item (pode conter códigos de cor)
     * @param lore Descrição do item (pode conter códigos de cor)
     * @return O ItemStack criado
     */
    public static ItemStack createItem(XMaterial material, String name, String... lore) {
        ItemStack item = material.parseItem();
        if (item == null) {
            
            item = new ItemStack(Material.STONE);
        }
        
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.colorize(name));
        
        if (lore.length > 0) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(MessageUtils.colorize(line));
            }
            meta.setLore(coloredLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Cria um item para menu com nome e descrição coloridos
     *
     * @param material Material do item
     * @param name Nome do item (pode conter códigos de cor)
     * @param lore Lista de linhas da descrição (pode conter códigos de cor)
     * @return O ItemStack criado
     */
    public static ItemStack createItem(XMaterial material, String name, List<String> lore) {
        ItemStack item = material.parseItem();
        if (item == null) {
            
            item = new ItemStack(Material.STONE);
        }
        
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.colorize(name));
        
        if (!lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(MessageUtils.colorize(line));
            }
            meta.setLore(coloredLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Cria uma borda decorativa no inventário
     *
     * @param inventory O inventário
     * @param material Material da borda
     * @param pattern Tipo de padrão da borda (1: completa, 2: só cantos, 3: superior e inferior)
     */
    public static void createBorder(Inventory inventory, XMaterial material, int pattern) {
        int size = inventory.getSize();
        int rows = size / 9;
        ItemStack borderItem = createItem(material, " ");
        
        switch (pattern) {
            case 1: 
                
                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, borderItem);                 
                    inventory.setItem(size - 9 + i, borderItem);      
                }
                
                
                for (int i = 1; i < rows - 1; i++) {
                    inventory.setItem(i * 9, borderItem);             
                    inventory.setItem(i * 9 + 8, borderItem);         
                }
                break;
                
            case 2: 
                inventory.setItem(0, borderItem);                    
                inventory.setItem(8, borderItem);                    
                inventory.setItem(size - 9, borderItem);             
                inventory.setItem(size - 1, borderItem);             
                break;
                
            case 3: 
                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, borderItem);                
                    inventory.setItem(size - 9 + i, borderItem);     
                }
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Preenche slots especificados com um item decorativo
     *
     * @param inventory O inventário
     * @param material Material do item
     * @param slots Array de slots para preencher
     */
    public static void fillSlots(Inventory inventory, XMaterial material, int... slots) {
        ItemStack item = createItem(material, " ");
        for (int slot : slots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, item);
            }
        }
    }
    
    /**
     * Adiciona decorações coloridas alternadas em uma linha específica
     *
     * @param inventory O inventário
     * @param row Número da linha (começando em 0)
     * @param material1 Primeiro material
     * @param material2 Segundo material
     */
    public static void createColoredRow(Inventory inventory, int row, XMaterial material1, XMaterial material2) {
        int startSlot = row * 9;
        for (int i = 0; i < 9; i++) {
            XMaterial material = (i % 2 == 0) ? material1 : material2;
            inventory.setItem(startSlot + i, createItem(material, " "));
        }
    }
    
    /**
     * Cria um padrão de tabuleiro no fundo do inventário
     * 
     * @param inventory O inventário
     * @param startRow Linha inicial (começando em 0)
     * @param endRow Linha final (começando em 0)
     * @param mat1 Primeiro material
     * @param mat2 Segundo material
     */
    public static void createCheckerboardPattern(Inventory inventory, int startRow, int endRow, XMaterial mat1, XMaterial mat2) {
        ItemStack item1 = createItem(mat1, " ");
        ItemStack item2 = createItem(mat2, " ");
        
        for (int row = startRow; row <= endRow; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = row * 9 + col;
                if ((row + col) % 2 == 0) {
                    inventory.setItem(slot, item1);
                } else {
                    inventory.setItem(slot, item2);
                }
            }
        }
    }
    
    /**
     * Obtém a cor para um item baseada em uma posição (para efeito visual)
     * 
     * @param position Posição na lista
     * @return Item material/cor correspondente
     */
    public static XMaterial getColorForPosition(int position) {
        XMaterial[] colors = {
            XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE,
            XMaterial.LIME_STAINED_GLASS_PANE,
            XMaterial.YELLOW_STAINED_GLASS_PANE,
            XMaterial.PINK_STAINED_GLASS_PANE,
            XMaterial.PURPLE_STAINED_GLASS_PANE,
            XMaterial.ORANGE_STAINED_GLASS_PANE,
            XMaterial.CYAN_STAINED_GLASS_PANE
        };
        
        return colors[position % colors.length];
    }
}
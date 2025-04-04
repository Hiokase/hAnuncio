package hplugins.anuncio.gui;

import com.cryptomorin.xseries.XMaterial;
import hplugins.anuncio.hAnuncio;
import hplugins.anuncio.models.Category;
import hplugins.anuncio.utils.GuiUtils;
import hplugins.anuncio.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Menu para listar e gerenciar categorias
 */
public class CategoryMenu implements Menu {

    private static final int ROWS = 6;
    private static final int INVENTORY_SIZE = ROWS * 9;
    private static final String TITLE = "&8✦ &bGerenciador de Categorias &8✦";
    
    
    private static final int BTN_CREATE_SLOT = INVENTORY_SIZE - 5;
    private static final int BTN_CLOSE_SLOT = INVENTORY_SIZE - 1;
    
    
    private static final XMaterial CATEGORY_MATERIAL = XMaterial.NAME_TAG;
    private static final XMaterial CREATE_BUTTON_MATERIAL = XMaterial.EMERALD;
    private static final XMaterial CLOSE_BUTTON_MATERIAL = XMaterial.BARRIER;
    private static final XMaterial INFO_MATERIAL = XMaterial.BOOK;
    
    private final hAnuncio plugin;
    
    public CategoryMenu(hAnuncio plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void open(Player player, Object... args) {
        player.openInventory(getInventory(player, args));
    }
    
    @Override
    public boolean handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        
        
        if (slot == BTN_CREATE_SLOT) {
            player.closeInventory();
            player.sendMessage(MessageUtils.colorize("&a&l✏ &aDigite o nome da nova categoria:"));
            plugin.getPlayerManager().startCategoryCreation(player);
            return true;
        }
        
        
        if (slot == BTN_CLOSE_SLOT) {
            player.closeInventory();
            return true;
        }
        
        
        if (slot >= 10 && slot <= INVENTORY_SIZE - 10) {
            
            if (slot % 9 != 0 && slot % 9 != 8) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && !clickedItem.getType().name().contains("PANE")) {
                    
                    String categoryName = MessageUtils.removeColor(clickedItem.getItemMeta().getDisplayName());
                    Category category = plugin.getCategoryManager().getCategoryByName(categoryName);
                    
                    if (category != null) {
                        
                        plugin.getMenuManager().openMenu(player, "categoryEditor", category);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public Inventory getInventory(Player player, Object... args) {
        
        Inventory inventory = MenuManager.createStyledInventory(TITLE, INVENTORY_SIZE, 2);
        
        
        ItemStack infoItem = MenuManager.createMenuItem(
                INFO_MATERIAL,
                "&e&lCategorias de Anúncios",
                "&7Total de categorias: &f" + plugin.getCategoryManager().getCategoryCount(),
                "&7Clique em uma categoria para editá-la.",
                "",
                "&e&lDica: &7As categorias permitem organizar",
                "&7os anúncios por tipo ou finalidade."
        );
        inventory.setItem(4, infoItem);
        
        
        MenuManager.addSeparator(inventory, 1);
        
        
        List<Category> categories = new ArrayList<>(plugin.getCategoryManager().getAllCategories());
        
        categories.sort(Comparator.comparing(Category::getName));
        
        
        int row = 2;
        int col = 1;
        
        for (Category category : categories) {
            
            int slot = row * 9 + col;
            
            
            if (col >= 7) {
                col = 1;
                row++;
                
                
                if (row >= ROWS - 1) {
                    break;
                }
            }
            
            
            XMaterial itemMaterial = CATEGORY_MATERIAL;
            
            List<String> lore = new ArrayList<>();
            lore.add("&8ID: " + category.getId());
            lore.add("");
            lore.add("&7Prefixo: " + category.getPrefix());
            lore.add("&7Cor: " + category.getColor() + "Exemplo");
            lore.add("&7Permissão: &f" + category.getPermission());
            lore.add("");
            lore.add("&a▶ Clique para editar esta categoria");
            
            
            XMaterial frameMaterial = GuiUtils.getColorForPosition(col);
            MenuManager.createFrame(inventory, slot, frameMaterial);
            
            ItemStack categoryItem = MenuManager.createMenuItem(
                    itemMaterial, 
                    "&b&l" + category.getName(), 
                    lore
            );
            
            inventory.setItem(slot, categoryItem);
            
            
            col += 2;
        }
        
        
        MenuManager.addSeparator(inventory, ROWS - 2);
        
        
        if (player.hasPermission(plugin.getConfigManager().getPermissions().getString("admin.category.create", "hanuncio.admin.category.create"))) {
            ItemStack createButton = MenuManager.createMenuItem(
                    CREATE_BUTTON_MATERIAL,
                    "&a&l+ Criar Nova Categoria",
                    "&7Clique para criar uma nova categoria",
                    "&7para os anúncios do servidor."
            );
            inventory.setItem(BTN_CREATE_SLOT, createButton);
        }
        
        
        ItemStack closeButton = MenuManager.createMenuItem(
                CLOSE_BUTTON_MATERIAL,
                "&c&lFechar",
                "&7Clique para fechar o menu"
        );
        inventory.setItem(BTN_CLOSE_SLOT, closeButton);
        
        return inventory;
    }
    
    @Override
    public boolean isThisMenu(Inventory inventory) {
        try {
            return hplugins.anuncio.utils.InventoryUtils.hasTitleMatching(inventory, MessageUtils.colorize(TITLE));
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getTitle() {
        return TITLE;
    }
}
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
import java.util.List;

/**
 * Menu para selecionar categorias para anúncios
 */
public class CategorySelectorMenu implements Menu {

    private static final int ROWS = 4;
    private static final int INVENTORY_SIZE = ROWS * 9;
    private static final String TITLE = "&8✦ &eSelecionar Categoria &8✦";
    
    private static final int BACK_SLOT = INVENTORY_SIZE - 5;
    private static final int CLOSE_SLOT = INVENTORY_SIZE - 1;
    
    
    private static final XMaterial INFO_MATERIAL = XMaterial.WRITABLE_BOOK;
    private static final XMaterial BACK_MATERIAL = XMaterial.ARROW;
    private static final XMaterial CLOSE_MATERIAL = XMaterial.BARRIER;
    
    private final hAnuncio plugin;
    
    public CategorySelectorMenu(hAnuncio plugin) {
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
        
        
        if (slot == BACK_SLOT) {
            plugin.getMenuManager().openMenu(player, "announcement");
            return true;
        }
        
        
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return true;
        }
        
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && !clickedItem.getType().name().contains("PANE")) {
            
            String displayName = clickedItem.getItemMeta().getDisplayName();
            String categoryName = MessageUtils.removeColor(displayName);
            
            
            if (categoryName.startsWith("(")) {
                categoryName = categoryName.substring(categoryName.indexOf(") ") + 2);
            }
            
            Category category = plugin.getCategoryManager().getCategoryByName(categoryName);
            
            if (category != null) {
                
                if (!player.hasPermission(category.getPermission()) && 
                    !player.hasPermission("hanuncio.category.*") && 
                    !player.isOp()) {
                    player.sendMessage(MessageUtils.colorize("&c&l⚠ &cVocê não tem permissão para usar esta categoria!"));
                    player.closeInventory();
                    return true;
                }
                
                
                plugin.getAnnouncementManager().setDraftCategory(player.getUniqueId(), category);
                
                
                plugin.getMenuManager().openMenu(player, "announcement");
                
                
                player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(plugin.getConfigManager().getConfig().getString(
                        "sounds.select", "ENTITY_EXPERIENCE_ORB_PICKUP")), 1f, 1f);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Inventory getInventory(Player player, Object... args) {
        
        Inventory inventory = MenuManager.createStyledInventory(TITLE, INVENTORY_SIZE, 2);
        
        
        List<String> infoLore = new ArrayList<>();
        infoLore.add("&8Selecionar Categoria");
        infoLore.add("");
        infoLore.add("&7Escolha a categoria mais adequada");
        infoLore.add("&7para o seu anúncio.");
        infoLore.add("");
        infoLore.add("&e&lDica: &7Cada categoria tem um custo");
        infoLore.add("&7e propósito diferentes.");
        
        ItemStack infoItem = MenuManager.createMenuItem(
                INFO_MATERIAL,
                "&e&lSelecione uma Categoria",
                infoLore
        );
        inventory.setItem(4, infoItem);
        
        
        MenuManager.addSeparator(inventory, 1);
        
        
        List<Category> availableCategories = plugin.getCategoryManager().getAllCategories();
        
        
        int[] categorySlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25
        };
        
        int slotIndex = 0;
        for (Category category : availableCategories) {
            if (slotIndex >= categorySlots.length) break;
            
            int slot = categorySlots[slotIndex++];
            boolean hasPermission = player.hasPermission(category.getPermission()) || 
                                   player.hasPermission("hanuncio.category.*") || 
                                   player.isOp();
            
            List<String> lore = new ArrayList<>();
            lore.add("&8ID: " + category.getId());
            lore.add("");
            lore.add("&7Prefixo: " + category.getPrefix());
            lore.add("&7Cor: " + category.getColor() + "Exemplo");
            lore.add("");
            
            
            double basePrice = plugin.getEconomyManager().getBasePrice();
            double multiplier = plugin.getConfigManager().getCategorias().getDouble("multipliers." + category.getId(), 1.0);
            double price = basePrice * multiplier;
            
            lore.add("&7Custo: &f" + price + " " + plugin.getEconomyManager().getCurrencyName());
            
            if (hasPermission) {
                lore.add("");
                lore.add("&a✓ Você tem permissão para usar");
                lore.add("&a✓ Clique para selecionar");
            } else {
                lore.add("");
                lore.add("&c✗ Você não tem permissão para usar");
                lore.add("&c✗ Permissão necessária: &f" + category.getPermission());
            }
            
            
            XMaterial material = hasPermission ? XMaterial.NAME_TAG : XMaterial.BARRIER;
            String displayName = hasPermission ? 
                    "&a" + category.getName() : 
                    "&c(Bloqueado) &7" + category.getName();
            
            ItemStack categoryItem = MenuManager.createMenuItem(material, displayName, lore);
            inventory.setItem(slot, categoryItem);
            
            
            if (hasPermission) {
                XMaterial frameMaterial = GuiUtils.getColorForPosition(slotIndex);
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int frameSlot = slot + (i * 9) + j;
                        if (frameSlot >= 0 && frameSlot < INVENTORY_SIZE && 
                            frameSlot != slot && 
                            inventory.getItem(frameSlot) == null) {
                            inventory.setItem(frameSlot, GuiUtils.createItem(frameMaterial, " "));
                        }
                    }
                }
            }
        }
        
        
        ItemStack backButton = MenuManager.createMenuItem(
                BACK_MATERIAL,
                "&a&lVoltar",
                "&7Voltar para o menu de anúncios"
        );
        inventory.setItem(BACK_SLOT, backButton);
        
        
        ItemStack closeButton = MenuManager.createMenuItem(
                CLOSE_MATERIAL,
                "&c&lFechar",
                "&7Fechar este menu"
        );
        inventory.setItem(CLOSE_SLOT, closeButton);
        
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
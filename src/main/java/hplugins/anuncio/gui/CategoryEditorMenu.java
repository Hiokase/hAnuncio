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
import java.util.Arrays;
import java.util.List;

/**
 * Menu para editar uma categoria específica
 */
public class CategoryEditorMenu implements Menu {

    private static final int ROWS = 5;
    private static final int INVENTORY_SIZE = ROWS * 9;
    private static final String TITLE = "&8✦ &bEditar &f";
    
    
    private static final int NAME_SLOT = 11;
    private static final int PREFIX_SLOT = 13;
    private static final int COLOR_SLOT = 15;
    private static final int DELETE_SLOT = 31;
    private static final int BACK_SLOT = INVENTORY_SIZE - 5;
    private static final int CLOSE_SLOT = INVENTORY_SIZE - 1;
    
    
    private static final XMaterial INFO_MATERIAL = XMaterial.BOOKSHELF;
    private static final XMaterial NAME_MATERIAL = XMaterial.NAME_TAG;
    private static final XMaterial PREFIX_MATERIAL = XMaterial.WRITABLE_BOOK;
    private static final XMaterial COLOR_MATERIAL = XMaterial.LIME_DYE;
    private static final XMaterial DELETE_MATERIAL = XMaterial.REDSTONE_BLOCK;
    private static final XMaterial BACK_MATERIAL = XMaterial.ARROW;
    private static final XMaterial CLOSE_MATERIAL = XMaterial.BARRIER;
    
    
    private static final XMaterial PREVIEW_FRAME_MATERIAL = XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE;
    private static final XMaterial WARNING_MATERIAL = XMaterial.ORANGE_STAINED_GLASS_PANE;
    
    private final hAnuncio plugin;
    
    public CategoryEditorMenu(hAnuncio plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void open(Player player, Object... args) {
        if (args.length < 1 || !(args[0] instanceof Category)) {
            player.sendMessage(MessageUtils.colorize("&cErro ao abrir o editor de categoria!"));
            return;
        }
        
        Category category = (Category) args[0];
        player.openInventory(getInventory(player, category));
    }
    
    @Override
    public boolean handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        
        
        String fullTitle = event.getView().getTitle();
        String categoryName = MessageUtils.removeColor(fullTitle.substring(MessageUtils.colorize(TITLE).length()));
        
        Category category = plugin.getCategoryManager().getCategoryByName(categoryName);
        
        if (category == null) {
            player.closeInventory();
            player.sendMessage(MessageUtils.colorize("&c⚠ Categoria não encontrada!"));
            return true;
        }
        
        switch (slot) {
            
            case NAME_SLOT:
                player.closeInventory();
                player.sendMessage(MessageUtils.colorize("&a&l✏ &aDigite o novo nome para a categoria:"));
                player.sendMessage(MessageUtils.colorize("&7Nome atual: &f" + category.getName()));
                plugin.getPlayerManager().startCategoryEdit(player, category, "name");
                break;
                
            
            case PREFIX_SLOT:
                player.closeInventory();
                player.sendMessage(MessageUtils.colorize("&a&l✏ &aDigite o novo prefixo para a categoria:"));
                player.sendMessage(MessageUtils.colorize("&7Prefixo atual: &f" + category.getPrefix()));
                plugin.getPlayerManager().startCategoryEdit(player, category, "prefix");
                break;
                
            
            case COLOR_SLOT:
                player.closeInventory();
                player.sendMessage(MessageUtils.colorize("&a&l✏ &aDigite o novo código de cor para a categoria:"));
                player.sendMessage(MessageUtils.colorize("&7Cor atual: " + category.getColor() + "Exemplo"));
                player.sendMessage(MessageUtils.colorize("&7Códigos de cor disponíveis: &0&l⬛ &8&l⬛ &7&l⬛ &f&l⬛ &c&l⬛ &e&l⬛ &a&l⬛ &b&l⬛ &9&l⬛ &d&l⬛ &5&l⬛ &6&l⬛"));
                player.sendMessage(MessageUtils.colorize("&7(Exemplo: &a&o&6 &7para cor dourada)"));
                plugin.getPlayerManager().startCategoryEdit(player, category, "color");
                break;
                
            
            case DELETE_SLOT:
                if (player.hasPermission(plugin.getConfigManager().getPermissions().getString("admin.category.delete", "hanuncio.admin.category.delete"))) {
                    player.closeInventory();
                    player.sendMessage(MessageUtils.colorize("&c&l⚠ &cVocê tem certeza que deseja deletar a categoria &f" + category.getName() + "&c?"));
                    player.sendMessage(MessageUtils.colorize("&cEsta ação não pode ser desfeita!"));
                    player.sendMessage(MessageUtils.colorize("&cDigite &f/hac confirmardeletar " + category.getName() + " &cpara confirmar!"));
                }
                break;
                
            
            case BACK_SLOT:
                plugin.getMenuManager().openMenu(player, "categories");
                break;
                
            
            case CLOSE_SLOT:
                player.closeInventory();
                break;
        }
        
        return true;
    }
    
    @Override
    public Inventory getInventory(Player player, Object... args) {
        if (args.length < 1 || !(args[0] instanceof Category)) {
            return MenuManager.createInventory("&c&lErro ao abrir menu", 9);
        }
        
        Category category = (Category) args[0];
        Inventory inventory = MenuManager.createStyledInventory(TITLE + category.getName(), INVENTORY_SIZE, 3);
        
        
        String previewMessage = MessageUtils.colorize(category.getPrefix() + " " + category.getColor() + "Este é um exemplo de anúncio");
        
        List<String> previewLore = new ArrayList<>();
        previewLore.add("&8Visualização de anúncio");
        previewLore.add("");
        previewLore.add("&7Veja como o anúncio aparecerá para");
        previewLore.add("&7os jogadores no chat:");
        previewLore.add("");
        previewLore.add("&f" + previewMessage);
        previewLore.add("");
        previewLore.add("&e&lDica: &7Escolha cores agradáveis para seus");
        previewLore.add("&7jogadores lerem facilmente.");
        
        ItemStack previewItem = MenuManager.createMenuItem(
                INFO_MATERIAL, 
                "&e&lVisualização da Categoria", 
                previewLore
        );
        
        inventory.setItem(4, previewItem);
        
        MenuManager.createFrame(inventory, 4, PREVIEW_FRAME_MATERIAL);
        
        
        List<String> nameLore = new ArrayList<>();
        nameLore.add("&8ID interno: " + category.getId());
        nameLore.add("");
        nameLore.add("&7Nome atual: &f" + category.getName());
        nameLore.add("");
        nameLore.add("&7O nome é usado para identificar");
        nameLore.add("&7a categoria nos menus e comandos.");
        nameLore.add("");
        nameLore.add("&a▶ Clique para alterar o nome");
        
        ItemStack nameItem = MenuManager.createMenuItem(NAME_MATERIAL, "&e&lAlterar Nome", nameLore);
        inventory.setItem(NAME_SLOT, nameItem);
        
        
        List<String> prefixLore = new ArrayList<>();
        prefixLore.add("");
        prefixLore.add("&7Prefixo atual: &f" + category.getPrefix());
        prefixLore.add("");
        prefixLore.add("&7O prefixo é mostrado antes da");
        prefixLore.add("&7mensagem no chat. Pode conter");
        prefixLore.add("&7códigos de cor e formatação.");
        prefixLore.add("");
        prefixLore.add("&a▶ Clique para alterar o prefixo");
        
        ItemStack prefixItem = MenuManager.createMenuItem(PREFIX_MATERIAL, "&e&lAlterar Prefixo", prefixLore);
        inventory.setItem(PREFIX_SLOT, prefixItem);
        
        
        List<String> colorLore = new ArrayList<>();
        colorLore.add("");
        colorLore.add("&7Cor atual: " + category.getColor() + "Exemplo");
        colorLore.add("");
        colorLore.add("&7A cor é aplicada ao texto do anúncio.");
        colorLore.add("&7Cores disponíveis:");
        colorLore.add("&0&l⬛ &f- &0Preto &8(&0&0&7)");
        colorLore.add("&8&l⬛ &f- &8Cinza escuro &8(&0&8&7)");
        colorLore.add("&7&l⬛ &f- &7Cinza &8(&0&7&7)");
        colorLore.add("&f&l⬛ &f- &fBranco &8(&0&f&7)");
        colorLore.add("&c&l⬛ &f- &cVermelho &8(&0&c&7)");
        colorLore.add("&e&l⬛ &f- &eAmarelo &8(&0&e&7)");
        colorLore.add("");
        colorLore.add("&a▶ Clique para alterar a cor");
        
        ItemStack colorItem = MenuManager.createMenuItem(COLOR_MATERIAL, "&e&lAlterar Cor", colorLore);
        inventory.setItem(COLOR_SLOT, colorItem);
        
        MenuManager.addSeparator(inventory, 3);
        
        
        if (player.hasPermission(plugin.getConfigManager().getPermissions().getString("admin.category.delete", "hanuncio.admin.category.delete"))) {
            List<String> deleteLore = new ArrayList<>();
            deleteLore.add("");
            deleteLore.add("&7Remove permanentemente esta categoria");
            deleteLore.add("&7do sistema de anúncios.");
            deleteLore.add("");
            deleteLore.add("&c&l⚠ CUIDADO:");
            deleteLore.add("&cEsta ação não pode ser desfeita!");
            deleteLore.add("");
            deleteLore.add("&c▶ Clique para deletar");
            
            ItemStack deleteItem = MenuManager.createMenuItem(DELETE_MATERIAL, "&c&lDeletar Categoria", deleteLore);
            inventory.setItem(DELETE_SLOT, deleteItem);
            
            
            for (int i = 0; i < 9; i++) {
                if (i != 4) { 
                    inventory.setItem(27 + i, GuiUtils.createItem(WARNING_MATERIAL, " "));
                }
            }
        }
        
        
        ItemStack backButton = MenuManager.createMenuItem(
                BACK_MATERIAL,
                "&a&lVoltar",
                "&7Voltar para o menu de categorias"
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
            return hplugins.anuncio.utils.InventoryUtils.titleStartsWith(inventory, MessageUtils.colorize(TITLE));
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getTitle() {
        return TITLE;
    }
}
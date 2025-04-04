package hplugins.anuncio.gui;

import com.cryptomorin.xseries.XMaterial;
import hplugins.anuncio.hAnuncio;
import hplugins.anuncio.models.Category;
import hplugins.anuncio.utils.GuiUtils;
import hplugins.anuncio.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu principal para criação de anúncios
 */
public class AnnouncementMenu implements Menu {
    
    private static final int ROWS = 6;
    private static final int INVENTORY_SIZE = ROWS * 9;
    private static final String TITLE = "&8✦ &a&lCriar Anúncio &8✦";
    
    
    private static final int WELCOME_SLOT = 4;
    private static final int CATEGORY_SLOT = 13;
    private static final int MESSAGE_SLOT = 22;
    private static final int PREVIEW_SLOT = 31;
    private static final int SOUND_SLOT = 29;
    private static final int EFFECTS_SLOT = 33;
    private static final int CONFIRM_SLOT = 49;
    private static final int CANCEL_SLOT = 45;
    private static final int INFO_SLOT = 53;
    
    
    private static final XMaterial WELCOME_MATERIAL = XMaterial.WRITABLE_BOOK;
    private static final XMaterial CATEGORY_MATERIAL = XMaterial.NAME_TAG;
    private static final XMaterial MESSAGE_MATERIAL = XMaterial.PAPER;
    private static final XMaterial PREVIEW_MATERIAL = XMaterial.ENCHANTED_BOOK;
    private static final XMaterial SOUND_MATERIAL = XMaterial.NOTE_BLOCK;
    private static final XMaterial EFFECTS_MATERIAL = XMaterial.FIREWORK_ROCKET;
    private static final XMaterial CONFIRM_MATERIAL = XMaterial.EMERALD;
    private static final XMaterial CANCEL_MATERIAL = XMaterial.BARRIER;
    private static final XMaterial INFO_MATERIAL = XMaterial.BOOK;
    
    
    private static final XMaterial TOP_DECORATION = XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE;
    private static final XMaterial PROGRESS_DECORATION = XMaterial.LIME_STAINED_GLASS_PANE;
    private static final XMaterial PENDING_DECORATION = XMaterial.GRAY_STAINED_GLASS_PANE;
    
    private final hAnuncio plugin;
    
    public AnnouncementMenu(hAnuncio plugin) {
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
        
        
        String message = plugin.getAnnouncementManager().getDraftMessage(player.getUniqueId());
        Category category = plugin.getAnnouncementManager().getDraftCategory(player.getUniqueId());
        boolean hasSound = plugin.getAnnouncementManager().getDraftHasSound(player.getUniqueId());
        boolean hasEffects = plugin.getAnnouncementManager().getDraftHasEffects(player.getUniqueId());
        
        switch (slot) {
            
            case CATEGORY_SLOT:
                
                plugin.getMenuManager().openMenu(player, "categorySelector");
                break;
                
            
            case MESSAGE_SLOT:
                if (category == null) {
                    player.sendMessage(MessageUtils.colorize("&c&l⚠ &cVocê precisa selecionar uma categoria primeiro!"));
                    return true;
                }
                
                player.closeInventory();
                player.sendMessage(MessageUtils.colorize("&a&l✏ &aDigite sua mensagem de anúncio:"));
                plugin.getAnnouncementManager().startMessageDraft(player);
                break;
                
            
            case PREVIEW_SLOT:
                if (category == null || message == null || message.isEmpty()) {
                    player.sendMessage(MessageUtils.colorize("&c&l⚠ &cVocê precisa selecionar uma categoria e escrever uma mensagem primeiro!"));
                    return true;
                }
                
                
                player.sendMessage(MessageUtils.colorize("&8&m------------------------------------"));
                player.sendMessage(MessageUtils.colorize("&e&lPrévia do anúncio:"));
                String fullMessage = category.getPrefix() + " " + category.getColor() + message;
                player.sendMessage(MessageUtils.colorize(fullMessage));
                player.sendMessage(MessageUtils.colorize("&8&m------------------------------------"));
                break;
                
            
            case SOUND_SLOT:
                plugin.getAnnouncementManager().setDraftHasSound(player.getUniqueId(), !hasSound);
                
                
                player.openInventory(getInventory(player));
                
                
                if (!hasSound) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(plugin.getConfigManager().getConfig().getString(
                            "announcement.sound", "ENTITY_EXPERIENCE_ORB_PICKUP")), 1f, 1f);
                }
                break;
                
            
            case EFFECTS_SLOT:
                plugin.getAnnouncementManager().setDraftHasEffects(player.getUniqueId(), !hasEffects);
                
                
                player.openInventory(getInventory(player));
                break;
                
            
            case CONFIRM_SLOT:
                if (category == null || message == null || message.isEmpty()) {
                    player.sendMessage(MessageUtils.colorize("&c&l⚠ &cVocê precisa preencher todos os campos obrigatórios!"));
                    return true;
                }
                
                
                double price = plugin.getEconomyManager().getAnnouncementPrice(player, category);
                if (!plugin.getEconomyManager().hasMoney(player, price)) {
                    player.closeInventory();
                    player.sendMessage(MessageUtils.colorize("&c&l⚠ &cVocê não tem dinheiro suficiente para enviar este anúncio!"));
                    player.sendMessage(MessageUtils.colorize("&cCusto: &f" + price + " " + plugin.getEconomyManager().getCurrencyName()));
                    return true;
                }
                
                
                boolean success = plugin.getAnnouncementManager().createAnnouncement(player, category, message, hasSound, hasEffects);
                
                if (success) {
                    
                    plugin.getEconomyManager().takeMoney(player, price);
                    
                    
                    player.closeInventory();
                    player.sendMessage(MessageUtils.colorize("&a&l✓ &aAnúncio enviado com sucesso!"));
                    
                    
                    plugin.getAnnouncementManager().clearDraft(player.getUniqueId());
                    
                    
                    player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(plugin.getConfigManager().getConfig().getString(
                            "sounds.success", "ENTITY_PLAYER_LEVELUP")), 1f, 1f);
                } else {
                    player.sendMessage(MessageUtils.colorize("&c&l⚠ &cOcorreu um erro ao enviar seu anúncio. Tente novamente."));
                }
                break;
                
            
            case CANCEL_SLOT:
                player.closeInventory();
                
                if (category != null || (message != null && !message.isEmpty())) {
                    player.sendMessage(MessageUtils.colorize("&e&l? &eDeseja salvar este rascunho para uso posterior?"));
                    player.sendMessage(MessageUtils.colorize("&7Digite &a/anunciar salvar &7para salvar ou &c/anunciar limpar &7para descartar."));
                }
                break;
                
            
            case INFO_SLOT:
                player.closeInventory();
                player.sendMessage(MessageUtils.colorize("&8&m------------------------------------"));
                player.sendMessage(MessageUtils.colorize("&e&lSobre o Sistema de Anúncios"));
                player.sendMessage(MessageUtils.colorize("&7• Cada categoria tem um custo diferente."));
                player.sendMessage(MessageUtils.colorize("&7• Sons e efeitos visuais podem ter custos adicionais."));
                player.sendMessage(MessageUtils.colorize("&7• Seu anúncio será enviado para todos os jogadores online."));
                player.sendMessage(MessageUtils.colorize("&7• Há um cooldown entre anúncios para evitar spam."));
                player.sendMessage(MessageUtils.colorize("&8&m------------------------------------"));
                break;
        }
        
        return true;
    }
    
    @Override
    public Inventory getInventory(Player player, Object... args) {
        
        Inventory inventory = MenuManager.createStyledInventory(TITLE, INVENTORY_SIZE, 2);
        
        
        String message = plugin.getAnnouncementManager().getDraftMessage(player.getUniqueId());
        Category category = plugin.getAnnouncementManager().getDraftCategory(player.getUniqueId());
        boolean hasSound = plugin.getAnnouncementManager().getDraftHasSound(player.getUniqueId());
        boolean hasEffects = plugin.getAnnouncementManager().getDraftHasEffects(player.getUniqueId());
        
        
        int progress = 0;
        if (category != null) progress++;
        if (message != null && !message.isEmpty()) progress++;
        
        
        List<String> welcomeLore = new ArrayList<>();
        welcomeLore.add("&8Sistema de Anúncios");
        welcomeLore.add("");
        welcomeLore.add("&7Bem-vindo ao sistema de anúncios!");
        welcomeLore.add("&7Preencha os campos abaixo para");
        welcomeLore.add("&7criar e enviar seu anúncio.");
        welcomeLore.add("");
        welcomeLore.add("&e&lDica: &7Siga a ordem indicada pelos");
        welcomeLore.add("&7números para completar seu anúncio.");
        
        ItemStack welcomeItem = MenuManager.createMenuItem(
                WELCOME_MATERIAL,
                "&a&lCriar Novo Anúncio",
                welcomeLore
        );
        inventory.setItem(WELCOME_SLOT, welcomeItem);
        
        
        List<String> categoryLore = new ArrayList<>();
        categoryLore.add("");
        if (category == null) {
            categoryLore.add("&7Escolha a categoria mais adequada");
            categoryLore.add("&7para seu anúncio.");
            categoryLore.add("");
            categoryLore.add("&c✗ Nenhuma categoria selecionada");
            categoryLore.add("");
            categoryLore.add("&e① &7Clique para selecionar uma categoria");
        } else {
            categoryLore.add("&7Categoria: &f" + category.getName());
            categoryLore.add("&7Prefixo: &f" + category.getPrefix());
            categoryLore.add("&7Cor: " + category.getColor() + "Exemplo");
            categoryLore.add("");
            categoryLore.add("&a✓ Categoria selecionada");
            categoryLore.add("");
            categoryLore.add("&7Clique para alterar a categoria");
        }
        
        ItemStack categoryItem = MenuManager.createMenuItem(
                CATEGORY_MATERIAL,
                "&e&l① Selecionar Categoria",
                categoryLore
        );
        inventory.setItem(CATEGORY_SLOT, categoryItem);
        
        
        XMaterial decorMaterial = (category != null) ? PROGRESS_DECORATION : PENDING_DECORATION;
        for (int i = 0; i < 3; i++) {
            inventory.setItem(CATEGORY_SLOT + 1 + i, GuiUtils.createItem(decorMaterial, " "));
        }
        
        
        List<String> messageLore = new ArrayList<>();
        messageLore.add("");
        if (message == null || message.isEmpty()) {
            messageLore.add("&7Escreva o texto do seu anúncio.");
            messageLore.add("&7Seja claro e objetivo.");
            messageLore.add("");
            messageLore.add("&c✗ Nenhuma mensagem escrita");
            messageLore.add("");
            messageLore.add("&e② &7Clique para escrever sua mensagem");
        } else {
            messageLore.add("&7Sua mensagem:");
            messageLore.add("&f\"" + message + "\"");
            messageLore.add("");
            messageLore.add("&a✓ Mensagem escrita");
            messageLore.add("");
            messageLore.add("&7Clique para editar sua mensagem");
        }
        
        ItemStack messageItem = MenuManager.createMenuItem(
                MESSAGE_MATERIAL,
                "&e&l② Escrever Mensagem",
                messageLore
        );
        inventory.setItem(MESSAGE_SLOT, messageItem);
        
        
        decorMaterial = (message != null && !message.isEmpty()) ? PROGRESS_DECORATION : PENDING_DECORATION;
        for (int i = 0; i < 3; i++) {
            inventory.setItem(MESSAGE_SLOT + 1 + i, GuiUtils.createItem(decorMaterial, " "));
        }
        
        
        List<String> previewLore = new ArrayList<>();
        previewLore.add("");
        if (category == null || message == null || message.isEmpty()) {
            previewLore.add("&7Veja como seu anúncio aparecerá");
            previewLore.add("&7para os outros jogadores.");
            previewLore.add("");
            previewLore.add("&c✗ Preencha os campos anteriores");
            previewLore.add("&cpara visualizar seu anúncio.");
        } else {
            previewLore.add("&7Veja como seu anúncio aparecerá:");
            previewLore.add("");
            String preview = category.getPrefix() + " " + category.getColor() + message;
            previewLore.add("&f" + preview);
            previewLore.add("");
            previewLore.add("&a✓ Anúncio pronto para visualização");
            previewLore.add("");
            previewLore.add("&7Clique para ver no chat");
        }
        
        ItemStack previewItem = MenuManager.createMenuItem(
                PREVIEW_MATERIAL,
                "&e&l③ Pré-visualizar Anúncio",
                previewLore
        );
        inventory.setItem(PREVIEW_SLOT, previewItem);
        
        
        List<String> soundLore = new ArrayList<>();
        soundLore.add("");
        soundLore.add("&7Adicione um som quando seu");
        soundLore.add("&7anúncio for exibido no chat.");
        soundLore.add("");
        
        if (hasSound) {
            soundLore.add("&a✓ Som ativado");
            soundLore.add("");
            soundLore.add("&7Um som será reproduzido para todos");
            soundLore.add("&7quando seu anúncio for enviado.");
            soundLore.add("");
            soundLore.add("&7Custo adicional: &f+" + plugin.getConfigManager().getConfig().getDouble("costs.sound", 50.0) 
                    + " " + plugin.getEconomyManager().getCurrencyName());
            soundLore.add("");
            soundLore.add("&7Clique para desativar");
        } else {
            soundLore.add("&c✗ Som desativado");
            soundLore.add("");
            soundLore.add("&7Nenhum som será reproduzido");
            soundLore.add("&7quando seu anúncio for enviado.");
            soundLore.add("");
            soundLore.add("&7Clique para ativar");
        }
        
        ItemStack soundItem = MenuManager.createMenuItem(
                SOUND_MATERIAL,
                hasSound ? "&a&lSom: Ativado" : "&c&lSom: Desativado",
                soundLore
        );
        inventory.setItem(SOUND_SLOT, soundItem);
        
        
        List<String> effectsLore = new ArrayList<>();
        effectsLore.add("");
        effectsLore.add("&7Adicione efeitos visuais quando");
        effectsLore.add("&7seu anúncio for exibido no chat.");
        effectsLore.add("");
        
        if (hasEffects) {
            effectsLore.add("&a✓ Efeitos ativados");
            effectsLore.add("");
            effectsLore.add("&7Serão exibidos efeitos visuais para");
            effectsLore.add("&7todos quando seu anúncio for enviado.");
            effectsLore.add("");
            effectsLore.add("&7Custo adicional: &f+" + plugin.getConfigManager().getConfig().getDouble("costs.effects", 100.0) 
                    + " " + plugin.getEconomyManager().getCurrencyName());
            effectsLore.add("");
            effectsLore.add("&7Clique para desativar");
        } else {
            effectsLore.add("&c✗ Efeitos desativados");
            effectsLore.add("");
            effectsLore.add("&7Nenhum efeito visual será exibido");
            effectsLore.add("&7quando seu anúncio for enviado.");
            effectsLore.add("");
            effectsLore.add("&7Clique para ativar");
        }
        
        ItemStack effectsItem = MenuManager.createMenuItem(
                EFFECTS_MATERIAL,
                hasEffects ? "&a&lEfeitos: Ativados" : "&c&lEfeitos: Desativados",
                effectsLore
        );
        inventory.setItem(EFFECTS_SLOT, effectsItem);
        
        
        List<String> confirmLore = new ArrayList<>();
        confirmLore.add("");
        
        if (category == null || message == null || message.isEmpty()) {
            confirmLore.add("&c✗ Preencha todos os campos obrigatórios");
            confirmLore.add("&cpara poder enviar seu anúncio.");
            confirmLore.add("");
            confirmLore.add("&7Campos obrigatórios:");
            confirmLore.add(category == null ? "&c✗ Categoria" : "&a✓ Categoria");
            confirmLore.add((message == null || message.isEmpty()) ? "&c✗ Mensagem" : "&a✓ Mensagem");
        } else {
            
            double basePrice = plugin.getEconomyManager().getBasePrice();
            double categoryMultiplier = plugin.getConfigManager().getCategorias().getDouble("multipliers." + category.getId(), 1.0);
            double soundCost = hasSound ? plugin.getConfigManager().getConfig().getDouble("costs.sound", 50.0) : 0;
            double effectsCost = hasEffects ? plugin.getConfigManager().getConfig().getDouble("costs.effects", 100.0) : 0;
            
            double totalPrice = basePrice * categoryMultiplier + soundCost + effectsCost;
            
            confirmLore.add("&a✓ Anúncio pronto para envio");
            confirmLore.add("");
            confirmLore.add("&7Custo total: &f" + totalPrice + " " + plugin.getEconomyManager().getCurrencyName());
            confirmLore.add("");
            confirmLore.add("&7Detalhamento:");
            confirmLore.add("&7• Preço base: &f" + basePrice);
            confirmLore.add("&7• Multiplicador da categoria: &f" + categoryMultiplier + "x");
            
            if (hasSound) {
                confirmLore.add("&7• Som: &f+" + soundCost);
            }
            
            if (hasEffects) {
                confirmLore.add("&7• Efeitos: &f+" + effectsCost);
            }
            
            confirmLore.add("");
            confirmLore.add("&e&lClique para enviar o anúncio");
        }
        
        ItemStack confirmItem = MenuManager.createMenuItem(
                CONFIRM_MATERIAL,
                "&a&lEnviar Anúncio",
                confirmLore
        );
        inventory.setItem(CONFIRM_SLOT, confirmItem);
        
        
        List<String> cancelLore = new ArrayList<>();
        cancelLore.add("");
        cancelLore.add("&7Feche este menu e cancele");
        cancelLore.add("&7a criação do anúncio.");
        cancelLore.add("");
        cancelLore.add("&7Seu progresso será salvo");
        cancelLore.add("&7temporariamente.");
        
        ItemStack cancelItem = MenuManager.createMenuItem(
                CANCEL_MATERIAL,
                "&c&lCancelar",
                cancelLore
        );
        inventory.setItem(CANCEL_SLOT, cancelItem);
        
        
        List<String> infoLore = new ArrayList<>();
        infoLore.add("&8Sistema de Anúncios v1.0");
        infoLore.add("");
        infoLore.add("&7Informações úteis:");
        infoLore.add("&7• Cooldown entre anúncios: &f" + plugin.getConfigManager().getConfig().getInt("announcement.cooldown", 60) + "s");
        infoLore.add("&7• Preço base: &f" + plugin.getEconomyManager().getBasePrice() + " " + plugin.getEconomyManager().getCurrencyName());
        infoLore.add("");
        infoLore.add("&e&lClique para mais informações");
        
        ItemStack infoItem = MenuManager.createMenuItem(
                INFO_MATERIAL,
                "&e&lInformações",
                infoLore
        );
        inventory.setItem(INFO_SLOT, infoItem);
        
        
        for (int i = 0; i < 5; i++) {
            XMaterial material = (i < progress) ? PROGRESS_DECORATION : PENDING_DECORATION;
            inventory.setItem(4 + i, GuiUtils.createItem(material, " "));
        }
        
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
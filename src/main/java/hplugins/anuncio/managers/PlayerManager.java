package hplugins.anuncio.managers;

import hplugins.anuncio.hAnuncio;
import hplugins.anuncio.models.Category;
import hplugins.anuncio.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Gerencia interações dos jogadores com o plugin
 */
public class PlayerManager implements Listener {
    
    private final hAnuncio plugin;
    
    
    private final Map<UUID, String> playerInteractions = new HashMap<>();
    private final Map<UUID, Category> editingCategories = new HashMap<>();
    private final Map<UUID, String> editingFields = new HashMap<>();
    
    
    private final Map<UUID, Long> announcementCooldowns = new HashMap<>();
    private final Map<UUID, Integer> dailyAnnouncementCount = new HashMap<>();
    private long lastDailyReset = System.currentTimeMillis();
    
    public PlayerManager(hAnuncio plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Inicia o processo de criação de uma categoria
     * 
     * @param player O jogador que está criando a categoria
     */
    public void startCategoryCreation(Player player) {
        playerInteractions.put(player.getUniqueId(), "creating_category");
    }
    
    /**
     * Inicia o processo de edição de uma categoria
     * 
     * @param player O jogador que está editando a categoria
     * @param category A categoria sendo editada
     * @param field O campo sendo editado (name, prefix, color)
     */
    public void startCategoryEdit(Player player, Category category, String field) {
        playerInteractions.put(player.getUniqueId(), "editing_category");
        editingCategories.put(player.getUniqueId(), category);
        editingFields.put(player.getUniqueId(), field);
    }
    
    /**
     * Cancela qualquer interação de chat do jogador
     * 
     * @param player O jogador
     */
    public void cancelInteraction(Player player) {
        playerInteractions.remove(player.getUniqueId());
        editingCategories.remove(player.getUniqueId());
        editingFields.remove(player.getUniqueId());
    }
    
    /**
     * Verifica se um jogador está em um processo interativo
     * 
     * @param player O jogador
     * @return true se estiver, false caso contrário
     */
    public boolean isInInteraction(Player player) {
        return playerInteractions.containsKey(player.getUniqueId());
    }
    
    /**
     * Obtém o tipo de interação que o jogador está realizando
     * 
     * @param player O jogador
     * @return O tipo de interação ou null se não estiver em uma interação
     */
    public String getInteraction(Player player) {
        return playerInteractions.get(player.getUniqueId());
    }
    
    /**
     * Trata mensagens de chat durante interações
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!isInInteraction(player)) {
            return;
        }
        
        
        event.setCancelled(true);
        
        
        String message = event.getMessage();
        
        
        String interaction = getInteraction(player);
        
        switch (interaction) {
            case "creating_category":
                handleCategoryCreation(player, message);
                break;
                
            case "editing_category":
                handleCategoryEdit(player, message);
                break;
        }
    }
    
    /**
     * Trata a criação de uma nova categoria
     */
    private void handleCategoryCreation(Player player, String name) {
        
        name = name.trim();
        
        
        if (name.isEmpty()) {
            player.sendMessage(MessageUtils.colorize("&cNome de categoria inválido! Digite novamente:"));
            return;
        }
        
        
        if (plugin.getCategoryManager().getCategoryByName(name) != null) {
            player.sendMessage(MessageUtils.colorize("&cJá existe uma categoria com este nome! Digite outro nome:"));
            return;
        }
        
        
        String id = name.toLowerCase().replace(" ", "_");
        String prefix = "&f[" + name + "]";
        String color = "&f";
        String permission = "hanuncio.category." + id.toLowerCase();
        
        Category category = plugin.getCategoryManager().createCategory(id, name, prefix, color, permission);
        
        
        player.sendMessage(MessageUtils.colorize("&aCategoria &f" + name + " &acriada com sucesso!"));
        
        
        cancelInteraction(player);
        
        
        plugin.getMenuManager().openMenu(player, "categoryEditor", category);
    }
    
    /**
     * Trata a edição de uma categoria existente
     */
    private void handleCategoryEdit(Player player, String value) {
        
        value = value.trim();
        
        
        Category category = editingCategories.get(player.getUniqueId());
        String field = editingFields.get(player.getUniqueId());
        
        if (category == null || field == null) {
            cancelInteraction(player);
            player.sendMessage(MessageUtils.colorize("&cOcorreu um erro ao editar a categoria. Tente novamente."));
            return;
        }
        
        
        switch (field) {
            case "name":
                
                Category existingCategory = plugin.getCategoryManager().getCategoryByName(value);
                if (existingCategory != null && !existingCategory.getId().equals(category.getId())) {
                    player.sendMessage(MessageUtils.colorize("&cJá existe uma categoria com este nome! Digite outro nome:"));
                    return;
                }
                
                category.setName(value);
                player.sendMessage(MessageUtils.colorize("&aNome da categoria alterado para &f" + value));
                break;
                
            case "prefix":
                category.setPrefix(value);
                player.sendMessage(MessageUtils.colorize("&aPrefixo da categoria alterado para &f" + value));
                break;
                
            case "color":
                
                if (!value.startsWith("&")) {
                    player.sendMessage(MessageUtils.colorize("&cCódigo de cor inválido! Deve começar com & seguido de um caractere (ex: &a). Digite novamente:"));
                    return;
                }
                
                category.setColor(value);
                player.sendMessage(MessageUtils.colorize("&aCor da categoria alterada para " + value + "Exemplo"));
                break;
                
            default:
                player.sendMessage(MessageUtils.colorize("&cCampo inválido!"));
                break;
        }
        
        
        plugin.getCategoryManager().updateCategory(category);
        
        
        cancelInteraction(player);
        
        
        plugin.getMenuManager().openMenu(player, "categoryEditor", category);
    }
    
    /**
     * Remove as interações quando o jogador sai do servidor
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        cancelInteraction(player);
    }
    
    /**
     * Verifica se o jogador está em cooldown para enviar anúncios
     * 
     * @param player O jogador para verificar
     * @return true se estiver em cooldown, false caso contrário
     */
    public boolean isOnCooldown(Player player) {
        checkDailyReset();
        
        
        if (player.hasPermission(plugin.getConfigManager().getPermissions().getString("bypass.cooldown", "hanuncio.bypass.cooldown"))) {
            return false;
        }
        
        UUID playerUUID = player.getUniqueId();
        if (!announcementCooldowns.containsKey(playerUUID)) {
            return false;
        }
        
        long lastAnnouncement = announcementCooldowns.get(playerUUID);
        long cooldownTime = plugin.getConfigManager().getConfig().getLong("announcement.cooldown", 60) * 1000; 
        
        return (System.currentTimeMillis() - lastAnnouncement) < cooldownTime;
    }
    
    /**
     * Obtém o tempo restante do cooldown em segundos
     * 
     * @param player O jogador para verificar
     * @return Tempo restante em segundos
     */
    public long getCooldownTimeLeft(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!announcementCooldowns.containsKey(playerUUID)) {
            return 0;
        }
        
        long lastAnnouncement = announcementCooldowns.get(playerUUID);
        long cooldownTime = plugin.getConfigManager().getConfig().getLong("announcement.cooldown", 60) * 1000; 
        long timeElapsed = System.currentTimeMillis() - lastAnnouncement;
        
        if (timeElapsed >= cooldownTime) {
            return 0;
        }
        
        return (cooldownTime - timeElapsed) / 1000; 
    }
    
    /**
     * Adiciona o jogador ao cooldown após enviar um anúncio
     * 
     * @param player O jogador que enviou o anúncio
     */
    public void addToCooldown(Player player) {
        checkDailyReset();
        
        UUID playerUUID = player.getUniqueId();
        announcementCooldowns.put(playerUUID, System.currentTimeMillis());
        
        
        int count = dailyAnnouncementCount.getOrDefault(playerUUID, 0);
        dailyAnnouncementCount.put(playerUUID, count + 1);
    }
    
    /**
     * Verifica se o jogador atingiu o limite diário de anúncios
     * 
     * @param player O jogador para verificar
     * @return true se atingiu o limite, false caso contrário
     */
    public boolean reachedDailyLimit(Player player) {
        checkDailyReset();
        
        
        if (player.hasPermission(plugin.getConfigManager().getPermissions().getString("bypass.daily-limit", "hanuncio.bypass.dailylimit"))) {
            return false;
        }
        
        UUID playerUUID = player.getUniqueId();
        int count = dailyAnnouncementCount.getOrDefault(playerUUID, 0);
        int limit = plugin.getConfigManager().getConfig().getInt("announcement.daily-limit", 5);
        
        return count >= limit;
    }
    
    /**
     * Verifica se é necessário resetar os contadores diários
     */
    private void checkDailyReset() {
        long now = System.currentTimeMillis();
        long dayInMillis = TimeUnit.DAYS.toMillis(1);
        
        if (now - lastDailyReset >= dayInMillis) {
            dailyAnnouncementCount.clear();
            lastDailyReset = now;
        }
    }
}
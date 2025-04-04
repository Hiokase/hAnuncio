package hplugins.anuncio.managers;

import com.cryptomorin.xseries.XSound;
import hplugins.anuncio.hAnuncio;
import hplugins.anuncio.models.Announcement;
import hplugins.anuncio.models.Category;
import hplugins.anuncio.utils.EffectUtils;
import hplugins.anuncio.utils.MessageUtils;
import hplugins.anuncio.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Gerencia os anúncios do sistema, criação, visualização e rascunhos
 */
public class AnnouncementManager implements Listener {
    
    private final hAnuncio plugin;
    private final List<Announcement> activeAnnouncements;
    
    
    private final Map<UUID, Category> draftCategories = new HashMap<>();
    private final Map<UUID, String> draftMessages = new HashMap<>();
    private final Map<UUID, Boolean> draftHasSound = new HashMap<>();
    private final Map<UUID, Boolean> draftHasEffects = new HashMap<>();
    
    
    private final Map<UUID, Boolean> inMessageDraftMode = new HashMap<>();
    
    public AnnouncementManager(hAnuncio plugin) {
        this.plugin = plugin;
        this.activeAnnouncements = new ArrayList<>();
        
        
        Bukkit.getPluginManager().registerEvents(new EffectUtils(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Cria e envia um anúncio para todos os jogadores online
     * 
     * @param player Jogador que está enviando o anúncio
     * @param category Categoria do anúncio
     * @param message Mensagem do anúncio
     * @param playSound Se deve reproduzir som ao enviar
     * @param showEffects Se deve mostrar efeitos visuais
     * @return true se o anúncio foi enviado com sucesso
     */
    public boolean createAnnouncement(Player player, Category category, String message, boolean playSound, boolean showEffects) {
        FileConfiguration chatConfig = plugin.getConfigManager().getChat();
        FileConfiguration notificacaoConfig = plugin.getConfigManager().getNotificacao();
        FileConfiguration efeitosConfig = plugin.getConfigManager().getEfeitos();
        
        try {
            
            Announcement announcement = new Announcement(
                    player.getUniqueId(),
                    player.getName(),
                    message,
                    category,
                    System.currentTimeMillis()
            );
            
            activeAnnouncements.add(announcement);
            
            
            String formattedMessage = formatAnnouncement(announcement, chatConfig);
            
            
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(formattedMessage);
            }
            
            
            Bukkit.getConsoleSender().sendMessage(formattedMessage);
            
            
            if (notificacaoConfig.getBoolean("enabled", true)) {
                String notificationMessage = notificacaoConfig.getString("message", "&e%player% enviou um anúncio!")
                        .replace("%player%", player.getName());
                
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (notificacaoConfig.getBoolean("use-action-bar", true) && VersionUtils.isVersionAbove(1, 9)) {
                        
                        MessageUtils.sendActionBar(onlinePlayer, MessageUtils.colorize(notificationMessage));
                    } else {
                        
                        String title = notificacaoConfig.getString("title", "&eNovo Anúncio!");
                        String subtitle = notificacaoConfig.getString("subtitle", "&7De: %player%")
                                .replace("%player%", player.getName());
                        
                        int fadeIn = notificacaoConfig.getInt("fade-in", 10);
                        int stay = notificacaoConfig.getInt("stay", 70);
                        int fadeOut = notificacaoConfig.getInt("fade-out", 20);
                        
                        onlinePlayer.sendTitle(
                                MessageUtils.colorize(title),
                                MessageUtils.colorize(subtitle),
                                fadeIn, stay, fadeOut
                        );
                    }
                }
            }
            
            
            if (playSound && efeitosConfig.getBoolean("sounds.enabled", true)) {
                String soundName = efeitosConfig.getString("sounds.name", "ENTITY_EXPERIENCE_ORB_PICKUP");
                float volume = (float) efeitosConfig.getDouble("sounds.volume", 1.0);
                float pitch = (float) efeitosConfig.getDouble("sounds.pitch", 1.0);
                
                XSound sound = XSound.matchXSound(soundName).orElse(XSound.ENTITY_EXPERIENCE_ORB_PICKUP);
                
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.playSound(onlinePlayer.getLocation(), sound.parseSound(), volume, pitch);
                }
            }
            
            
            if (showEffects && efeitosConfig.getBoolean("particles.enabled", true)) {
                EffectUtils.playAnnouncementEffect(player);
            }
            
            
            plugin.getPlayerManager().addToCooldown(player);
            
            
            player.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.announcement-sent")));
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao criar anúncio: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cria um anúncio com os parâmetros padrão
     */
    public void createAnnouncement(Player player, String message, Category category) {
        boolean useSound = plugin.getConfigManager().getConfig().getBoolean("announcement.default-sound", true);
        boolean useEffects = plugin.getConfigManager().getConfig().getBoolean("announcement.default-effects", true);
        createAnnouncement(player, category, message, useSound, useEffects);
    }
    
    private String formatAnnouncement(Announcement announcement, FileConfiguration chatConfig) {
        String format = chatConfig.getString("format", "&8[&6Anúncio&8] &7%player%&8: &f%message%");
        
        if (announcement.getCategory() != null) {
            format = chatConfig.getString("category-format", "&8[&6Anúncio&8] &8[%category_prefix%&8] &7%player%&8: %category_color%%message%");
        }
        
        String message = format
                .replace("%player%", announcement.getPlayerName())
                .replace("%message%", announcement.getMessage());
        
        if (announcement.getCategory() != null) {
            Category category = announcement.getCategory();
            message = message
                    .replace("%category%", category.getName())
                    .replace("%category_prefix%", category.getPrefix())
                    .replace("%category_color%", category.getColor());
        }
        
        return MessageUtils.colorize(message);
    }
    
    public List<Announcement> getActiveAnnouncements() {
        return new ArrayList<>(activeAnnouncements);
    }
    
    public void clearOldAnnouncements() {
        long now = System.currentTimeMillis();
        long maxAge = plugin.getConfigManager().getConfig().getLong("announcement-max-age", 3600000); 
        
        activeAnnouncements.removeIf(announcement -> (now - announcement.getTimestamp()) > maxAge);
    }
    
    
    
    /**
     * Obtém a categoria de rascunho de um jogador
     */
    public Category getDraftCategory(UUID playerUUID) {
        return draftCategories.get(playerUUID);
    }
    
    /**
     * Define a categoria para o rascunho de um jogador
     */
    public void setDraftCategory(UUID playerUUID, Category category) {
        draftCategories.put(playerUUID, category);
    }
    
    /**
     * Obtém a mensagem de rascunho de um jogador
     */
    public String getDraftMessage(UUID playerUUID) {
        return draftMessages.get(playerUUID);
    }
    
    /**
     * Define a mensagem para o rascunho de um jogador
     */
    public void setDraftMessage(UUID playerUUID, String message) {
        draftMessages.put(playerUUID, message);
    }
    
    /**
     * Verifica se o rascunho tem som ativado
     */
    public boolean getDraftHasSound(UUID playerUUID) {
        return draftHasSound.getOrDefault(playerUUID, false);
    }
    
    /**
     * Define se o rascunho terá som
     */
    public void setDraftHasSound(UUID playerUUID, boolean hasSound) {
        draftHasSound.put(playerUUID, hasSound);
    }
    
    /**
     * Verifica se o rascunho tem efeitos visuais ativados
     */
    public boolean getDraftHasEffects(UUID playerUUID) {
        return draftHasEffects.getOrDefault(playerUUID, false);
    }
    
    /**
     * Define se o rascunho terá efeitos visuais
     */
    public void setDraftHasEffects(UUID playerUUID, boolean hasEffects) {
        draftHasEffects.put(playerUUID, hasEffects);
    }
    
    /**
     * Limpa o rascunho atual de um jogador
     */
    public void clearDraft(UUID playerUUID) {
        draftCategories.remove(playerUUID);
        draftMessages.remove(playerUUID);
        draftHasSound.remove(playerUUID);
        draftHasEffects.remove(playerUUID);
    }
    
    /**
     * Coloca um jogador em modo de criação de mensagem
     */
    public void startMessageDraft(Player player) {
        inMessageDraftMode.put(player.getUniqueId(), true);
    }
    
    /**
     * Verifica se um jogador está em modo de criação de mensagem
     */
    public boolean isInMessageDraftMode(UUID playerUUID) {
        return inMessageDraftMode.getOrDefault(playerUUID, false);
    }
    
    /**
     * Remove um jogador do modo de criação de mensagem
     */
    public void stopMessageDraftMode(UUID playerUUID) {
        inMessageDraftMode.remove(playerUUID);
    }
    
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        
        if (isInMessageDraftMode(playerUUID)) {
            event.setCancelled(true);
            
            final String message = event.getMessage();
            
            
            if (message.equalsIgnoreCase("cancelar") || message.equalsIgnoreCase("cancel")) {
                stopMessageDraftMode(playerUUID);
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(MessageUtils.colorize("&c&l✗ &cOperação cancelada!"));
                    plugin.getMenuManager().openMenu(player, "announcement");
                });
                
                return;
            }
            
            
            setDraftMessage(playerUUID, message);
            stopMessageDraftMode(playerUUID);
            
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(MessageUtils.colorize("&a&l✓ &aMensagem definida com sucesso!"));
                plugin.getMenuManager().openMenu(player, "announcement");
            });
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        inMessageDraftMode.remove(playerUUID);
        
        
    }
}

package hplugins.anuncio.commands;

import com.cryptomorin.xseries.XMaterial;
import hplugins.anuncio.gui.AnnouncementMenu;
import hplugins.anuncio.hAnuncio;
import hplugins.anuncio.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AnunciarCommand implements CommandExecutor, TabCompleter {
    
    private final hAnuncio plugin;
    
    public AnunciarCommand(hAnuncio plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration permissions = plugin.getConfigManager().getPermissions();
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.player-only")));
            return true;
        }
        
        Player player = (Player) sender;
        
        
        if (!player.hasPermission(permissions.getString("announcements.create", "hanuncio.anunciar"))) {
            player.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.no-permission")));
            return true;
        }
        
        
        if (plugin.getPlayerManager().isOnCooldown(player)) {
            long timeLeft = plugin.getPlayerManager().getCooldownTimeLeft(player);
            player.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.cooldown")
                    .replace("%time%", String.valueOf(timeLeft))));
            return true;
        }
        
        
        if (plugin.getPlayerManager().reachedDailyLimit(player)) {
            player.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.daily-limit-reached")));
            return true;
        }
        
        
        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "limpar":
                    
                    plugin.getAnnouncementManager().clearDraft(player.getUniqueId());
                    player.sendMessage(MessageUtils.colorize("&a&l✓ &aRascunho de anúncio limpo com sucesso!"));
                    return true;
                    
                case "salvar":
                    
                    player.sendMessage(MessageUtils.colorize("&a&l✓ &aSeu rascunho foi salvo e estará disponível quando você abrir o menu novamente."));
                    return true;
                    
                case "ajuda":
                    
                    player.sendMessage(MessageUtils.colorize("&e&l=== Sistema de Anúncios - Ajuda ==="));
                    player.sendMessage(MessageUtils.colorize("&e/anunciar &7- Abre o menu de criação de anúncios"));
                    player.sendMessage(MessageUtils.colorize("&e/anunciar limpar &7- Limpa o rascunho atual"));
                    player.sendMessage(MessageUtils.colorize("&e/anunciar salvar &7- Salva o rascunho para uso posterior"));
                    return true;
                    
                default:
                    player.sendMessage(MessageUtils.colorize("&c&l⚠ &cComando desconhecido. Use &f/anunciar ajuda &cpara ver os comandos disponíveis."));
                    return true;
            }
        }
        
        
        plugin.getMenuManager().openMenu(player, "announcement");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            
            List<String> subCommands = new ArrayList<>();
            subCommands.add("limpar");
            subCommands.add("salvar");
            subCommands.add("ajuda");
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }
        
        return completions;
    }
}

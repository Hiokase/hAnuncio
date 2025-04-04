package hplugins.anuncio.commands;

import hplugins.anuncio.hAnuncio;
import hplugins.anuncio.models.Category;
import hplugins.anuncio.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabCompleter {
    
    private final hAnuncio plugin;
    
    public AdminCommand(hAnuncio plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration permissions = plugin.getConfigManager().getPermissions();
        String permPrefix = "hanuncio.admin";
        
        if (!plugin.getPermissionManager().hasPermission(sender.getName(), permPrefix)) {
            sender.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.no-permission")));
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!plugin.getPermissionManager().hasPermission(sender.getName(), permPrefix + ".reload")) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                plugin.getConfigManager().loadAllConfigs();
                plugin.getCategoryManager().loadCategories();
                sender.sendMessage(MessageUtils.colorize("&aConfiguração recarregada com sucesso!"));
                break;
                
            case "categorias":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtils.colorize("&cEste comando só pode ser usado por jogadores!"));
                    return true;
                }
                
                Player player = (Player) sender;
                if (!plugin.getPermissionManager().hasPermission(player, permPrefix + ".category.manage")) {
                    player.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.no-permission")));
                    return true;
                }

                plugin.getMenuManager().openMenu(player, "categories");
                break;
                
            case "confirmardeletar":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtils.colorize("&cEste comando só pode ser usado por jogadores!"));
                    return true;
                }
                
                player = (Player) sender;
                if (!plugin.getPermissionManager().hasPermission(player, permPrefix + ".category.delete")) {
                    player.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(MessageUtils.colorize("&cUso: /hac confirmardeletar <nome>"));
                    return true;
                }
                
                String categoryName = args[1];
                Category categoryToDelete = plugin.getCategoryManager().getCategoryByName(categoryName);
                
                if (categoryToDelete == null) {
                    sender.sendMessage(MessageUtils.colorize("&cCategoria não encontrada!"));
                    return true;
                }
                
                if (plugin.getCategoryManager().deleteCategory(categoryToDelete.getId())) {
                    sender.sendMessage(MessageUtils.colorize("&aCategoria &f" + categoryName + " &adeletada com sucesso!"));
                } else {
                    sender.sendMessage(MessageUtils.colorize("&cOcorreu um erro ao deletar a categoria. Tente novamente."));
                }
                break;
                
            case "economia":
                if (!plugin.getPermissionManager().hasPermission(sender.getName(), permPrefix + ".economy")) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(MessageUtils.colorize("&e=== Comandos de Economia ==="));
                    sender.sendMessage(MessageUtils.colorize("&7/hac economia info &f- Informações sobre o sistema de economia"));
                    sender.sendMessage(MessageUtils.colorize("&7/hac economia setprovider <nome> &f- Definir provedor de economia"));
                    sender.sendMessage(MessageUtils.colorize("&7/hac economia setpreco <valor> &f- Definir preço base de anúncios"));
                    return true;
                }
                
                switch (args[1].toLowerCase()) {
                    case "info":
                        sender.sendMessage(MessageUtils.colorize("&e=== Informações de Economia ==="));
                        sender.sendMessage(MessageUtils.colorize("&7Provedor ativo: &f" + plugin.getEconomyManager().getActiveProviderName()));
                        sender.sendMessage(MessageUtils.colorize("&7Preço base: &f" + plugin.getEconomyManager().getBasePrice()));
                        
                        List<String> providers = plugin.getEconomyManager().getAvailableProviders();
                        if (providers.isEmpty()) {
                            sender.sendMessage(MessageUtils.colorize("&7Provedores disponíveis: &cNenhum"));
                        } else {
                            sender.sendMessage(MessageUtils.colorize("&7Provedores disponíveis: &f" + String.join(", ", providers)));
                        }
                        break;
                        
                    case "setprovider":
                        if (args.length < 3) {
                            sender.sendMessage(MessageUtils.colorize("&cUso: /hac economia setprovider <nome>"));
                            return true;
                        }
                        
                        String providerName = args[2];
                        if (plugin.getEconomyManager().setProvider(providerName)) {
                            sender.sendMessage(MessageUtils.colorize("&aProvedor de economia alterado para: &f" + providerName));
                        } else {
                            sender.sendMessage(MessageUtils.colorize("&cProvedor de economia não encontrado ou não está disponível!"));
                            
                            List<String> availableProviders = plugin.getEconomyManager().getAvailableProviders();
                            if (!availableProviders.isEmpty()) {
                                sender.sendMessage(MessageUtils.colorize("&7Provedores disponíveis: &f" + String.join(", ", availableProviders)));
                            }
                        }
                        break;
                        
                    case "setpreco":
                        if (args.length < 3) {
                            sender.sendMessage(MessageUtils.colorize("&cUso: /hac economia setpreco <valor>"));
                            return true;
                        }
                        
                        try {
                            double price = Double.parseDouble(args[2]);
                            if (price < 0) {
                                sender.sendMessage(MessageUtils.colorize("&cO preço não pode ser negativo!"));
                                return true;
                            }
                            
                            plugin.getEconomyManager().setBasePrice(price);
                            sender.sendMessage(MessageUtils.colorize("&aPreço base de anúncios alterado para: &f" + price));
                        } catch (NumberFormatException e) {
                            sender.sendMessage(MessageUtils.colorize("&cValor inválido! Use um número válido."));
                        }
                        break;
                        
                    default:
                        sender.sendMessage(MessageUtils.colorize("&cComando desconhecido. Use /hac economia para ver os comandos disponíveis."));
                        break;
                }
                break;

            case "criarcategoria":
                if (!plugin.getPermissionManager().hasPermission(sender.getName(), permPrefix + ".category.create")) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.colorize("&cUso: /hac criarcategoria <nome> <prefixo> [cor]"));
                    return true;
                }
                
                String name = args[1];
                String prefix = args[2];
                String color = args.length > 3 ? args[3] : "&f";
                
                if (plugin.getCategoryManager().getCategoryByName(name) != null) {
                    sender.sendMessage(MessageUtils.colorize("&cEsta categoria já existe!"));
                    return true;
                }
                
                String id = name.toLowerCase().replace(" ", "_");
                String permission = "hanuncio.category." + id.toLowerCase();
                
                Category category = plugin.getCategoryManager().createCategory(id, name, prefix, color, permission);
                sender.sendMessage(MessageUtils.colorize("&aCategoria " + name + " criada com sucesso!"));
                break;
                
            case "deletarcategoria":
                if (!plugin.getPermissionManager().hasPermission(sender.getName(), permPrefix + ".category.delete")) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(MessageUtils.colorize("&cUso: /hac deletarcategoria <nome>"));
                    return true;
                }
                
                name = args[1];
                Category existingCategory = plugin.getCategoryManager().getCategoryByName(name);
                
                if (existingCategory == null) {
                    sender.sendMessage(MessageUtils.colorize("&cCategoria não encontrada!"));
                    return true;
                }
                
                if (plugin.getCategoryManager().deleteCategory(existingCategory.getId())) {
                    sender.sendMessage(MessageUtils.colorize("&aCategoria " + name + " removida com sucesso!"));
                } else {
                    sender.sendMessage(MessageUtils.colorize("&cOcorreu um erro ao deletar a categoria!"));
                }
                break;
                
            case "listarcategorias":
                if (!plugin.getPermissionManager().hasPermission(sender.getName(), permPrefix + ".category.list")) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                
                List<Category> categories = plugin.getCategoryManager().getAllCategories();
                if (categories.isEmpty()) {
                    sender.sendMessage(MessageUtils.colorize("&cNenhuma categoria encontrada!"));
                    return true;
                }
                
                sender.sendMessage(MessageUtils.colorize("&e=== Categorias Disponíveis ==="));
                for (Category cat : categories) {
                    sender.sendMessage(MessageUtils.colorize("&7- " + cat.getName() + " &8(Prefixo: " + cat.getPrefix() + "&8, Cor: " + cat.getColor() + "Exemplo&8)"));
                }
                break;
                
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "=== hAnuncio Admin Commands ===");
        sender.sendMessage(ChatColor.GRAY + "/hac reload " + ChatColor.WHITE + "- Recarregar configurações");
        sender.sendMessage(ChatColor.GRAY + "/hac categorias " + ChatColor.WHITE + "- Abrir o menu de gerenciamento de categorias");
        sender.sendMessage(ChatColor.GRAY + "/hac economia " + ChatColor.WHITE + "- Gerenciar sistema de economia");

        sender.sendMessage(ChatColor.YELLOW + "=== Comandos Legados ===");
        sender.sendMessage(ChatColor.GRAY + "/hac criarcategoria <nome> <prefixo> [cor] " + ChatColor.WHITE + "- Criar categoria");
        sender.sendMessage(ChatColor.GRAY + "/hac deletarcategoria <nome> " + ChatColor.WHITE + "- Deletar categoria");
        sender.sendMessage(ChatColor.GRAY + "/hac listarcategorias " + ChatColor.WHITE + "- Listar todas categorias");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String permPrefix = "hanuncio.admin";
        
        if (!plugin.getPermissionManager().hasPermission(sender.getName(), permPrefix)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("reload", "categorias", "confirmardeletar", "economia", 
                                "criarcategoria", "deletarcategoria", "listarcategorias")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("confirmardeletar") || 
                args[0].equalsIgnoreCase("deletarcategoria")) {
                
                return plugin.getCategoryManager().getAllCategories().stream()
                        .map(Category::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            
            if (args[0].equalsIgnoreCase("economia")) {
                return Arrays.asList("info", "setprovider", "setpreco")
                        .stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("economia") && args[1].equalsIgnoreCase("setprovider")) {
                return plugin.getEconomyManager().getAvailableProviders()
                        .stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
}

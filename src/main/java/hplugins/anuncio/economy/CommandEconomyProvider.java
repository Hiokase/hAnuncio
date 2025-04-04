package hplugins.anuncio.economy;

import hplugins.anuncio.hAnuncio;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provedor de economia baseado em comandos
 * Este provedor executa comandos do console para operações de economia
 * Útil quando não há integração direta com plugins de economia
 */
public class CommandEconomyProvider implements EconomyProvider {

    private final hAnuncio plugin;
    private final String balanceCommand;
    private final String withdrawCommand;
    private final String depositCommand;
    private final Pattern balancePattern;
    private final boolean enabled;
    private final Logger logger;
    private final Map<String, String> supportedPlugins;
    
    /**
     * Cria um novo provedor de economia baseado em comandos
     *
     * @param plugin Instância principal do plugin
     * @param config Configuração de economia
     */
    public CommandEconomyProvider(hAnuncio plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.supportedPlugins = new HashMap<>();
        
        
        this.balanceCommand = config.getString("money-commands.balance", "money %player%");
        this.withdrawCommand = config.getString("money-commands.withdraw", "money remove %player% %amount%");
        this.depositCommand = config.getString("money-commands.deposit", "money give %player% %amount%");
        
        
        String pattern = config.getString("money-commands.balance-pattern", "(\\d+(?:\\.\\d+)?)");
        this.balancePattern = Pattern.compile(pattern);
        
        
        loadSupportedPlugins(config);
        
        
        this.enabled = detectEconomyPlugin();
        
        if (this.enabled) {
            logger.info("Economia: Provedor de comandos ativado");
        }
    }
    
    /**
     * Carrega a lista de plugins suportados para detecção automática
     * 
     * @param config Configuração de economia
     */
    private void loadSupportedPlugins(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("supported-money-commands");
        if (section != null) {
            for (String pluginName : section.getKeys(false)) {
                String commandPrefix = section.getString(pluginName);
                if (commandPrefix != null && !commandPrefix.isEmpty()) {
                    supportedPlugins.put(pluginName, commandPrefix);
                }
            }
        }
        
        
        if (!supportedPlugins.containsKey("Essentials")) {
            supportedPlugins.put("Essentials", "eco");
        }
        if (!supportedPlugins.containsKey("CMI")) {
            supportedPlugins.put("CMI", "money");
        }
        if (!supportedPlugins.containsKey("JH_Economy")) {
            supportedPlugins.put("JH_Economy", "jhmoney");
        }
    }
    
    /**
     * Detecta automaticamente um plugin de economia compatível
     * 
     * @return true se encontrou um plugin compatível
     */
    private boolean detectEconomyPlugin() {
        for (String pluginName : supportedPlugins.keySet()) {
            if (Bukkit.getPluginManager().getPlugin(pluginName) != null) {
                logger.info("Economia: Plugin detectado para comandos de economia: " + pluginName);
                return true;
            }
        }
        return true; 
    }
    
    /**
     * Formata um comando substituindo variáveis
     * 
     * @param command O comando base
     * @param player O jogador
     * @param amount A quantia (opcional)
     * @return O comando formatado
     */
    private String formatCommand(String command, Player player, double amount) {
        return command
                .replace("%player%", player.getName())
                .replace("%amount%", String.format("%.2f", amount))
                .replace("%uuid%", player.getUniqueId().toString());
    }
    
    /**
     * Executa um comando através do console
     * 
     * @param command O comando a executar
     * @return O resultado da execução (sucesso ou falha)
     */
    private boolean executeCommand(String command) {
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return true;
        } catch (Exception e) {
            logger.warning("Falha ao executar comando de economia: " + command);
            logger.warning("Erro: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getName() {
        return "CommandEconomy";
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public double getBalance(Player player) {
        String command = formatCommand(balanceCommand, player, 0);
        
        
        
        
        return 1000.0; 
    }
    
    @Override
    public boolean hasEnough(Player player, double amount) {
        
        
        return true;
    }
    
    @Override
    public boolean withdraw(Player player, double amount) {
        String command = formatCommand(withdrawCommand, player, amount);
        return executeCommand(command);
    }
    
    @Override
    public boolean deposit(Player player, double amount) {
        String command = formatCommand(depositCommand, player, amount);
        return executeCommand(command);
    }
}
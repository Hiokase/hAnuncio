package hplugins.anuncio.economy;

import hplugins.anuncio.hAnuncio;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Classe principal que gerencia todos os provedores de economia
 * Este adaptador permite que o plugin use qualquer fonte de economia
 */
public class EconomyAdapter {
    
    private final List<EconomyProvider> providers = new ArrayList<>();
    private EconomyProvider activeProvider = null;
    private final Logger logger;
    
    /**
     * Cria um novo adaptador de economia
     * 
     * @param economyConfig Configuração de economia do plugin
     * @param logger Logger do plugin para mensagens
     * @param plugin Instância do plugin principal (opcional, necessário para CommandEconomyProvider)
     */
    public EconomyAdapter(FileConfiguration economyConfig, Logger logger, hAnuncio plugin) {
        this.logger = logger;
        setupProviders(economyConfig, plugin);
    }
    
    /**
     * Construtor alternativo para compatibilidade
     * 
     * @param economyConfig Configuração de economia do plugin
     * @param logger Logger do plugin para mensagens
     */
    public EconomyAdapter(FileConfiguration economyConfig, Logger logger) {
        this.logger = logger;
        setupProviders(economyConfig, null);
    }
    
    /**
     * Configura todos os provedores de economia disponíveis
     * 
     * @param config Configuração de economia
     * @param plugin Instância do plugin (pode ser null)
     */
    private void setupProviders(FileConfiguration config, hAnuncio plugin) {
        
        boolean useVault = config.getBoolean("use-vault", true);
        if (useVault) {
            VaultEconomyProvider vaultProvider = new VaultEconomyProvider();
            if (vaultProvider.isEnabled()) {
                providers.add(vaultProvider);
                logger.info("Economia: Vault adicionado como provedor");
            }
        }
        
        
        if (config.getBoolean("use-jh-economy", true)) {
            JHEconomyProvider jhProvider = new JHEconomyProvider();
            if (jhProvider.isEnabled()) {
                providers.add(jhProvider);
                logger.info("Economia: JH_Economy adicionado como provedor (implementação especializada)");
            }
        }
        
        
        ConfigurationSection customProviders = config.getConfigurationSection("custom-providers");
        if (customProviders != null) {
            for (String providerName : customProviders.getKeys(false)) {
                
                if (providerName.equalsIgnoreCase("JH_Economy")) continue;
                
                ConfigurationSection providerSection = customProviders.getConfigurationSection(providerName);
                if (providerSection != null) {
                    String balanceMethod = providerSection.getString("balance-method", "");
                    String withdrawMethod = providerSection.getString("withdraw-method", "");
                    String depositMethod = providerSection.getString("deposit-method", "");
                    boolean usePlayerObject = providerSection.getBoolean("use-player-object", false);
                    
                    if (!balanceMethod.isEmpty() && !withdrawMethod.isEmpty()) {
                        GenericEconomyProvider provider = new GenericEconomyProvider(
                                providerName, balanceMethod, withdrawMethod, depositMethod, usePlayerObject);
                        
                        if (provider.isEnabled()) {
                            providers.add(provider);
                            logger.info("Economia: " + providerName + " adicionado como provedor");
                        }
                    }
                }
            }
        }
        
        
        ConfigurationSection legacyProviders = config.getConfigurationSection("supported-plugins");
        if (legacyProviders != null) {
            for (String providerName : legacyProviders.getKeys(false)) {
                ConfigurationSection providerSection = legacyProviders.getConfigurationSection(providerName);
                if (providerSection != null) {
                    String balanceMethod = providerSection.getString("balance-method", "");
                    String withdrawMethod = providerSection.getString("withdraw-method", "");
                    String depositMethod = providerSection.getString("deposit-method", balanceMethod.replace("get", "add"));
                    boolean usePlayerObject = providerSection.getBoolean("use-player-object", false);
                    
                    if (!balanceMethod.isEmpty() && !withdrawMethod.isEmpty()) {
                        GenericEconomyProvider provider = new GenericEconomyProvider(
                                providerName, balanceMethod, withdrawMethod, depositMethod, usePlayerObject);
                        
                        if (provider.isEnabled()) {
                            providers.add(provider);
                            logger.info("Economia: " + providerName + " adicionado como provedor (legado)");
                        }
                    }
                }
            }
        }
        
        
        String preferredProvider = config.getString("preferred-plugin", "");
        if (!preferredProvider.isEmpty()) {
            for (EconomyProvider provider : providers) {
                if (provider.getName().equalsIgnoreCase(preferredProvider)) {
                    activeProvider = provider;
                    logger.info("Economia: " + provider.getName() + " definido como provedor preferido");
                    break;
                }
            }
        }
        
        
        if (activeProvider == null && !providers.isEmpty()) {
            activeProvider = providers.get(0);
            logger.info("Economia: " + activeProvider.getName() + " definido como provedor padrão");
        }
        
        
        boolean useMoneyCommands = config.getBoolean("use-money-commands", false);
        if (plugin != null && useMoneyCommands) {
            CommandEconomyProvider commandProvider = new CommandEconomyProvider(plugin, config);
            providers.add(commandProvider);
            logger.info("Economia: Provedor de comandos adicionado como fallback");
            
            
            if (activeProvider == null) {
                activeProvider = commandProvider;
                logger.info("Economia: Usando comandos de economia como provedor padrão");
            }
        } else if (activeProvider == null) {
            logger.warning("Economia: Nenhum provedor de economia encontrado ou habilitado!");
        }
    }
    
    /**
     * Verifica se o adaptador tem pelo menos um provedor de economia ativo
     * 
     * @return true se existe pelo menos um provedor de economia ativo
     */
    public boolean hasProvider() {
        return activeProvider != null && activeProvider.isEnabled();
    }
    
    /**
     * Verifica se o jogador tem saldo suficiente
     * 
     * @param player O jogador
     * @param amount A quantidade a verificar
     * @return true se o jogador tem saldo suficiente
     */
    public boolean hasEnough(Player player, double amount) {
        if (!hasProvider()) return false;
        return activeProvider.hasEnough(player, amount);
    }
    
    /**
     * Retira dinheiro do jogador
     * 
     * @param player O jogador
     * @param amount A quantidade a retirar
     * @return true se a operação foi bem-sucedida
     */
    public boolean withdraw(Player player, double amount) {
        if (!hasProvider()) return false;
        return activeProvider.withdraw(player, amount);
    }
    
    /**
     * Deposita dinheiro na conta do jogador
     * 
     * @param player O jogador
     * @param amount A quantidade a depositar
     * @return true se a operação foi bem-sucedida
     */
    public boolean deposit(Player player, double amount) {
        if (!hasProvider()) return false;
        return activeProvider.deposit(player, amount);
    }
    
    /**
     * Obtém o saldo do jogador
     * 
     * @param player O jogador
     * @return O saldo atual do jogador
     */
    public double getBalance(Player player) {
        if (!hasProvider()) return 0.0;
        return activeProvider.getBalance(player);
    }
    
    /**
     * Muda o provedor de economia ativo
     * 
     * @param providerName Nome do provedor a ser ativado
     * @return true se o provedor foi encontrado e ativado
     */
    public boolean setActiveProvider(String providerName) {
        for (EconomyProvider provider : providers) {
            if (provider.getName().equalsIgnoreCase(providerName) && provider.isEnabled()) {
                activeProvider = provider;
                logger.info("Economia: Provedor alterado para " + provider.getName());
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtém o nome do provedor de economia ativo
     * 
     * @return Nome do provedor ativo ou "Nenhum" se não houver
     */
    public String getActiveProviderName() {
        return hasProvider() ? activeProvider.getName() : "Nenhum";
    }
    
    /**
     * Lista todos os provedores de economia disponíveis
     * 
     * @return Lista com nomes dos provedores disponíveis
     */
    public List<String> getAvailableProviders() {
        List<String> result = new ArrayList<>();
        for (EconomyProvider provider : providers) {
            if (provider.isEnabled()) {
                result.add(provider.getName());
            }
        }
        return result;
    }
}
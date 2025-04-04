package hplugins.anuncio.managers;

import hplugins.anuncio.hAnuncio;
import hplugins.anuncio.economy.EconomyAdapter;
import hplugins.anuncio.models.Category;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Gerenciador de economia do plugin
 * Usa o EconomyAdapter para interagir com diferentes plugins de economia
 */
public class EconomyManager {
    
    private final hAnuncio plugin;
    private final EconomyAdapter economyAdapter;
    private double basePrice;
    
    /**
     * Cria um novo gerenciador de economia
     * 
     * @param plugin A instância principal do plugin
     */
    public EconomyManager(hAnuncio plugin) {
        this.plugin = plugin;
        this.economyAdapter = createEconomyAdapter();
        this.basePrice = loadPrices();
    }
    
    /**
     * Cria o adaptador do sistema de economia
     * 
     * @return O adaptador de economia configurado
     */
    private EconomyAdapter createEconomyAdapter() {
        FileConfiguration economyConfig = plugin.getConfigManager().getEconomy();
        
        EconomyAdapter adapter = new EconomyAdapter(economyConfig, plugin.getLogger(), plugin);
        
        if (adapter.hasProvider()) {
            plugin.getLogger().info("Economia: Usando " + adapter.getActiveProviderName() + " para integração!");
        } else {
            plugin.getLogger().warning("Economia: Nenhum plugin de economia encontrado! Certifique-se de que você tem o Vault ou outro plugin de economia compatível instalado.");
        }
        return adapter;
    }
    
    /**
     * Carrega os preços da configuração
     * 
     * @return O preço base de um anúncio
     */
    private double loadPrices() {
        FileConfiguration economyConfig = plugin.getConfigManager().getEconomy();
        return economyConfig.getDouble("price", 100.0);
    }
    
    /**
     * Verifica se o jogador tem dinheiro suficiente
     * 
     * @param player O jogador
     * @param amount A quantidade a verificar
     * @return true se o jogador tem dinheiro suficiente
     */
    public boolean hasEnough(Player player, double amount) {
        return economyAdapter.hasEnough(player, amount);
    }
    
    /**
     * Retira dinheiro do jogador
     * 
     * @param player O jogador
     * @param amount A quantidade a retirar
     * @return true se o dinheiro foi retirado com sucesso
     */
    public boolean withdrawMoney(Player player, double amount) {
        return economyAdapter.withdraw(player, amount);
    }
    
    /**
     * Adiciona dinheiro ao jogador
     * 
     * @param player O jogador
     * @param amount A quantidade a adicionar
     * @return true se o dinheiro foi adicionado com sucesso
     */
    public boolean depositMoney(Player player, double amount) {
        return economyAdapter.deposit(player, amount);
    }
    
    /**
     * Obtém o saldo do jogador
     * 
     * @param player O jogador
     * @return O saldo atual do jogador
     */
    public double getBalance(Player player) {
        return economyAdapter.getBalance(player);
    }
    
    /**
     * Verifica se existe algum plugin de economia disponível
     * 
     * @return true se há pelo menos um plugin de economia funcional
     */
    public boolean isEconomyEnabled() {
        return economyAdapter.hasProvider();
    }
    
    /**
     * Obtém o nome do provedor de economia ativo
     * 
     * @return O nome do provedor atual
     */
    public String getActiveProviderName() {
        return economyAdapter.getActiveProviderName();
    }
    
    /**
     * Lista todos os provedores de economia disponíveis
     * 
     * @return Lista com os nomes dos provedores
     */
    public List<String> getAvailableProviders() {
        return economyAdapter.getAvailableProviders();
    }
    
    /**
     * Muda o provedor de economia ativo
     * 
     * @param providerName Nome do provedor para ativar
     * @return true se o provedor foi encontrado e ativado
     */
    public boolean setProvider(String providerName) {
        return economyAdapter.setActiveProvider(providerName);
    }
    
    /**
     * Obtém o preço base de um anúncio
     * 
     * @return O preço base configurado
     */
    public double getBasePrice() {
        return basePrice;
    }
    
    /**
     * Atualiza o preço base de um anúncio
     * 
     * @param newPrice O novo preço
     */
    public void setBasePrice(double newPrice) {
        this.basePrice = newPrice;
        FileConfiguration economyConfig = plugin.getConfigManager().getEconomy();
        economyConfig.set("price", newPrice);
        plugin.getConfigManager().saveConfig("economy.yml");
    }
    
    /**
     * Calcula o preço total de um anúncio com base na categoria e opções extras
     * 
     * @param player O jogador que está criando o anúncio (para possíveis descontos)
     * @param category A categoria do anúncio
     * @param hasSound Se o anúncio terá som
     * @param hasEffects Se o anúncio terá efeitos visuais
     * @return O preço total do anúncio
     */
    public double getAnnouncementPrice(Player player, Category category, boolean hasSound, boolean hasEffects) {
        double basePrice = getBasePrice();
        
        
        double categoryMultiplier = plugin.getConfigManager().getCategorias().getDouble("multipliers." + category.getId(), 1.0);
        double price = basePrice * categoryMultiplier;
        
        
        if (hasSound) {
            price += plugin.getConfigManager().getConfig().getDouble("costs.sound", 50.0);
        }
        
        if (hasEffects) {
            price += plugin.getConfigManager().getConfig().getDouble("costs.effects", 100.0);
        }
        
        
        if (player.hasPermission(plugin.getConfigManager().getPermissions().getString("discounts.vip", "hanuncio.discount.vip"))) {
            double discount = plugin.getConfigManager().getConfig().getDouble("costs.discounts.vip", 0.1); 
            price = price * (1 - discount);
        }
        
        return price;
    }
    
    /**
     * Sobrecarga para obter o preço do anúncio atual do jogador
     */
    public double getAnnouncementPrice(Player player, Category category) {
        boolean hasSound = plugin.getAnnouncementManager().getDraftHasSound(player.getUniqueId());
        boolean hasEffects = plugin.getAnnouncementManager().getDraftHasEffects(player.getUniqueId());
        return getAnnouncementPrice(player, category, hasSound, hasEffects);
    }
    
    /**
     * Verifica se o jogador tem dinheiro suficiente
     */
    public boolean hasMoney(Player player, double amount) {
        return hasEnough(player, amount);
    }
    
    /**
     * Retira dinheiro do jogador
     */
    public boolean takeMoney(Player player, double amount) {
        return withdrawMoney(player, amount);
    }
    
    /**
     * Obtém o nome da moeda
     */
    public String getCurrencyName() {
        return plugin.getConfigManager().getEconomy().getString("currency-name", "$");
    }
}

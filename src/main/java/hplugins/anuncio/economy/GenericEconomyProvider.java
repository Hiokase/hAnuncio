package hplugins.anuncio.economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Um provedor de economia genérico que usa reflexão para interagir com qualquer plugin de economia
 * Isto permite que o hAnuncio suporte qualquer plugin de economia sem dependências diretas
 */
public class GenericEconomyProvider implements EconomyProvider {
    
    private final Plugin economyPlugin;
    private final String pluginName;
    private final String balanceMethod;
    private final String withdrawMethod;
    private final String depositMethod;
    private final boolean hasPlayerArg;
    private final boolean enabled;
    private final Logger logger;
    
    /**
     * Constrói um novo provedor de economia genérico
     * 
     * @param pluginName O nome do plugin de economia
     * @param balanceMethod O nome do método para obter saldo (ex: "getBalance")
     * @param withdrawMethod O nome do método para retirar dinheiro (ex: "withdrawPlayer")
     * @param depositMethod O nome do método para depositar dinheiro (ex: "depositPlayer")
     * @param hasPlayerArg Se true, o método recebe Player como argumento; se false, recebe String (nome do jogador)
     */
    public GenericEconomyProvider(String pluginName, String balanceMethod, String withdrawMethod, 
                                  String depositMethod, boolean hasPlayerArg) {
        this.pluginName = pluginName;
        this.balanceMethod = balanceMethod;
        this.withdrawMethod = withdrawMethod;
        this.depositMethod = depositMethod;
        this.hasPlayerArg = hasPlayerArg;
        this.logger = Bukkit.getLogger();
        
        
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin != null && plugin.isEnabled()) {
            this.economyPlugin = plugin;
            this.enabled = true;
            logger.info("Economia: Plugin " + pluginName + " detectado e habilitado!");
        } else {
            this.economyPlugin = null;
            this.enabled = false;
            logger.warning("Economia: Plugin " + pluginName + " não encontrado ou não está habilitado!");
        }
    }
    
    @Override
    public boolean hasEnough(Player player, double amount) {
        double balance = getBalance(player);
        return balance >= amount;
    }
    
    @Override
    public boolean withdraw(Player player, double amount) {
        if (!enabled || economyPlugin == null) return false;
        
        try {
            Object result;
            
            if (hasPlayerArg) {
                Method method = economyPlugin.getClass().getMethod(withdrawMethod, Player.class, double.class);
                result = method.invoke(economyPlugin, player, amount);
            } else {
                Method method = economyPlugin.getClass().getMethod(withdrawMethod, String.class, double.class);
                result = method.invoke(economyPlugin, player.getName(), amount);
            }
            
            
            return result == null || (result instanceof Boolean && (Boolean) result);
            
        } catch (Exception e) {
            logger.severe("Erro ao tentar retirar dinheiro usando " + pluginName + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deposit(Player player, double amount) {
        if (!enabled || economyPlugin == null) return false;
        
        try {
            Object result;
            
            if (hasPlayerArg) {
                Method method = economyPlugin.getClass().getMethod(depositMethod, Player.class, double.class);
                result = method.invoke(economyPlugin, player, amount);
            } else {
                Method method = economyPlugin.getClass().getMethod(depositMethod, String.class, double.class);
                result = method.invoke(economyPlugin, player.getName(), amount);
            }
            
            
            return result == null || (result instanceof Boolean && (Boolean) result);
            
        } catch (Exception e) {
            logger.severe("Erro ao tentar depositar dinheiro usando " + pluginName + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public double getBalance(Player player) {
        if (!enabled || economyPlugin == null) return 0.0;
        
        try {
            Object result;
            
            if (hasPlayerArg) {
                Method method = economyPlugin.getClass().getMethod(balanceMethod, Player.class);
                result = method.invoke(economyPlugin, player);
            } else {
                Method method = economyPlugin.getClass().getMethod(balanceMethod, String.class);
                result = method.invoke(economyPlugin, player.getName());
            }
            
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            } else {
                logger.warning("O método " + balanceMethod + " não retornou um número!");
                return 0.0;
            }
            
        } catch (Exception e) {
            logger.severe("Erro ao tentar obter saldo usando " + pluginName + ": " + e.getMessage());
            return 0.0;
        }
    }
    
    @Override
    public boolean isEnabled() {
        return enabled && economyPlugin != null && economyPlugin.isEnabled();
    }
    
    @Override
    public String getName() {
        return pluginName;
    }
}
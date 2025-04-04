package hplugins.anuncio.economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Provedor de economia específico para o JH_Economy
 * Esta classe implementa acesso direto aos métodos do JH_Economy usando reflexão
 */
public class JHEconomyProvider implements EconomyProvider {
    
    private boolean enabled = false;
    private Method getMoney = null;
    private Method addMoney = null;
    private Method removeMoney = null;
    private Class<?> mainClass = null;
    
    /**
     * Cria um novo provedor de economia para o JH_Economy
     */
    public JHEconomyProvider() {
        try {
            
            Plugin plugin = Bukkit.getPluginManager().getPlugin("JH_Economy");
            if (plugin == null || !plugin.isEnabled()) {
                Bukkit.getLogger().warning("JH_Economy não encontrado ou não está habilitado!");
                return;
            }
            
            
            mainClass = Class.forName("JH_Economy.Main");
            
            
            getMoney = mainClass.getDeclaredMethod("getMoney", Player.class);
            addMoney = mainClass.getDeclaredMethod("addMoney", Player.class, double.class);
            removeMoney = mainClass.getDeclaredMethod("removeMoney", Player.class, double.class);
            
            enabled = true;
            Bukkit.getLogger().info("JH_Economy integrado com sucesso!");
            
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().severe("Não foi possível encontrar a classe principal do JH_Economy: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            Bukkit.getLogger().severe("Não foi possível encontrar um método do JH_Economy: " + e.getMessage());
        } catch (Exception e) {
            Bukkit.getLogger().severe("Erro ao integrar com JH_Economy: " + e.getMessage());
        }
    }

    @Override
    public boolean hasEnough(Player player, double amount) {
        double balance = getBalance(player);
        return balance >= amount;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!enabled || removeMoney == null) return false;
        
        try {
            
            Object result = removeMoney.invoke(null, player, amount);
            
            
            
            return result == null || (result instanceof Boolean && (Boolean) result);
            
        } catch (Exception e) {
            Bukkit.getLogger().severe("Erro ao retirar dinheiro usando JH_Economy: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deposit(Player player, double amount) {
        if (!enabled || addMoney == null) return false;
        
        try {
            
            Object result = addMoney.invoke(null, player, amount);
            
            
            
            return result == null || (result instanceof Boolean && (Boolean) result);
            
        } catch (Exception e) {
            Bukkit.getLogger().severe("Erro ao adicionar dinheiro usando JH_Economy: " + e.getMessage());
            return false;
        }
    }

    @Override
    public double getBalance(Player player) {
        if (!enabled || getMoney == null) return 0.0;
        
        try {
            
            Object result = getMoney.invoke(null, player);
            
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            } else {
                Bukkit.getLogger().warning("JH_Economy.getMoney não retornou um número!");
                return 0.0;
            }
            
        } catch (Exception e) {
            Bukkit.getLogger().severe("Erro ao obter saldo usando JH_Economy: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return "JH_Economy";
    }
}
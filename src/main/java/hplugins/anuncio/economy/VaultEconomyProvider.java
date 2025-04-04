package hplugins.anuncio.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Implementação do EconomyProvider para o Vault
 */
public class VaultEconomyProvider implements EconomyProvider {
    
    private Economy economy;
    private boolean enabled;
    
    public VaultEconomyProvider() {
        this.enabled = setupVault();
    }
    
    private boolean setupVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    @Override
    public boolean hasEnough(Player player, double amount) {
        return economy.has(player, amount);
    }
    
    @Override
    public boolean withdraw(Player player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    @Override
    public boolean deposit(Player player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
    
    @Override
    public double getBalance(Player player) {
        return economy.getBalance(player);
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public String getName() {
        return "Vault";
    }
}
package hplugins.anuncio.economy;

import org.bukkit.entity.Player;

/**
 * Interface para provedores de economia
 * Isso permite a criação de adaptadores para qualquer plugin de economia
 */
public interface EconomyProvider {
    
    /**
     * Verifica se o jogador possui saldo suficiente
     * 
     * @param player O jogador
     * @param amount Quantidade a verificar
     * @return true se tem saldo suficiente, false caso contrário
     */
    boolean hasEnough(Player player, double amount);
    
    /**
     * Retira dinheiro do jogador
     * 
     * @param player O jogador
     * @param amount Quantidade a retirar
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    boolean withdraw(Player player, double amount);
    
    /**
     * Adiciona dinheiro ao jogador
     * 
     * @param player O jogador
     * @param amount Quantidade a adicionar
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    boolean deposit(Player player, double amount);
    
    /**
     * Obtém o saldo do jogador
     * 
     * @param player O jogador
     * @return O saldo atual do jogador
     */
    double getBalance(Player player);
    
    /**
     * Verifica se este provedor está disponível e funcionando
     * 
     * @return true se o provedor está operacional, false caso contrário
     */
    boolean isEnabled();
    
    /**
     * Obtém o nome do plugin de economia
     * 
     * @return O nome do plugin de economia
     */
    String getName();
}
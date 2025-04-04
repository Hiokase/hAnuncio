package hplugins.anuncio.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MessageUtils {
    
    
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)[&§][0-9A-FK-OR]");
    
    /**
     * Colorize a message with color codes
     * @param message The message to colorize
     * @return The colorized message
     */
    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Remove todos os códigos de cor de uma string
     * 
     * @param message A mensagem que contém códigos de cor
     * @return A mensagem sem códigos de cor
     */
    public static String removeColor(String message) {
        if (message == null) return "";
        return COLOR_PATTERN.matcher(message).replaceAll("");
    }
    
    /**
     * Aplica cores a uma lista de strings
     * 
     * @param messages Lista de mensagens para colorir
     * @return Lista com mensagens coloridas
     */
    public static List<String> colorize(List<String> messages) {
        List<String> colorized = new ArrayList<>();
        for (String message : messages) {
            colorized.add(colorize(message));
        }
        return colorized;
    }
    
    /**
     * Centraliza um texto para exibição no chat, preenchendo com espaços
     *
     * @param message A mensagem a ser centralizada
     * @param length O comprimento total desejado (incluindo espaços)
     * @return A mensagem centralizada
     */
    public static String centered(String message, int length) {
        if (message == null) return "";
        
        String stripped = removeColor(message);
        int messageLength = stripped.length();
        int spaces = (length - messageLength) / 2;
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            result.append(" ");
        }
        result.append(message);
        
        return result.toString();
    }
    
    /**
     * Send an action bar message to a player
     * @param player The player to send the message to
     * @param message The message to send
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null) return;
        
        message = colorize(message);
        
        if (VersionUtils.isVersionAbove(1, 11)) {
            
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
            return;
        }
        
        
        try {
            Class<?> packetPlayOutChatClass = getNMSClass("PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");
            Class<?> chatComponentTextClass = getNMSClass("ChatComponentText");
            java.lang.reflect.Constructor<?> constructor = chatComponentTextClass.getConstructor(String.class);
            Object chatComponentTextObject = constructor.newInstance(message);
            
            Object chatCompontentText = iChatBaseComponentClass.cast(chatComponentTextObject);
            Object packetPlayOutChat;
            
            if (VersionUtils.isVersionAbove(1, 8) && !VersionUtils.isVersionAbove(1, 12)) {
                
                Class<?> chatMessageTypeClass = getNMSClass("ChatMessageType");
                Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
                Constructor<?> packetConstructor = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass);
                packetPlayOutChat = packetConstructor.newInstance(chatCompontentText, chatMessageTypes[2]);
            } else {
                
                Constructor<?> packetConstructor = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class);
                packetPlayOutChat = packetConstructor.newInstance(chatCompontentText, (byte) 2);
            }
            
            sendPacket(player, packetPlayOutChat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Envia um título e subtítulo para o jogador
     * 
     * @param player O jogador para enviar
     * @param title O título principal (pode ser null)
     * @param subtitle O subtítulo (pode ser null)
     * @param fadeIn Tempo de fade in em ticks
     * @param stay Tempo de exibição em ticks
     * @param fadeOut Tempo de fade out em ticks
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) return;
        
        if (title != null) {
            title = colorize(title);
        }
        
        if (subtitle != null) {
            subtitle = colorize(subtitle);
        }
        
        if (VersionUtils.isVersionAbove(1, 8)) {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        } else {
            
            sendActionBar(player, title != null ? title : (subtitle != null ? subtitle : ""));
        }
    }
    
    /**
     * Send a packet to a player
     * @param player The player to send the packet to
     * @param packet The packet to send
     */
    private static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get an NMS class
     * @param className The name of the class
     * @return The class
     */
    private static Class<?> getNMSClass(String className) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

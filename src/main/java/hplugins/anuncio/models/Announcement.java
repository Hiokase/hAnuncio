package hplugins.anuncio.models;

import java.util.UUID;

public class Announcement {
    
    private final UUID playerUUID;
    private final String playerName;
    private final String message;
    private final Category category;
    private final long timestamp;
    
    public Announcement(UUID playerUUID, String playerName, String message, Category category, long timestamp) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.message = message;
        this.category = category;
        this.timestamp = timestamp;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "Announcement{" +
                "playerName='" + playerName + '\'' +
                ", message='" + message + '\'' +
                ", category=" + (category != null ? category.getName() : "none") +
                ", timestamp=" + timestamp +
                '}';
    }
}

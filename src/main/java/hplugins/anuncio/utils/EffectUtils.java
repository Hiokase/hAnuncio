package hplugins.anuncio.utils;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import hplugins.anuncio.hAnuncio;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Objects;
import java.util.Random;

public class EffectUtils implements Listener {
    
    private final hAnuncio plugin;
    private static final Random random = new Random();
    
    public EffectUtils(hAnuncio plugin) {
        this.plugin = plugin;
    }
    
    public static void playAnnouncementEffect(Player player) {
        FileConfiguration effectsConfig = hAnuncio.getInstance().getConfigManager().getEfeitos();
        
        
        if (!effectsConfig.getBoolean("particles.enabled", true)) {
            return;
        }
        
        String effectType = effectsConfig.getString("particles.type", "FIREWORK").toUpperCase();
        
        switch (effectType) {
            case "FIREWORK":
                spawnFirework(player);
                break;
            case "PARTICLE":
                spawnParticles(player, effectsConfig);
                break;
            default:
                
                spawnFirework(player);
                break;
        }
    }
    
    private static void spawnFirework(Player player) {
        FileConfiguration effectsConfig = hAnuncio.getInstance().getConfigManager().getEfeitos();
        ConfigurationSection fireworkSection = effectsConfig.getConfigurationSection("particles.firework");
        
        if (fireworkSection == null) {
            fireworkSection = effectsConfig.createSection("particles.firework");
        }
        
        Location location = player.getLocation();
        
        
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        
        
        int power = fireworkSection.getInt("power", 1);
        meta.setPower(Math.min(power, 3)); 
        
        
        FireworkEffect.Builder builder = FireworkEffect.builder();
        
        
        String typeStr = fireworkSection.getString("type", "BALL");
        try {
            FireworkEffect.Type type = FireworkEffect.Type.valueOf(typeStr.toUpperCase());
            builder.with(type);
        } catch (IllegalArgumentException e) {
            builder.with(FireworkEffect.Type.BALL); 
        }
        
        
        boolean useRandomColors = fireworkSection.getBoolean("random-colors", true);
        if (useRandomColors) {
            builder.withColor(getRandomColor(), getRandomColor());
        } else {
            
            String colorStr = fireworkSection.getString("colors", "255,0,0");
            String[] colors = colorStr.split(";");
            
            for (String color : colors) {
                String[] rgb = color.split(",");
                if (rgb.length >= 3) {
                    try {
                        int r = Integer.parseInt(rgb[0].trim());
                        int g = Integer.parseInt(rgb[1].trim());
                        int b = Integer.parseInt(rgb[2].trim());
                        builder.withColor(Color.fromRGB(r, g, b));
                    } catch (NumberFormatException e) {
                        builder.withColor(Color.RED); 
                    }
                }
            }
        }
        
        
        boolean useRandomFadeColors = fireworkSection.getBoolean("random-fade-colors", true);
        if (useRandomFadeColors) {
            builder.withFade(getRandomColor());
        } else {
            
            String fadeColorStr = fireworkSection.getString("fade-colors", "0,0,255");
            String[] fadeColors = fadeColorStr.split(";");
            
            for (String fadeColor : fadeColors) {
                String[] rgb = fadeColor.split(",");
                if (rgb.length >= 3) {
                    try {
                        int r = Integer.parseInt(rgb[0].trim());
                        int g = Integer.parseInt(rgb[1].trim());
                        int b = Integer.parseInt(rgb[2].trim());
                        builder.withFade(Color.fromRGB(r, g, b));
                    } catch (NumberFormatException e) {
                        builder.withFade(Color.BLUE); 
                    }
                }
            }
        }
        
        
        builder.flicker(fireworkSection.getBoolean("flicker", true));
        builder.trail(fireworkSection.getBoolean("trail", true));
        
        meta.addEffect(builder.build());
        firework.setFireworkMeta(meta);
    }
    
    private static void spawnParticles(Player player, FileConfiguration effectsConfig) {
        if (!VersionUtils.isVersionAbove(1, 9)) {
            
            playSound(player, effectsConfig);
            return;
        }
        
        ConfigurationSection particleSection = effectsConfig.getConfigurationSection("particles.custom");
        
        if (particleSection == null) {
            particleSection = effectsConfig.createSection("particles.custom");
        }
        
        String particleName = particleSection.getString("name", "VILLAGER_HAPPY");
        int count = particleSection.getInt("count", 20);
        double offsetX = particleSection.getDouble("offset-x", 0.5);
        double offsetY = particleSection.getDouble("offset-y", 0.5);
        double offsetZ = particleSection.getDouble("offset-z", 0.5);
        double speed = particleSection.getDouble("speed", 0.1);
        
        try {
            
            Location location = player.getLocation().add(0, 1, 0);
            player.getWorld().spawnParticle(
                    org.bukkit.Particle.valueOf(particleName),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    count,
                    offsetX,
                    offsetY,
                    offsetZ,
                    speed
            );
        } catch (Exception e) {
            
            playSound(player, effectsConfig);
        }
    }
    
    private static void playSound(Player player, FileConfiguration effectsConfig) {
        if (!effectsConfig.getBoolean("sounds.enabled", true)) {
            return;
        }
        
        String soundName = effectsConfig.getString("sounds.name", "ENTITY_EXPERIENCE_ORB_PICKUP");
        float volume = (float) effectsConfig.getDouble("sounds.volume", 1.0);
        float pitch = (float) effectsConfig.getDouble("sounds.pitch", 1.0);
        
        XSound sound = XSound.matchXSound(soundName).orElse(XSound.ENTITY_EXPERIENCE_ORB_PICKUP);
        player.playSound(player.getLocation(), sound.parseSound(), volume, pitch);
    }
    
    private static Color getRandomColor() {
        return Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        
        if (event.getDamager() instanceof Firework && event.getEntity() instanceof Player) {
            Firework firework = (Firework) event.getDamager();
            
            
            if (plugin.getConfigManager().getEfeitos().getBoolean("particles.firework.prevent-damage", true)) {
                event.setCancelled(true);
            }
        }
    }
}

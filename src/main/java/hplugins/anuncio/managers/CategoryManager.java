package hplugins.anuncio.managers;

import hplugins.anuncio.hAnuncio;
import hplugins.anuncio.models.Category;
import hplugins.anuncio.utils.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerencia as categorias disponíveis para anúncios
 */
public class CategoryManager {
    
    private final hAnuncio plugin;
    private final Map<String, Category> categories = new HashMap<>();
    private final File categoriesFile;
    private FileConfiguration categoriesConfig;
    
    public CategoryManager(hAnuncio plugin) {
        this.plugin = plugin;
        this.categoriesFile = new File(plugin.getDataFolder(), "categories.yml");
        
        
        loadCategories();
    }
    
    /**
     * Carrega as categorias do arquivo de configuração
     */
    public void loadCategories() {
        categories.clear();
        
        
        if (!categoriesFile.exists()) {
            try {
                categoriesFile.getParentFile().mkdirs();
                categoriesFile.createNewFile();
                
                
                categoriesConfig = YamlConfiguration.loadConfiguration(categoriesFile);
                createDefaultCategories();
                categoriesConfig.save(categoriesFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo categories.yml");
                e.printStackTrace();
                return;
            }
        } else {
            
            categoriesConfig = YamlConfiguration.loadConfiguration(categoriesFile);
        }
        
        
        ConfigurationSection section = categoriesConfig.getConfigurationSection("categories");
        
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".name", key);
                String prefix = section.getString(key + ".prefix", "[" + name + "]");
                String color = section.getString(key + ".color", "&f");
                String permission = section.getString(key + ".permission", "hanuncio.category." + key.toLowerCase());
                
                Category category = new Category(key, name, prefix, color, permission);
                categories.put(key.toLowerCase(), category);
            }
        }
        
        
        if (categories.isEmpty()) {
            createDefaultCategories();
            try {
                categoriesConfig.save(categoriesFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível salvar as categorias padrão");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Cria as categorias padrão no arquivo de configuração
     */
    private void createDefaultCategories() {
        
        createCategory("geral", "Geral", "&f[Geral]", "&f", "hanuncio.category.geral");
        
        
        createCategory("comercio", "Comércio", "&6[Comércio]", "&6", "hanuncio.category.comercio");
        
        
        createCategory("anuncio", "Anúncio", "&a[Anúncio]", "&a", "hanuncio.category.anuncio");
        
        
        createCategory("recrutamento", "Recrutamento", "&9[Recrutamento]", "&9", "hanuncio.category.recrutamento");
        
        
        createCategory("evento", "Evento", "&5[Evento]", "&5", "hanuncio.category.evento");
    }
    
    /**
     * Cria uma nova categoria no arquivo de configuração e no cache
     * 
     * @param id ID único da categoria
     * @param name Nome da categoria
     * @param prefix Prefixo da categoria
     * @param color Cor da categoria
     * @param permission Permissão para usar a categoria
     * @return A categoria criada
     */
    public Category createCategory(String id, String name, String prefix, String color, String permission) {
        
        categoriesConfig.set("categories." + id + ".name", name);
        categoriesConfig.set("categories." + id + ".prefix", prefix);
        categoriesConfig.set("categories." + id + ".color", color);
        categoriesConfig.set("categories." + id + ".permission", permission);
        
        
        try {
            categoriesConfig.save(categoriesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível salvar a categoria: " + name);
            e.printStackTrace();
        }
        
        
        Category category = new Category(id, name, prefix, color, permission);
        categories.put(id.toLowerCase(), category);
        
        return category;
    }
    
    /**
     * Atualiza uma categoria existente
     * 
     * @param category A categoria atualizada
     * @return true se a categoria foi atualizada com sucesso, false caso contrário
     */
    public boolean updateCategory(Category category) {
        if (!categories.containsKey(category.getId().toLowerCase())) {
            return false;
        }
        
        
        categoriesConfig.set("categories." + category.getId() + ".name", category.getName());
        categoriesConfig.set("categories." + category.getId() + ".prefix", category.getPrefix());
        categoriesConfig.set("categories." + category.getId() + ".color", category.getColor());
        categoriesConfig.set("categories." + category.getId() + ".permission", category.getPermission());
        
        
        try {
            categoriesConfig.save(categoriesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível atualizar a categoria: " + category.getName());
            e.printStackTrace();
            return false;
        }
        
        
        categories.put(category.getId().toLowerCase(), category);
        
        return true;
    }
    
    /**
     * Deleta uma categoria
     * 
     * @param categoryId ID da categoria a ser deletada
     * @return true se a categoria foi deletada com sucesso, false caso contrário
     */
    public boolean deleteCategory(String categoryId) {
        if (!categories.containsKey(categoryId.toLowerCase())) {
            return false;
        }
        
        
        categoriesConfig.set("categories." + categoryId, null);
        
        
        try {
            categoriesConfig.save(categoriesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível deletar a categoria: " + categoryId);
            e.printStackTrace();
            return false;
        }
        
        
        categories.remove(categoryId.toLowerCase());
        
        return true;
    }
    
    /**
     * Obtém uma categoria pelo ID
     * 
     * @param id ID da categoria
     * @return A categoria, ou null se não existir
     */
    public Category getCategory(String id) {
        return categories.get(id.toLowerCase());
    }
    
    /**
     * Obtém uma categoria pelo nome
     * 
     * @param name Nome da categoria
     * @return A categoria, ou null se não existir
     */
    public Category getCategoryByName(String name) {
        for (Category category : categories.values()) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }
    
    /**
     * Obtém todas as categorias
     * 
     * @return Lista com todas as categorias
     */
    public List<Category> getAllCategories() {
        return new ArrayList<>(categories.values());
    }
    
    /**
     * Obtém todas as categorias que um jogador tem permissão para usar
     * 
     * @param playerName Nome do jogador
     * @return Lista com as categorias permitidas
     */
    public List<Category> getPlayerCategories(String playerName) {
        List<Category> playerCategories = new ArrayList<>();
        
        for (Category category : categories.values()) {
            if (plugin.getPermissionManager().hasPermission(playerName, category.getPermission())) {
                playerCategories.add(category);
            }
        }
        
        return playerCategories;
    }
    
    /**
     * Obtém o número total de categorias disponíveis
     * 
     * @return O número de categorias
     */
    public int getCategoryCount() {
        return categories.size();
    }
}
package hplugins.anuncio.models;

/**
 * Representa uma categoria de anúncio
 */
public class Category {
    
    private String id;
    private String name;
    private String prefix;
    private String color;
    private String permission;
    
    /**
     * Cria uma nova categoria de anúncio
     * 
     * @param id ID único da categoria
     * @param name Nome da categoria
     * @param prefix Prefixo da categoria (mostrado antes da mensagem)
     * @param color Cor da categoria (código de cor)
     * @param permission Permissão necessária para usar esta categoria
     */
    public Category(String id, String name, String prefix, String color, String permission) {
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.color = color;
        this.permission = permission;
    }
    
    /**
     * Obtém o ID da categoria
     * 
     * @return ID da categoria
     */
    public String getId() {
        return id;
    }
    
    /**
     * Define o ID da categoria
     * 
     * @param id Novo ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Obtém o nome da categoria
     * 
     * @return Nome da categoria
     */
    public String getName() {
        return name;
    }
    
    /**
     * Define o nome da categoria
     * 
     * @param name Novo nome
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Obtém o prefixo da categoria
     * 
     * @return Prefixo
     */
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * Define o prefixo da categoria
     * 
     * @param prefix Novo prefixo
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    /**
     * Obtém o código de cor da categoria
     * 
     * @return Código de cor
     */
    public String getColor() {
        return color;
    }
    
    /**
     * Define o código de cor da categoria
     * 
     * @param color Novo código de cor
     */
    public void setColor(String color) {
        this.color = color;
    }
    
    /**
     * Obtém a permissão necessária para usar esta categoria
     * 
     * @return Permissão
     */
    public String getPermission() {
        return permission;
    }
    
    /**
     * Define a permissão necessária para usar esta categoria
     * 
     * @param permission Nova permissão
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Category category = (Category) o;
        
        return id.equalsIgnoreCase(category.id);
    }
    
    @Override
    public int hashCode() {
        return id.toLowerCase().hashCode();
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", color='" + color + '\'' +
                ", permission='" + permission + '\'' +
                '}';
    }
}
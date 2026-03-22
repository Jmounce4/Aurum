package BHML.aurum.runes.core;

/**
 * Configuration class for rune limits per item type.
 * All values are adjustable for easy balancing.
 */
public class RuneLimits {
    
    // Maximum total runes per item type
    public static int SWORD_MAX_RUNES = 3;
    public static int BOW_MAX_RUNES = 3;
    public static int PICKAXE_MAX_RUNES = 2;
    public static int CHESTPLATE_MAX_RUNES = 1;
    
    // Maximum elemental runes per item type
    public static int SWORD_MAX_ELEMENTAL = 1;
    public static int BOW_MAX_ELEMENTAL = 1;
    public static int PICKAXE_MAX_ELEMENTAL = 1;
    public static int CHESTPLATE_MAX_ELEMENTAL = 1;
    
    /**
     * Gets the maximum total runes allowed for the given item type
     * @param itemType The item type (e.g., "sword", "bow", "pickaxe", "chestplate")
     * @return Maximum number of runes allowed
     */
    public static int getMaxRunes(String itemType) {
        return switch (itemType.toLowerCase()) {
            case "sword" -> SWORD_MAX_RUNES;
            case "bow" -> BOW_MAX_RUNES;
            case "pickaxe" -> PICKAXE_MAX_RUNES;
            case "chestplate" -> CHESTPLATE_MAX_RUNES;
            default -> 0;
        };
    }
    
    /**
     * Gets the maximum elemental runes allowed for the given item type
     * @param itemType The item type (e.g., "sword", "bow", "pickaxe", "chestplate")
     * @return Maximum number of elemental runes allowed
     */
    public static int getMaxElementalRunes(String itemType) {
        return switch (itemType.toLowerCase()) {
            case "sword" -> SWORD_MAX_ELEMENTAL;
            case "bow" -> BOW_MAX_ELEMENTAL;
            case "pickaxe" -> PICKAXE_MAX_ELEMENTAL;
            case "chestplate" -> CHESTPLATE_MAX_ELEMENTAL;
            default -> 0;
        };
    }
    
    /**
     * Checks if an element is considered elemental (not NORMAL)
     * @param element The element to check
     * @return true if elemental, false if normal
     */
    public static boolean isElemental(BHML.aurum.elements.Element element) {
        return element != null && element != BHML.aurum.elements.Element.NORMAL;
    }
}

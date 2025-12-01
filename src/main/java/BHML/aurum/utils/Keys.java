package BHML.aurum.utils;
import BHML.aurum.Aurum;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class Keys {
    public static NamespacedKey SCROLL_ID;
    public static NamespacedKey SCROLL_USES;
    public static NamespacedKey SCROLL_MAX_USES;
    public static NamespacedKey SCROLL_COOLDOWN;
    public static NamespacedKey CLERIC_SCROLL_ADDED;


    public static void init(JavaPlugin plugin) {
        SCROLL_ID        = new NamespacedKey(plugin, "scroll_id");
        SCROLL_USES      = new NamespacedKey(plugin, "scroll_uses");
        SCROLL_MAX_USES  = new NamespacedKey(plugin, "scroll_max_uses");
        SCROLL_COOLDOWN  = new NamespacedKey(plugin, "scroll_cd");
        CLERIC_SCROLL_ADDED = new NamespacedKey(plugin, "cleric_scroll_added");
    }
}

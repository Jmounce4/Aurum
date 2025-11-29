package BHML.aurum.scrolls.core;

import BHML.aurum.scrolls.fire.Fireball;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class ScrollFactory {

    private static final Map<String, Scroll> REGISTERED_SCROLLS = new HashMap<>();

    static {
        // Register all scrolls HERE
        register(new Fireball());
        // register(new ZapScroll());
        // register(new EarthSpikeScroll());
    }

    private static void register(Scroll scroll) {
        REGISTERED_SCROLLS.put(scroll.getId().toLowerCase(), scroll);
    }

    public static Scroll getScroll(String id) {
        return REGISTERED_SCROLLS.get(id.toLowerCase());
    }

    public static ItemStack createScrollItem(Scroll scroll) {
        return ScrollUtils.createScrollItem(scroll);
    }

    public static Map<String, Scroll> getScrolls() {
        return REGISTERED_SCROLLS;
    }

}

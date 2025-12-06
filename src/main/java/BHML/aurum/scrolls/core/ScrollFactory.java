package BHML.aurum.scrolls.core;

import BHML.aurum.scrolls.air.Gust;
import BHML.aurum.scrolls.air.Windstride;
import BHML.aurum.scrolls.earth.Rumble;
import BHML.aurum.scrolls.earth.TerraPath;
import BHML.aurum.scrolls.ender.EndShot;
import BHML.aurum.scrolls.fire.Fireball;
import BHML.aurum.scrolls.lightning.Flash;
import BHML.aurum.scrolls.lightning.FlyingThunderGod;
import BHML.aurum.scrolls.lightning.Zap;
import BHML.aurum.scrolls.water.Geyser;
import BHML.aurum.scrolls.water.LiquidLance;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class ScrollFactory {

    private static final Map<String, Scroll> REGISTERED_SCROLLS = new HashMap<>();

    static {
        // Register all scrolls HERE
        register(new Fireball());
        register(new LiquidLance());
        register(new Geyser());
        register(new Zap());
        register(new Flash());
        register(new FlyingThunderGod());
        register(new Gust());
        register(new Windstride());
        register(new Rumble());
        register(new TerraPath());
        register(new EndShot());

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

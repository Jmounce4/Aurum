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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScrollRegistry {

    private static final Map<String, Scroll> REGISTRY = new HashMap<>();

    public static void register(Scroll scroll) {
        REGISTRY.put(scroll.getId().toLowerCase(), scroll);
    }

    public static Scroll get(String id) {
        return REGISTRY.get(id.toLowerCase());
    }

    public static Map<String, Scroll> all() { return Collections.unmodifiableMap(REGISTRY); }

    // Call this at startup
    public static void registerDefaults() {
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
    }



}

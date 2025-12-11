package BHML.aurum.runes.core;

import BHML.aurum.runes.normal.Sniper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RuneRegistry {

    private static final Map<String, Rune> runes = new HashMap<>();

    public static void registerRune(Rune rune) {
        runes.put(rune.getId().toLowerCase(), rune);
    }

    public static Rune getRune(String id) {
        return runes.get(id.toLowerCase());
    }

    public static Collection<Rune> getAllRunes() {
        return runes.values();
    }

    public static void registerDefaults() {
        registerRune(new Sniper());

    }


}

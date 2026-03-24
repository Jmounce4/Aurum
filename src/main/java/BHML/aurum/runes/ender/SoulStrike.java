package BHML.aurum.runes.ender;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class SoulStrike implements Rune {
    
    @Override
    public Element getElement() {
        return Element.ENDER;
    }

    @Override
    public String getId() {
        return "soul_strike";
    }

    @Override
    public String getName() {
        return "Soul Strike";
    }

    @Override
    public String getDescription() {
        return "Defeating a target imbues the soul of the defeated enemy in your sword. Your next strike with a soul will deal bonus damage based on the strength of the stolen soul.";
    }

    @Override
    public int getCooldown() {
        return 0; // No cooldown - passive effect
    }

    @Override
    public String getDisplayItem() {
        return "echo_shard"; // Ender/very rare rune uses echo shard
    }

    @Override
    public String getItem() {
        return "sword";
    }
}

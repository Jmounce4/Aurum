package BHML.aurum.runes.earth;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Absorb implements Rune {
    
    @Override
    public Element getElement() {
        return Element.EARTH;
    }

    @Override
    public String getId() {
        return "absorb";
    }

    @Override
    public String getName() {
        return "Absorb";
    }

    @Override
    public String getDescription() {
        return "Killing a target gives you bonus Health & Damage for 10 minutes (up to 10 stacks)";
    }

    @Override
    public int getCooldown() {
        return 0; // No cooldown - passive effect
    }

    @Override
    public String getDisplayItem() {
        return "gold_nugget";
    }

    @Override
    public String getItem() {
        return "sword";
    }
}

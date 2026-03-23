package BHML.aurum.runes.normal;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Experienced implements Rune {
    
    @Override
    public Element getElement() {
        return Element.NORMAL;
    }

    @Override
    public String getId() {
        return "experienced";
    }

    @Override
    public String getName() {
        return "Experienced";
    }

    @Override
    public String getDescription() {
        return "Deal more damage based on your XP level.";
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

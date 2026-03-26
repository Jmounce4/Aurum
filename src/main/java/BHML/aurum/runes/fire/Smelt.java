package BHML.aurum.runes.fire;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Smelt implements Rune {
    
    @Override
    public Element getElement() {
        return Element.FIRE;
    }

    @Override
    public String getId() {
        return "smelt";
    }

    @Override
    public String getName() {
        return "Smelt";
    }

    @Override
    public String getDescription() {
        return "Automatically smelts ores and grants +1 XP per ore mined";
    }

    @Override
    public int getCooldown() {
        return 0; // No cooldown - passive effect
    }

    @Override
    public String getDisplayItem() {
        return "AMETHYST_SHARD";
    }

    @Override
    public String getItem() {
        return "pickaxe";
    }
}

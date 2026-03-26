package BHML.aurum.runes.water;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Cascade implements Rune {
    
    @Override
    public Element getElement() {
        return Element.WATER;
    }

    @Override
    public String getId() {
        return "cascade";
    }

    @Override
    public String getName() {
        return "Cascade";
    }

    @Override
    public String getDescription() {
        return "Also mines the block directly below the mined block";
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

package BHML.aurum.runes.earth;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Trench implements Rune {
    
    @Override
    public Element getElement() {
        return Element.EARTH;
    }

    @Override
    public String getId() {
        return "trench";
    }

    @Override
    public String getName() {
        return "Trench";
    }

    @Override
    public String getDescription() {
        return "Breaks all blocks within a 3x3 radius around the mined block";
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

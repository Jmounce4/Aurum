package BHML.aurum.runes.water;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Flow implements Rune {
    
    @Override
    public Element getElement() {
        return Element.WATER;
    }

    @Override
    public String getId() {
        return "flow";
    }

    @Override
    public String getName() {
        return "Flow";
    }

    @Override
    public String getDescription() {
        return "Build up flow by hitting targets. Flow increases movement and attack speed. Lose flow when you are hit.";
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
        return "sword";
    }
}

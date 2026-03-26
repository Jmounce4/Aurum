package BHML.aurum.runes.air;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Vacuum implements Rune {

    @Override
    public Element getElement() {
        return Element.AIR;
    }

    @Override
    public String getId() {
        return "vacuum";
    }

    @Override
    public String getName() {
        return "Vacuum";
    }

    @Override
    public String getDescription() {
        return "Gain haste and pull in nearby items on the ground";
    }

    @Override
    public int getCooldown() {
        return 0; // Passive effect
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

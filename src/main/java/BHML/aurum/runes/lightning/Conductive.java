package BHML.aurum.runes.lightning;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Conductive implements Rune {

    @Override
    public Element getElement() {
        return Element.LIGHTNING;
    }

    @Override
    public String getId() {
        return "conductive";
    }

    @Override
    public String getName() {
        return "Conductive";
    }

    @Override
    public String getDescription() {
        return "Electricity draws you to the nearest gold ore.";
    }

    @Override
    public int getCooldown() {
        return 0;
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

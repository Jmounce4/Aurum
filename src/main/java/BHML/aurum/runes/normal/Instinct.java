package BHML.aurum.runes.normal;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Instinct implements Rune {

    @Override
    public Element getElement() {
        return Element.NORMAL;
    }

    @Override
    public String getId() {
        return "instinct";
    }

    @Override
    public String getName() {
        return "Instinct";
    }

    @Override
    public String getDescription() {
        return "Chance to dodge attacks";
    }

    @Override
    public int getCooldown() {
        return 0;
    }

    @Override
    public String getDisplayItem() {
        return "GOLD_NUGGET";
    }

    @Override
    public String getItem() {
        return "sword";
    }
}

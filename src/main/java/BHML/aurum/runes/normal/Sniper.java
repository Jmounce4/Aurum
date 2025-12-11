package BHML.aurum.runes.normal;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Sniper implements Rune {

    @Override
    public Element getElement() {
        return Element.NORMAL;
    }

    @Override
    public String getId() {
        return "sniper";
    }

    @Override
    public String getName() {
        return "Sniper";
    }

    @Override
    public String getDescription() {
        return "Arrows deal +1 damage per 10 blocks traveled.";
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
        return "bow";
    }


}

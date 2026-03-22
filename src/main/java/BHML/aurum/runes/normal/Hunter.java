package BHML.aurum.runes.normal;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Hunter implements Rune {

    @Override
    public Element getElement() {
        return Element.NORMAL;
    }

    @Override
    public String getId() {
        return "hunter";
    }

    @Override
    public String getName() {
        return "Hunter";
    }

    @Override
    public String getDescription() {
        return "Bonus XP and loot from bow kills.";
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

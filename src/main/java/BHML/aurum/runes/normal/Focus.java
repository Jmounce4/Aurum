package BHML.aurum.runes.normal;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Focus implements Rune {

    @Override
    public Element getElement() {
        return Element.NORMAL;
    }

    @Override
    public String getId() {
        return "focus";
    }

    @Override
    public String getName() {
        return "Focus";
    }

    @Override
    public String getDescription() {
        return "Shoot further and more accurately. Crouch for more focus.";
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

package BHML.aurum.runes.lightning;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class ShockingEntry implements Rune {

    @Override
    public Element getElement() {
        return Element.LIGHTNING;
    }

    @Override
    public String getId() {
        return "shocking_entry";
    }

    @Override
    public String getName() {
        return "Shocking Entry";
    }

    @Override
    public String getDescription() {
        return "Your first attack on a target does bonus damage. Can be activated again if the user has not attacked the target for 5 seconds.";
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
        return "sword";
    }
}

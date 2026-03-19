package BHML.aurum.runes.fire;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class FiredUp implements Rune {

    @Override
    public Element getElement() {
        return Element.FIRE;
    }

    @Override
    public String getId() {
        return "fired_up";
    }

    @Override
    public String getName() {
        return "Fired Up";
    }

    @Override
    public String getDescription() {
        return "Landing consecutive attacks fires you up. Increase damage and deal AoE when Fired Up.";
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

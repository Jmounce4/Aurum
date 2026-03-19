package BHML.aurum.runes.air;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class Acrobat implements Rune {

    @Override
    public Element getElement() {
        return Element.AIR;
    }

    @Override
    public String getId() {
        return "acrobat";
    }

    @Override
    public String getName() {
        return "Acrobat";
    }

    @Override
    public String getDescription() {
        return "Attacking in the air gives unique effects. Jump attacks knock up, falling attacks strike down, crouching attacks thrust with knockback.";
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

package BHML.aurum.scrolls.core;

import BHML.aurum.elements.Element;
import org.bukkit.entity.Player;

public interface Scroll {

    Element getElement();
    String getId();
    String getName();
    int getMaxUses();
    String getDescription();
    int getGoldCost();
    int getRestoreAmount();
    int getCooldown();
    void cast(Player caster);



}

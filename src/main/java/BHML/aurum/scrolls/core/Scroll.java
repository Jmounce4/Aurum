package BHML.aurum.scrolls.core;

import BHML.aurum.elements.Element;
import org.bukkit.entity.Player;

public interface Scroll {

    Element getElement();
    String getId();
    int getMaxUses();
    String getDescription();
    int getGoldCost();
    int getRestoreAmount();
    void cast(Player caster);



}

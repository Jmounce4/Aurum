package BHML.aurum.runes.normal;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Conqueror implements Rune {

    @Override
    public Element getElement() {
        return Element.NORMAL;
    }

    @Override
    public String getId() {
        return "CONQUEROR";
    }

    @Override
    public String getName() {
        return "Conqueror";
    }

    @Override
    public String getDescription() {
        return "Defeating mobs has a chance to conquer them as subjugates.";
    }

    @Override
    public int getCooldown() {
        return 0;
    }

    @Override
    public String getDisplayItem() {
        return "gold_nugget";
    }

    @Override
    public String getItem() {
        return "sword";
    }
}

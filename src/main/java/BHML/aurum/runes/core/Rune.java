package BHML.aurum.runes.core;

import BHML.aurum.elements.Element;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Rune {

    // Many runes will be "Normal", but I wanted ability to make elements if needed.
    Element getElement();
    String getId();
    String getName();
    String getDescription();

    // Not necessary for all runes, but some may have a cooldown
    int getCooldown();

    // Gold nugget for normal/common, amethyst shard for elemental/rare, and MAYBE echo shard for ender/very rare
    String getDisplayItem();

    //This is for what item the rune works on "bow", "sword", "chestplate", "pickaxe"
    String getItem();




}

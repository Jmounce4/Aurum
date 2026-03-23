package BHML.aurum.runes.ender;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.Rune;

public class MonsterMiner implements Rune {
    
    @Override
    public Element getElement() {
        return Element.ENDER;
    }

    @Override
    public String getId() {
        return "monster_miner";
    }

    @Override
    public String getName() {
        return "Monster Miner";
    }

    @Override
    public String getDescription() {
        return "Mine and drop a mob spawner in the Overworld. This will break your pickaxe.";
    }

    @Override
    public int getCooldown() {
        return 0; // No cooldown - one-time use effect
    }

    @Override
    public String getDisplayItem() {
        return "amethyst_shard";
    }

    @Override
    public String getItem() {
        return "pickaxe";
    }
}

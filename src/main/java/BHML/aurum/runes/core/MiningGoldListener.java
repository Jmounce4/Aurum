package BHML.aurum.runes.core;

import BHML.aurum.runes.core.RuneRegistry;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Random;

public class MiningGoldListener implements Listener {

    private static final Random RANDOM = new Random();
    private static final int CHANCE = 64; // 1/64 chance

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if the broken block is gold ore
        if (event.getBlock().getType() != Material.GOLD_ORE && 
            event.getBlock().getType() != Material.DEEPSLATE_GOLD_ORE) {
            return;
        }

        // Check if the drop was actually gold (not silk touch)
        boolean droppedGold = false;
        for (ItemStack drop : event.getBlock().getDrops()) {
            if (drop.getType() == Material.GOLD_INGOT) {
                droppedGold = true;
                break;
            }
        }

        // If no gold was dropped (silk touch or other), don't proceed
        if (!droppedGold) {
            return;
        }

        // 1/64 chance to drop a rune
        if (RANDOM.nextInt(CHANCE) != 0) {
            return;
        }

        // Get a random rune
        Collection<Rune> allRunes = RuneRegistry.getAllRunes();
        if (allRunes.isEmpty()) {
            return;
        }

        Rune randomRune = allRunes.toArray(new Rune[0])[RANDOM.nextInt(allRunes.size())];
        if (randomRune == null) {
            return;
        }

        // Create the rune item using the reusable method
        ItemStack runeItem = RuneUtils.createRuneItem(randomRune);

        // Drop the rune at the block location
        event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), runeItem);
    }
}

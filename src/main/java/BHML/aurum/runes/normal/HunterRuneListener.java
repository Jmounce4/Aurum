package BHML.aurum.runes.normal;

import BHML.aurum.runes.core.RuneUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class HunterRuneListener implements Listener {

    private static final double XP_BONUS_MULTIPLIER = 1.5; // 50% more XP
    private static final int LOOTING_LEVEL = 2; // Looting 2 equivalent

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Check if the killer is a player
        if (!(event.getEntity().getKiller() instanceof Player player)) {
            return;
        }

        // Check if the player is holding a bow with the Hunter rune
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!RuneUtils.hasRune(weapon, new Hunter())) {
            return;
        }

        // Apply XP bonus (50% more)
        int originalXP = event.getDroppedExp();
        int bonusXP = (int) (1+(originalXP * XP_BONUS_MULTIPLIER));
        event.setDroppedExp(bonusXP);

        // Apply loot bonus (equivalent to Looting 2)
        // Minecraft already handles looting enchantment, but we need to simulate it
        // Since we can't directly add looting to the bow, we'll manually increase drops
        applyLootingBonus(event);

        // Send feedback to player
        player.sendMessage("§a[Hunter] +" + (bonusXP - originalXP) + " XP | Looting " + LOOTING_LEVEL + " bonus applied!");
    }

    private void applyLootingBonus(EntityDeathEvent event) {
        // For common drops that benefit from looting, we'll add extra drops
        // This simulates the Looting 2 enchantment effect
        
        // Create a copy of the drops to avoid ConcurrentModificationException
        java.util.List<ItemStack> originalDrops = new java.util.ArrayList<>(event.getDrops());
        
        for (int i = 0; i < LOOTING_LEVEL; i++) {
            // 50% chance for each looting level to drop extra items
            if (Math.random() < 0.5) {
                for (ItemStack drop : originalDrops) {
                    // Clone the drop and add it to the drops list
                    ItemStack bonusDrop = drop.clone();
                    event.getDrops().add(bonusDrop);
                }
            }
        }
    }
}

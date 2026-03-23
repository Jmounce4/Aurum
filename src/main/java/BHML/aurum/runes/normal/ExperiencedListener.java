package BHML.aurum.runes.normal;

import BHML.aurum.runes.core.RuneUtils;
import BHML.aurum.runes.core.RuneRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Handles the Experienced rune functionality:
 * - Grants bonus damage based on player's XP level
 * - 0.05 damage per XP level
 * - Only applies when holding sword with Experienced rune
 */
public class ExperiencedListener implements Listener {
    
    private static final double DAMAGE_PER_XP_LEVEL = 0.05;
    
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        
        // Check if player has Experienced rune
        if (!hasExperiencedRune(player)) return;
        
        // Calculate damage bonus based on XP level
        int xpLevel = player.getLevel();
        if (xpLevel <= 0) return;
        
        double damageBonus = xpLevel * DAMAGE_PER_XP_LEVEL;
        event.setDamage(event.getDamage() + damageBonus);
    }
    
    /**
     * Checks if player has Experienced rune in their main hand
     */
    private boolean hasExperiencedRune(Player player) {
        return RuneUtils.hasRune(player.getInventory().getItemInMainHand(), RuneRegistry.getRune("experienced"));
    }
}

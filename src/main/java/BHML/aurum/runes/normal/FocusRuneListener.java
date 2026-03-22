package BHML.aurum.runes.normal;

import BHML.aurum.runes.core.RuneUtils;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

public class FocusRuneListener implements Listener {

    // Focus rune values - standing
    private static final double STANDING_SPEED = 4.0;
    private static final double STANDING_SPREAD_REDUCTION = 0.4;
    
    // Focus rune values - crouching
    private static final double CROUCHING_SPEED = 4.5;
    private static final double CROUCHING_SPREAD_REDUCTION = 0.6;

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Arrow arrow)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        // Check if the bow has the Focus rune
        if (!RuneUtils.hasRune(player.getInventory().getItemInMainHand(), new Focus())) {
            return;
        }

        // Check bow pullback amount (0.0 - 1.0)
        double pullback = event.getForce();
        
        // Determine values based on whether player is crouching
        double speed, spreadReduction;
        if (player.isSneaking()) {
            speed = CROUCHING_SPEED;
            spreadReduction = CROUCHING_SPREAD_REDUCTION;
        } else {
            speed = STANDING_SPEED;
            spreadReduction = STANDING_SPREAD_REDUCTION;
        }

        // Apply hybrid approach (Method 4 from BowListener)
        Vector velocity = arrow.getVelocity().clone().normalize();
        velocity.multiply(speed * pullback);
        
        // Apply proper spread reduction using the same fixed method
        Vector intendedDirection = player.getEyeLocation().getDirection().normalize();
        Vector correctedVel = velocity.clone().multiply(1.0 - spreadReduction)
                               .add(intendedDirection.multiply(velocity.length() * spreadReduction));
        
        arrow.setVelocity(correctedVel);
        
        // Send feedback to player
        String status = player.isSneaking() ? "Crouching" : "Standing";
        player.sendMessage("§a[Focus] " + status + " - Speed: " + String.format("%.1f", speed * pullback) + 
                          " | Accuracy: " + String.format("%.0f", spreadReduction * 100) + "%");
    }
}

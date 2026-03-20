package BHML.aurum.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

public class BowListener implements Listener {

    // ========== TUNABLE VALUES ==========
    // Method 1: Velocity Modification
    private static final double VELOCITY_MULTIPLIER = 10.0; // Speed multiplier for velocity normalization (3.0 is around normal)
    private static final boolean ENABLE_VELOCITY_MODIFICATION = false;
    
    // Method 2: Spread Reduction
    private static final double SPREAD_REDUCTION_FACTOR = 0.8; // 0.0 = no reduction, 1.0 = perfect accuracy
    private static final boolean ENABLE_SPREAD_REDUCTION = false;
    
    // Method 3: Manual Arrow (Full Control)
    private static final double MANUAL_ARROW_SPEED = 6.0; // Speed for manually launched arrows
    private static final boolean ENABLE_MANUAL_ARROW = false;
    
    // Method 4: Hybrid Approach (Velocity + Spread)
    private static final double HYBRID_SPEED = 5.0; // Speed for hybrid approach
    private static final double HYBRID_SPREAD_REDUCTION = 0.6; // Spread reduction for hybrid
    private static final boolean ENABLE_HYBRID = true; // Set to true to test hybrid method
    // ====================================

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Arrow arrow)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        // Check bow pullback amount (0.0 - 1.0)
        double pullback = event.getForce();
        
        // ========== METHOD 1: VELOCITY MODIFICATION ==========
        if (ENABLE_VELOCITY_MODIFICATION) {
            Vector velocity = arrow.getVelocity().clone().normalize();
            velocity.multiply(VELOCITY_MULTIPLIER * pullback); // Scale with pullback
            arrow.setVelocity(velocity);
            player.sendMessage("§a[Velocity Mod] Speed: " + String.format("%.1f", VELOCITY_MULTIPLIER * pullback));
        }
        
        // ========== METHOD 2: SPREAD REDUCTION ==========
        else if (ENABLE_SPREAD_REDUCTION) {
            Vector currentVel = arrow.getVelocity();
            
            // Get the player's intended direction (where they're looking)
            Vector intendedDirection = player.getEyeLocation().getDirection().normalize();
            
            // Blend current velocity with intended direction to reduce spread
            // Higher SPREAD_REDUCTION_FACTOR = more accuracy (1.0 = perfect accuracy)
            Vector correctedVel = currentVel.clone().multiply(1.0 - SPREAD_REDUCTION_FACTOR)
                                   .add(intendedDirection.multiply(currentVel.length() * SPREAD_REDUCTION_FACTOR));
            
            arrow.setVelocity(correctedVel);
            player.sendMessage("§a[Spread Reduction] Accuracy: " + String.format("%.0f", SPREAD_REDUCTION_FACTOR * 100) + "%");
        }
        
        // ========== METHOD 3: MANUAL ARROW (FULL CONTROL) ==========
        else if (ENABLE_MANUAL_ARROW) {
            // Cancel default velocity
            event.setCancelled(true);
            
            // Launch new arrow with no spread
            Arrow straightArrow = player.launchProjectile(Arrow.class);
            
            // Set exact straight velocity
            Vector direction = player.getEyeLocation().getDirection().normalize();
            straightArrow.setVelocity(direction.multiply(MANUAL_ARROW_SPEED * pullback));
            
            straightArrow.setCritical(pullback >= 1.0); // Critical only on full draw
            straightArrow.setGravity(true);
            
            player.sendMessage("§a[Manual Arrow] Speed: " + String.format("%.1f", MANUAL_ARROW_SPEED * pullback));
        }
        
        // ========== METHOD 4: HYBRID APPROACH ==========
        else if (ENABLE_HYBRID) {
            Vector velocity = arrow.getVelocity().clone().normalize();
            velocity.multiply(HYBRID_SPEED * pullback);
            
            // Apply proper spread reduction using the same fixed method
            Vector intendedDirection = player.getEyeLocation().getDirection().normalize();
            Vector correctedVel = velocity.clone().multiply(1.0 - HYBRID_SPREAD_REDUCTION)
                                   .add(intendedDirection.multiply(velocity.length() * HYBRID_SPREAD_REDUCTION));
            
            arrow.setVelocity(correctedVel);
            player.sendMessage("§a[Hybrid] Speed: " + String.format("%.1f", HYBRID_SPEED * pullback) + " | Accuracy: " + String.format("%.0f", HYBRID_SPREAD_REDUCTION * 100) + "%");
        }
        
        // ========== DEFAULT: NO ACCURACY MODIFICATIONS ==========
        else {
            player.sendMessage("§7[Default] No accuracy modifications applied");
        }
    }

}

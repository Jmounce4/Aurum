package BHML.aurum.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

public class BowListener implements Listener {

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Arrow arrow)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        //NOTE FOR LATER: Implement with runes. ALSO IMPORTANT, check for bow pullback amount!

        // Cancel default velocity
        event.setCancelled(true);

        // Launch a new arrow with no spread
        Arrow straightArrow = player.launchProjectile(Arrow.class);

        // Set exact straight velocity (multiplier = speed)
        Vector direction = player.getEyeLocation().getDirection().normalize();
        straightArrow.setVelocity(direction.multiply(6)); // 6 = speed factor

        straightArrow.setCritical(true);

        // Optional: keep gravity or disable
        straightArrow.setGravity(true);
    }

}

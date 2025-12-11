package BHML.aurum.runes.normal;

import BHML.aurum.runes.core.RuneUtils;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SniperRuneListener implements Listener {

    private final Map<UUID, Location> starts = new HashMap<>();

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getProjectile() instanceof Arrow arrow)) return;

        ItemStack bow = event.getBow();
        if (bow == null) return;

        if (!RuneUtils.hasRune(bow, new Sniper())) return;

        starts.put(arrow.getUniqueId(), arrow.getLocation());
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Arrow arrow)) return;

        Location start = starts.remove(arrow.getUniqueId());
        if (start == null) return;

        double distance = start.distance(arrow.getLocation());
        double bonus = Math.floor(distance / 10.0);

        if (bonus > 0) {
            event.setDamage(event.getDamage() + bonus);
        }
    }

}

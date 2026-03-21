package BHML.aurum.runes.lightning;

import BHML.aurum.Aurum;
import BHML.aurum.runes.core.RuneUtils;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ShockingEntryListener implements Listener {

    private final Map<UUID, Map<UUID, Long>> playerTargetCooldowns = new HashMap<>();
    private static final double BONUS_DAMAGE = 10.0;
    private static final long COOLDOWN_TIME = 5000; // 5 seconds in milliseconds

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;

        if (!RuneUtils.hasRune(weapon, new ShockingEntry())) return;

        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Get or create the player's target cooldown map
        Map<UUID, Long> targetCooldowns = playerTargetCooldowns.computeIfAbsent(playerId, k -> new HashMap<>());

        long currentTime = System.currentTimeMillis();
        Long lastAttackTime = targetCooldowns.get(targetId);

        // Check if this is the first attack or if cooldown has expired
        if (lastAttackTime == null || (currentTime - lastAttackTime) >= COOLDOWN_TIME) {
            // Apply bonus damage
            event.setDamage(event.getDamage() + BONUS_DAMAGE);
            
            // Update the last attack time for this target
            targetCooldowns.put(targetId, currentTime);
            
            // Show visual and sound effects
            createShockingEffect(target);
            player.sendMessage(ChatColor.YELLOW + "Shocking Entry! +" + BONUS_DAMAGE + " damage!");
            player.playSound(target.getLocation(), Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.8f, 1.8f);
        }
    }

    private void createShockingEffect(LivingEntity target) {
        Location targetLoc = target.getLocation();
        World world = targetLoc.getWorld();
        Random rand = new Random();
        
        // Create 3 electrical rings at different heights
        double[] heights = {0.3, 1.0, 1.7}; // legs, torso, head
        
        for (double baseHeight : heights) {
            for (int i = 0; i < 27; i++) {
                double angle = (double) i / 16 * Math.PI * 2;
                double radius = 0.4;
                
                // Add vertical jitter to each particle
                double verticalJitter = (rand.nextDouble() - 0.5) * 0.9;
                double height = baseHeight + verticalJitter;
                
                // Add small horizontal jitter for electrical effect
                double horizontalJitter = (rand.nextDouble() - 0.5) * 0.1;
                double jitterRadius = radius + horizontalJitter;
                
                double x = Math.cos(angle) * jitterRadius;
                double z = Math.sin(angle) * jitterRadius;
                
                Location sparkLoc = targetLoc.clone().add(x, height, z);
                world.spawnParticle(Particle.ELECTRIC_SPARK, sparkLoc, 1, 0, 0, 0, 0);
            }
        }
        
        // Quick sound effect
        world.playSound(targetLoc, Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.6f, 1.8f);
    }
}

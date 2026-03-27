package BHML.aurum.runes.lightning;

import BHML.aurum.Aurum;
import BHML.aurum.runes.core.RuneUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ConductiveListener implements Listener {

    private final Map<UUID, BukkitRunnable> particleTasks = new HashMap<>();
    private static final int SEARCH_RADIUS = 30;
    private static final int PARTICLE_INTERVAL = 100; // ticks (5 seconds)
    private static final Material GOLD_ORE = Material.GOLD_ORE;
    private static final Material DEEPSLATE_GOLD_ORE = Material.DEEPSLATE_GOLD_ORE;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !RuneUtils.hasRune(item, new Conductive())) {
            stopParticleTask(playerId);
            return;
        }

        // Start particle task if not already running
        if (!particleTasks.containsKey(playerId)) {
            startParticleTask(player);
        }
    }

    private void startParticleTask(Player player) {
        UUID playerId = player.getUniqueId();
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player still has the rune
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || !RuneUtils.hasRune(item, new Conductive())) {
                    this.cancel();
                    particleTasks.remove(playerId);
                    return;
                }

                // Find nearest gold ore
                Location nearestOre = findNearestGoldOre(player.getLocation());
                if (nearestOre != null) {
                    Location startPos = player.getLocation().clone().add(0, 1.0, 0);
                    createElectricTrail(startPos, nearestOre);
                }
            }
        };
        
        task.runTaskTimer(Aurum.getPlugin(Aurum.class), 0, PARTICLE_INTERVAL);
        particleTasks.put(playerId, task);
    }

    private void stopParticleTask(UUID playerId) {
        BukkitRunnable task = particleTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    private Location findNearestGoldOre(Location playerLoc) {
        World world = playerLoc.getWorld();
        Location nearest = null;
        double minDistance = Double.MAX_VALUE;

        int minX = playerLoc.getBlockX() - SEARCH_RADIUS;
        int maxX = playerLoc.getBlockX() + SEARCH_RADIUS;
        int minY = Math.max(world.getMinHeight(), playerLoc.getBlockY() - SEARCH_RADIUS);
        int maxY = Math.min(world.getMaxHeight(), playerLoc.getBlockY() + SEARCH_RADIUS);
        int minZ = playerLoc.getBlockZ() - SEARCH_RADIUS;
        int maxZ = playerLoc.getBlockZ() + SEARCH_RADIUS;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location blockLoc = new Location(world, x, y, z);
                    Material block = blockLoc.getBlock().getType();
                    
                    if (block == GOLD_ORE || block == DEEPSLATE_GOLD_ORE) {
                        double distance = playerLoc.distanceSquared(blockLoc);
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearest = blockLoc.clone().add(0.5, 0.5, 0.5); // Center of block
                        }
                    }
                }
            }
        }

        return nearest;
    }

    private void createElectricTrail(Location start, Location end) {
        World world = start.getWorld();
        Random rand = new Random();
        
        // Calculate distance and number of particles (1 per block)
        double distance = start.distance(end);
        int particleCount = (int) Math.ceil(distance);
        
        Vector direction = end.clone().subtract(start).toVector().normalize();
        
        for (int i = 0; i <= particleCount; i++) {
            // Calculate position along the path
            double progress = (double) i / particleCount;
            Location currentPos = start.clone().add(direction.clone().multiply(distance * progress));
            
            // Add jitter for lightning effect
            double jitterX = (rand.nextDouble() - 0.5) * 0.8;
            double jitterY = (rand.nextDouble() - 0.5) * 0.8;
            double jitterZ = (rand.nextDouble() - 0.5) * 0.8;
            
            Location particleLoc = currentPos.add(jitterX, jitterY, jitterZ);
            
            // Spawn electric spark particle
            world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0, 0, 0, 0);
        }
        
        // Play subtle sound at the target location
        world.playSound(end, Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.3f, 2.0f);
    }
}

package BHML.aurum.runes.normal;

import BHML.aurum.Aurum;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SubjugateManager {

    private static final int FOLLOW_RANGE = 20;

    public void manageSubjugate(LivingEntity subjugate, Player conqueror) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (subjugate.isDead() || !subjugate.isValid()) {
                    this.cancel();
                    return;
                }
                
                // Always clear inappropriate targets first
                if (subjugate instanceof Mob mob) {
                    LivingEntity currentTarget = mob.getTarget();
                    if (currentTarget != null && !isValidTarget(currentTarget, subjugate, conqueror)) {
                        mob.setTarget(null);
                    }
                }
                
                // Handle follow behavior
                handleFollowBehavior(subjugate, conqueror);
                
                // Handle combat
                LivingEntity target = findValidTarget(subjugate, conqueror);
                if (target != null) {
                    attackTarget(subjugate, target);
                } else {
                    // Clear target if no valid targets to prevent attacking friends
                    if (subjugate instanceof Mob mob) {
                        mob.setTarget(null);
                    }
                }
            }
        }.runTaskTimer(Aurum.getPlugin(Aurum.class), 5L, 5L); // Check every 5 ticks (0.25 seconds) for better control
    }
    
    private void handleFollowBehavior(LivingEntity subjugate, Player conqueror) {
        double distance = subjugate.getLocation().distance(conqueror.getLocation());
        
        // If too far away, move towards conqueror
        if (distance > FOLLOW_RANGE) {
            moveTowards(subjugate, conqueror.getLocation());
        }
    }
    
    private LivingEntity findValidTarget(LivingEntity subjugate, Player conqueror) {
        LivingEntity bestTarget = null;
        double bestDistance = Double.MAX_VALUE;
        
        for (Entity entity : subjugate.getNearbyEntities(16, 16, 16)) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (entity == subjugate) continue;
            
            // Check if valid target
            if (!isValidTarget(livingEntity, subjugate, conqueror)) continue;
            
            double distance = subjugate.getLocation().distance(livingEntity.getLocation());
            if (distance < bestDistance) {
                bestDistance = distance;
                bestTarget = livingEntity;
            }
        }
        
        return bestTarget;
    }
    
    private boolean isValidTarget(LivingEntity potentialTarget, LivingEntity subjugate, Player conqueror) {
        // NEVER target the owner (if conqueror is provided)
        if (conqueror != null && potentialTarget == conqueror) return false;
        
        // NEVER target other subjugates
        if (isSubjugate(potentialTarget)) return false;
        
        // NEVER target players
        if (potentialTarget instanceof Player) return false;
        
        // Don't attack creepers (to avoid griefing)
        if (potentialTarget.getType() == EntityType.CREEPER) return false;
        
        // Don't attack tameable animals that are tamed
        if (potentialTarget instanceof Tameable tameable && tameable.isTamed()) return false;
        
        // Don't attack neutral mobs (spiders during day, etc.)
        if (isNeutralMob(potentialTarget)) return false;
        
        // Attack hostile mobs
        return isHostileMob(potentialTarget);
    }
    
    private void attackTarget(LivingEntity attacker, LivingEntity target) {
        if (attacker instanceof Mob mob && target != null && !target.isDead()) {
            // Double-check that this is still a valid target
            if (isValidTarget(target, attacker, null)) { // We don't have conqueror here, so pass null
                mob.setTarget(target);
            } else {
                mob.setTarget(null);
            }
        }
    }
    
    private void moveTowards(LivingEntity entity, Location target) {
        if (entity instanceof Mob mob) {
            // Clear current target to prevent attacking while moving
            mob.setTarget(null);
            
            // Use Bukkit's pathfinding instead of teleport
            mob.getPathfinder().moveTo(target, 1.0); // Speed multiplier
        }
    }
    
    private boolean isSubjugate(LivingEntity entity) {
        // Check if entity has the subjugate marker
        PersistentDataContainer container = entity.getPersistentDataContainer();
        return container.has(new NamespacedKey(Aurum.getPlugin(Aurum.class), "conqueror_subjugate"), PersistentDataType.STRING);
    }
    
    private boolean isHostileMob(LivingEntity entity) {
        String entityType = entity.getType().name();
        return entityType.contains("ZOMBIE") || 
               entityType.contains("SKELETON") || 
               entityType.contains("PILLAGER") || 
               entityType.contains("VINDICATOR") ||
               entityType.contains("EVOKER") ||
               entityType.contains("WITCH") ||
               entityType.equals("HUSK") ||
               entityType.equals("DROWNED") ||
               entityType.equals("STRAY") ||
               entityType.equals("BOGGED");
    }
    
    private boolean isNeutralMob(LivingEntity entity) {
        String entityType = entity.getType().name();
        if (entityType.equals("SPIDER") || entityType.equals("CAVE_SPIDER")) {
            long time = entity.getWorld().getTime();
            return time >= 0 && time < 13000; // Daytime
        }
        
        return entityType.equals("WOLF") ||
               entityType.equals("IRON_GOLEM") ||
               entityType.equals("SNOW_GOLEM") ||
               entityType.equals("LLAMA") ||
               entityType.equals("TRADER_LLAMA") ||
               entityType.equals("POLAR_BEAR") ||
               entityType.equals("DOLPHIN") ||
               entityType.equals("PANDA");
    }
}

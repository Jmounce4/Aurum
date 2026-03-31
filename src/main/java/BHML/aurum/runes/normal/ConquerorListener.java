package BHML.aurum.runes.normal;

import BHML.aurum.Aurum;
import BHML.aurum.runes.core.RuneUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ConquerorListener implements Listener {

    private static final double CONQUEST_CHANCE = 1.0; // 25% for testing (change to 0.01 for production)
    private static final String SUBJUGATE_KEY = "conqueror_subjugate";
    private static final String CONQUEROR_KEY = "conqueror_owner";
    
    private final Map<UUID, Set<UUID>> playerSubjugates = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Wait a moment for the world to fully load before checking for subjugates
        new BukkitRunnable() {
            @Override
            public void run() {
                reconnectExistingSubjugates(player);
            }
        }.runTaskLater(Aurum.getPlugin(Aurum.class), 20L); // 1 second delay
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        if (killer == null) return;
        
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null || !RuneUtils.hasRune(weapon, new Conqueror())) return;
        
        // Check if entity is a conquerable mob
        if (!isConquerableMob(entity)) return;
        
        // Roll for conquest
        if (Math.random() < CONQUEST_CHANCE) {
            conquerMob(killer, entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Prevent subjugates from damaging their owner in ANY way
        Entity damager = event.getDamager();
        LivingEntity actualDamager = null;
        
        // Handle projectiles (arrows, etc.)
        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof LivingEntity shooter) {
                actualDamager = shooter;
            }
        } else if (damager instanceof LivingEntity) {
            actualDamager = (LivingEntity) damager;
        }
        
        if (actualDamager != null && event.getEntity() instanceof Player victim && isSubjugate(actualDamager)) {
            PersistentDataContainer container = actualDamager.getPersistentDataContainer();
            String ownerId = container.get(new NamespacedKey(Aurum.getPlugin(Aurum.class), CONQUEROR_KEY), PersistentDataType.STRING);
            if (ownerId != null && ownerId.equals(victim.getUniqueId().toString())) {
                event.setCancelled(true);
                actualDamager.sendMessage(ChatColor.RED + "You cannot attack your owner!");
                return;
            }
        }
    }

    private boolean isConquerableMob(LivingEntity entity) {
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

    private void conquerMob(Player conqueror, LivingEntity deadEntity) {
        Location spawnLocation = deadEntity.getLocation();
        EntityType mobType = deadEntity.getType();
        
        // Spawn new mob of the same type
        LivingEntity newSubjugate = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, mobType);
        
        // Disable default AI behavior
        if (newSubjugate instanceof Mob mob) {
            mob.setAI(false);
            // Re-enable AI but with controlled behavior
            mob.setAI(true);
        }
        
        // Set health to half
        newSubjugate.setHealth(newSubjugate.getMaxHealth() / 2);
        newSubjugate.setPersistent(true);
        
        // Copy equipment from dead mob if possible
        if (deadEntity.getEquipment() != null) {
            newSubjugate.getEquipment().setArmorContents(deadEntity.getEquipment().getArmorContents());
            newSubjugate.getEquipment().setItemInMainHand(deadEntity.getEquipment().getItemInMainHand());
            newSubjugate.getEquipment().setItemInOffHand(deadEntity.getEquipment().getItemInOffHand());
        }
        
        // Set subjugate metadata
        PersistentDataContainer container = newSubjugate.getPersistentDataContainer();
        container.set(new NamespacedKey(Aurum.getPlugin(Aurum.class), SUBJUGATE_KEY), PersistentDataType.STRING, "true");
        container.set(new NamespacedKey(Aurum.getPlugin(Aurum.class), CONQUEROR_KEY), PersistentDataType.STRING, conqueror.getUniqueId().toString());
        
        // Set custom name (normal visibility - only shows when looking at them)
        newSubjugate.setCustomName(ChatColor.GOLD + conqueror.getName() + "'s Subjugate");
        
        // Add to player's subjugates
        playerSubjugates.computeIfAbsent(conqueror.getUniqueId(), k -> new HashSet<>()).add(newSubjugate.getUniqueId());
        
        // Start AI management
        SubjugateManager subjugateManager = new SubjugateManager();
        subjugateManager.manageSubjugate(newSubjugate, conqueror);
        
        conqueror.sendMessage(ChatColor.GOLD + "You have conquered a " + mobType.name() + "!");
        conqueror.playSound(conqueror.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    private void reconnectExistingSubjugates(Player player) {
        String playerUUID = player.getUniqueId().toString();
        
        // Search all loaded worlds for this player's subjugates
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity livingEntity)) continue;
                
                // Check if this entity is a subjugate owned by this player
                if (isSubjugateOwnedByPlayer(livingEntity, playerUUID)) {
                    // Restart AI management for this subjugate
                    SubjugateManager subjugateManager = new SubjugateManager();
                    subjugateManager.manageSubjugate(livingEntity, player);
                    
                    // Add to tracking
                    playerSubjugates.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(livingEntity.getUniqueId());
                    
                    // Update custom name to current player name (in case they changed it)
                    livingEntity.setCustomName(ChatColor.GOLD + player.getName() + "'s Subjugate");
                }
            }
        }
    }

    private boolean isSubjugateOwnedByPlayer(LivingEntity entity, String playerUUID) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        
        // Check if it's a subjugate
        if (!container.has(new NamespacedKey(Aurum.getPlugin(Aurum.class), SUBJUGATE_KEY), PersistentDataType.STRING)) {
            return false;
        }
        
        // Check if owned by this player
        String ownerId = container.get(new NamespacedKey(Aurum.getPlugin(Aurum.class), CONQUEROR_KEY), PersistentDataType.STRING);
        return playerUUID.equals(ownerId);
    }

    private boolean isSubjugate(LivingEntity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        return container.has(new NamespacedKey(Aurum.getPlugin(Aurum.class), SUBJUGATE_KEY), PersistentDataType.STRING);
    }
}

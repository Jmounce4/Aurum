package BHML.aurum.runes.earth;

import BHML.aurum.Aurum;
import BHML.aurum.runes.core.RuneUtils;
import BHML.aurum.runes.core.RuneRegistry;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the Absorb rune functionality:
 * - Tracks absorb stacks per player
 * - Manages stack expiration (10 minutes)
 * - Applies health bonuses (0.5 hearts per stack)
 * - Applies damage bonuses (0.25 damage per stack)
 * - Refreshes oldest stack when at max capacity
 */
public class AbsorbListener implements Listener {
    
    private static final double HEALTH_PER_STACK = 1.0; // 0.5 hearts = 1.0 health points
    private static final double DAMAGE_PER_STACK = 0.25;
    private static final int MAX_STACKS = 10;
    private static final long STACK_DURATION_MS = 10 * 60 * 1000; // 10 minutes in milliseconds
    
    private final JavaPlugin plugin;
    private final Map<UUID, AbsorbData> playerStacks = new ConcurrentHashMap<>();
    
    public AbsorbListener(JavaPlugin plugin) {
        this.plugin = plugin;
        startExpirationTask();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        if (killer == null) return;
        
        // Check if killer has Absorb rune
        if (!hasAbsorbRune(killer)) return;
        
        // Don't count player kills as absorbing
        if (entity instanceof Player) return;
        
        addAbsorbStack(killer);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load existing stacks from storage if needed
        // For now, we'll start fresh when players join
        Player player = event.getPlayer();
        if (!playerStacks.containsKey(player.getUniqueId())) {
            playerStacks.put(player.getUniqueId(), new AbsorbData());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up data when player leaves
        UUID playerId = event.getPlayer().getUniqueId();
        AbsorbData data = playerStacks.remove(playerId);
        if (data != null) {
            // Reset player attributes to normal
            resetPlayerAttributes(event.getPlayer());
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        AbsorbData data = playerStacks.get(player.getUniqueId());
        
        if (data == null || data.getCurrentStacks() == 0) return;
        
        // Only apply damage bonus when holding sword with Absorb rune
        if (!hasAbsorbRune(player)) return;
        
        // Apply damage bonus
        double damageBonus = data.getCurrentStacks() * DAMAGE_PER_STACK;
        event.setDamage(event.getDamage() + damageBonus);
    }
    
    /**
     * Adds a new absorb stack to the player
     */
    private void addAbsorbStack(Player player) {
        UUID playerId = player.getUniqueId();
        AbsorbData data = playerStacks.computeIfAbsent(playerId, k -> new AbsorbData());
        
        long currentTime = System.currentTimeMillis();
        
        if (data.getCurrentStacks() >= MAX_STACKS) {
            // At max stacks - remove oldest and add new one
            data.removeOldestStack();
            data.addStack(currentTime);
        } else {
            // Add new stack
            data.addStack(currentTime);
        }
        
        // Apply attribute changes
        updatePlayerAttributes(player);
        
        // Give instant health bonus
        double healthBonus = HEALTH_PER_STACK;
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();
        player.setHealth(Math.min(currentHealth + healthBonus, maxHealth));
    }
    
    /**
     * Updates player attributes based on current stacks
     */
    private void updatePlayerAttributes(Player player) {
        AbsorbData data = playerStacks.get(player.getUniqueId());
        if (data == null) return;
        
        int stacks = data.getCurrentStacks();
        
        // Update max health
        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            double baseMaxHealth = 20.0; // Base max health
            double healthBonus = stacks * HEALTH_PER_STACK;
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseMaxHealth + healthBonus);
        }
    }
    
    /**
     * Resets player attributes to normal values
     */
    private void resetPlayerAttributes(Player player) {
        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
        }
    }
    
    /**
     * Checks if player has Absorb rune in their main hand
     */
    private boolean hasAbsorbRune(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return RuneUtils.hasRune(item, RuneRegistry.getRune("absorb"));
    }
    
    /**
     * Starts the task that checks for expired stacks
     */
    private void startExpirationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                boolean needsUpdate = false;
                
                for (Map.Entry<UUID, AbsorbData> entry : playerStacks.entrySet()) {
                    AbsorbData data = entry.getValue();
                    if (data.removeExpiredStacks(currentTime)) {
                        needsUpdate = true;
                        
                        // Update player attributes if they're online
                        Player player = Bukkit.getPlayer(entry.getKey());
                        if (player != null && player.isOnline()) {
                            updatePlayerAttributes(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }
    
    /**
     * Inner class to track absorb stacks for a player
     */
    private static class AbsorbData {
        private final Queue<Long> stackTimes = new LinkedList<>();
        
        public void addStack(long time) {
            stackTimes.offer(time);
        }
        
        public void removeOldestStack() {
            if (!stackTimes.isEmpty()) {
                stackTimes.poll();
            }
        }
        
        public boolean removeExpiredStacks(long currentTime) {
            boolean removed = false;
            while (!stackTimes.isEmpty() && (currentTime - stackTimes.peek()) > STACK_DURATION_MS) {
                stackTimes.poll();
                removed = true;
            }
            return removed;
        }
        
        public int getCurrentStacks() {
            return stackTimes.size();
        }
        
        public long getOldestStackTime() {
            return stackTimes.isEmpty() ? 0 : stackTimes.peek();
        }
    }
}

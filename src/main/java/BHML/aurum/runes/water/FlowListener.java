package BHML.aurum.runes.water;

import BHML.aurum.Aurum;
import BHML.aurum.runes.core.RuneUtils;
import BHML.aurum.runes.core.RuneRegistry;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the Flow rune functionality:
 * - Tracks flow stacks per player (max 20 stacks)
 * - Each stack gives 1% movement speed and attack speed
 * - Stacks are gained by hitting targets
 * - All stacks are lost when player takes damage
 * - Buffs only apply when holding the Flow sword
 */
public class FlowListener implements Listener {
    
    private static final int MAX_STACKS = 20;
    private static final double SPEED_PER_STACK = 0.01; // 1% per stack
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final String MOVEMENT_SPEED_NAME = "Flow Movement Speed";
    private static final String ATTACK_SPEED_NAME = "Flow Attack Speed";
    
    private final JavaPlugin plugin;
    private final ConcurrentHashMap<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
    
    public FlowListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        
        // Check if player has Flow rune
        if (!hasFlowRune(player)) return;
        
        // Don't count hitting players as building flow
        if (event.getEntity() instanceof Player) return;
        
        addFlowStack(player);
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        
        // Remove 10 flow stacks when player takes any damage
        UUID playerId = player.getUniqueId();
        int currentStacks = playerStacks.getOrDefault(playerId, 0);
        
        if (currentStacks > 0) {
            int newStacks = Math.max(0, currentStacks - 10);
            playerStacks.put(playerId, newStacks);
            updatePlayerBuffs(player);
            
            plugin.getLogger().info("Flow: " + player.getName() + " was hit, lost " + 
                (currentStacks - newStacks) + " stacks, now has " + newStacks);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Initialize with 0 stacks if not already present
        playerStacks.putIfAbsent(player.getUniqueId(), 0);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up data when player leaves
        UUID playerId = event.getPlayer().getUniqueId();
        playerStacks.remove(playerId);
        // Reset player attributes to normal
        resetPlayerAttributes(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        // Schedule the update to run after the item switch is complete
        Bukkit.getScheduler().runTask(plugin, () -> updatePlayerBuffs(player));
    }
    
    /**
     * Adds a flow stack to the player
     */
    private void addFlowStack(Player player) {
        UUID playerId = player.getUniqueId();
        int currentStacks = playerStacks.getOrDefault(playerId, 0);
        
        if (currentStacks < MAX_STACKS) {
            playerStacks.put(playerId, currentStacks + 1);
        }
        
        updatePlayerBuffs(player);
    }
    
    /**
     * Clears all flow stacks for the player
     */
    private void clearFlowStacks(Player player) {
        UUID playerId = player.getUniqueId();
        playerStacks.put(playerId, 0);
        updatePlayerBuffs(player);
    }
    
    /**
     * Updates player buffs based on current stacks and held item
     */
    private void updatePlayerBuffs(Player player) {
        UUID playerId = player.getUniqueId();
        int stacks = playerStacks.getOrDefault(playerId, 0);
        boolean hasFlowSword = hasFlowRune(player);
        
        // Debug logging (remove this after fixing)
        plugin.getLogger().info("Flow update: " + player.getName() + " stacks=" + stacks + " hasSword=" + hasFlowSword);
        
        // Always reset attributes first to ensure clean state
        resetPlayerAttributes(player);
        
        // Only apply buffs if holding Flow sword AND have stacks
        if (stacks > 0 && hasFlowSword) {
            applyFlowBuffs(player, stacks);
            plugin.getLogger().info("Flow applied: " + stacks + " stacks to " + player.getName());
        }
    }
    
    /**
     * Applies flow buffs to the player
     */
    private void applyFlowBuffs(Player player, int stacks) {
        double speedBonus = stacks * SPEED_PER_STACK;
        
        // Apply movement speed bonus
        if (player.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            AttributeModifier movementModifier = new AttributeModifier(
                MOVEMENT_SPEED_UUID,
                MOVEMENT_SPEED_NAME,
                speedBonus,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(movementModifier);
        }
        
        // Apply attack speed bonus
        if (player.getAttribute(Attribute.ATTACK_SPEED) != null) {
            AttributeModifier attackModifier = new AttributeModifier(
                ATTACK_SPEED_UUID,
                ATTACK_SPEED_NAME,
                speedBonus,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            player.getAttribute(Attribute.ATTACK_SPEED).addModifier(attackModifier);
        }
    }
    
    /**
     * Resets player attributes to normal values
     */
    private void resetPlayerAttributes(Player player) {
        // Remove movement speed modifier if it exists
        if (player.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            try {
                player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(MOVEMENT_SPEED_UUID);
            } catch (IllegalArgumentException e) {
                // Modifier doesn't exist, which is fine
            }
        }
        
        // Remove attack speed modifier if it exists
        if (player.getAttribute(Attribute.ATTACK_SPEED) != null) {
            try {
                player.getAttribute(Attribute.ATTACK_SPEED).removeModifier(ATTACK_SPEED_UUID);
            } catch (IllegalArgumentException e) {
                // Modifier doesn't exist, which is fine
            }
        }
    }
    
    /**
     * Checks if player has Flow rune in their main hand
     */
    private boolean hasFlowRune(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return RuneUtils.hasRune(item, RuneRegistry.getRune("flow"));
    }
    
    /**
     * Gets the current flow stacks for a player
     */
    public int getFlowStacks(Player player) {
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }
}

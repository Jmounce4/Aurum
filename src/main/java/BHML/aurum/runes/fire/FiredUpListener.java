package BHML.aurum.runes.fire;

import BHML.aurum.Aurum;
import BHML.aurum.runes.core.RuneUtils;
import BHML.aurum.scrolls.core.ScrollUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FiredUpListener implements Listener {

    private final Map<UUID, PlayerComboData> playerCombos = new HashMap<>();
    private final Map<UUID, Boolean> firedUpPlayers = new HashMap<>();
    private final Map<UUID, BukkitRunnable> particleTasks = new HashMap<>();

    private static class PlayerComboData {
        int consecutiveHits = 0;
        long lastHitTime = 0;
        long comboStartTime = 0;

        void reset() {
            consecutiveHits = 0;
            lastHitTime = 0;
            comboStartTime = 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;

        if (!RuneUtils.hasRune(weapon, new FiredUp())) return;
        
        // Filter out sweep attacks - only process direct sword hits
        if (isSweepAttack(player, weapon, event)) return;

        // Skip processing if this is spell damage (not a direct weapon hit)
        // Spell damage has the player as damager but no weapon in main hand or the damage doesn't match expected weapon damage
        double expectedWeaponDamage = getExpectedSwordDamage(weapon);
        if (Math.abs(event.getDamage() - expectedWeaponDamage) > 0.5) {
            // This is likely spell damage, not the original weapon hit
            return;
        }

        UUID playerId = player.getUniqueId();
        PlayerComboData comboData = playerCombos.computeIfAbsent(playerId, k -> new PlayerComboData());
        long currentTime = System.currentTimeMillis();

        // If player is already fired up, just reset timer and apply splash damage
        if (isFiredUp(playerId)) {
            resetFiredUpTimer(player);
            // Apply 30% bonus damage to main target
            double bonusDamage = event.getDamage() * 0.3;
            ScrollUtils.applySpellDamage(player, target, bonusDamage);
            player.sendMessage(ChatColor.BLUE + "Damage dealt to target: " + event.getDamage() + " + " + bonusDamage + " = " + (event.getDamage() + bonusDamage));
            //target.setFireTicks(60); // Brief fire effect
            // Apply splash damage to nearby targets
            applySplashDamage(player, target, event.getDamage());
            return;
        }

        // Check if this is a fully charged attack (swords don't have charge like bows, so we'll consider any attack)
        // Reset combo if too much time has passed since last hit
        if (currentTime - comboData.lastHitTime > 7000) {
            comboData.reset();
        }

        comboData.consecutiveHits++;
        comboData.lastHitTime = currentTime;

        if (comboData.consecutiveHits == 1) {
            comboData.comboStartTime = currentTime;
        }

        // Check if we've reached 3 consecutive hits within 7 seconds
        if (comboData.consecutiveHits >= 3 && (currentTime - comboData.comboStartTime) <= 7000) {
            activateFiredUp(player);
            comboData.reset(); // Reset combo after activation
        }
    }

    private void activateFiredUp(Player player) {
        UUID playerId = player.getUniqueId();
        
        // If already fired up, just restart the timer (don't show message again)
        boolean wasAlreadyFiredUp = firedUpPlayers.getOrDefault(playerId, false);
        
        // Cancel existing task first
        if (particleTasks.containsKey(playerId)) {
            particleTasks.get(playerId).cancel();
            particleTasks.remove(playerId);
        }
        
        firedUpPlayers.put(playerId, true);

        // Start particle effects
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = (7 * 20) / 3; // 7 seconds divided by 3-tick intervals

            @Override
            public void run() {
                if (!firedUpPlayers.getOrDefault(playerId, false) || ticks >= maxTicks) {
                    firedUpPlayers.put(playerId, false);
                    particleTasks.remove(playerId);
                    player.sendMessage(ChatColor.RED + "Fired Up Expired");
                    cancel();
                    return;
                }

                // Emit wild fire particles from player body
                Location loc = player.getLocation().add(0, 0.9, 0);
                World world = player.getWorld();
                
                for (int i = 0; i < 1; i++) {
                    // Emit from player's body core with small random offsets
                    double offsetX = (Math.random() - 0.5) * 0.3; // ±0.15 blocks
                    double offsetZ = (Math.random() - 0.5) * 0.3; // ±0.15 blocks
                    double offsetY = Math.random() * 0.8; // 0 to 0.8 blocks up
                    
                    world.spawnParticle(Particle.FLAME, loc.clone().add(offsetX, offsetY, offsetZ), 
                            1, 0.1, 0.1, 0.1, 0.04);
                    /*world.spawnParticle(Particle.SMOKE, loc.clone().add(offsetX, offsetY, offsetZ),
                            1, 0.1, 0.1, 0.1, 0.01);*/
                }

                ticks++;
            }
        };
        
        particleTasks.put(playerId, task);
        task.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, 3L);

        // Only show activation message for new activations, not timer resets
        if (!wasAlreadyFiredUp) {
            player.sendMessage(ChatColor.GOLD + "You are Fired Up!");
            player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.3f, 0.7f);
        }
    }

    private void applySplashDamage(Player player, LivingEntity mainTarget, double originalDamage) {
        Location hitLocation = mainTarget.getLocation();
        double splashDamage = originalDamage * 0.3;
        double radius = 1.4;

        // Small particle effect to indicate 1.4 block AoE
        hitLocation.getWorld().spawnParticle(Particle.FLAME, hitLocation, 14, 0.4, 0.4, 0.4, 0.06);

        // Apply splash damage to nearby entities (excluding main target)
        for (Entity entity : hitLocation.getWorld().getNearbyEntities(hitLocation, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (target.equals(mainTarget)) continue; // Skip main target - already got bonus damage
            if (target.equals(player)) continue; // Don't damage self

            // Check if target is exposed to splash (similar to fireball explosion logic)
            if (!ScrollUtils.isExposedTo(hitLocation, target)) continue;

            // Use ScrollUtils.canHit for proper hit detection
            if (!ScrollUtils.canHit(player, target, true, JavaPlugin.getPlugin(Aurum.class))) {
                continue;
            }

            // Apply splash damage
            ScrollUtils.applySpellDamage(player, target, splashDamage);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "AoE damage: " + splashDamage);
            //player.sendMessage(ChatColor.YELLOW + "Splash damage applied");
            //target.setFireTicks(60); // Brief fire effect
        }
    }

    private boolean isFiredUp(UUID playerId) {
        return firedUpPlayers.getOrDefault(playerId, false);
    }

    private boolean isSweepAttack(Player player, ItemStack weapon, EntityDamageByEntityEvent event) {
        // Vanilla sweep attacks occur with any sword when fully charged (not moving/sprinting)
        // Sweep attacks do less damage (1 damage + enchantment bonus) and have a specific damage pattern
        // The main hit does full damage, sweep hits do reduced damage
        
        // Check if this is a sweep hit by damage amount
        // Sweep attacks typically do 1 damage + enchantment bonus
        // If damage is very low compared to what a sword should do, it's likely a sweep
        double expectedDamage = getExpectedSwordDamage(weapon);
        return event.getDamage() < expectedDamage * 0.3; // Less than 30% of expected damage = sweep
    }
    
    private double getExpectedSwordDamage(ItemStack sword) {
        // Get base damage for sword type
        double baseDamage = switch (sword.getType()) {
            case WOODEN_SWORD -> 4.0;
            case STONE_SWORD -> 5.0;
            case IRON_SWORD -> 6.0;
            case GOLDEN_SWORD -> 4.0;
            case DIAMOND_SWORD -> 7.0;
            case NETHERITE_SWORD -> 8.0;
            default -> 6.0; // Default fallback
        };
        
        // Add sharpness bonus if present
        if (sword.containsEnchantment(Enchantment.SHARPNESS)) {
            int level = sword.getEnchantmentLevel(Enchantment.SHARPNESS);
            baseDamage += level * 1.25; // Each sharpness level adds 1.25 damage
        }
        
        return baseDamage;
    }

    private void resetFiredUpTimer(Player player) {
        // Extend fired up duration by restarting the 7-second timer
        UUID playerId = player.getUniqueId();
        if (firedUpPlayers.getOrDefault(playerId, false)) {
            activateFiredUp(player); // This will cancel old task and start fresh 7 seconds
        }
    }
}

package BHML.aurum.runes.air;

import BHML.aurum.Aurum;
import BHML.aurum.runes.core.RuneUtils;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AcrobatListener implements Listener {

    private final Map<UUID, AcrobatPlayerData> playerData = new HashMap<>();

    private static class AcrobatPlayerData {
        boolean isInJump = false;
        boolean hasUsedJumpAttack = false;
        boolean wasOnGround = true;
        boolean isSneaking = false;
        double jumpStartY = 0.0;
        boolean hasJumpBoost = false;
        
        void reset() {
            isInJump = false;
            hasUsedJumpAttack = false;
            wasOnGround = true;
            isSneaking = false;
            jumpStartY = 0.0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;

        if (!RuneUtils.hasRune(weapon, new Acrobat())) return;

        UUID playerId = player.getUniqueId();
        AcrobatPlayerData data = playerData.computeIfAbsent(playerId, k -> new AcrobatPlayerData());

        // Prevent air attacks if player is in water
        if (player.isInWater()) {
            return;
        }

        // Check if player is crouching in the air - this takes priority over other effects
        if (data.isSneaking && !player.isOnGround()) {
            performCrouchAttack(player, target, event);
            return;
        }

        // Check if player is in the air
        if (!player.isOnGround()) {
            // Check if player is moving upward (jump phase)
            if (player.getVelocity().getY() > 0 && !data.hasUsedJumpAttack) {
                performJumpAttack(player, target, event);
            } 
            // Check if player is falling or second strike in air
            else if (player.getVelocity().getY() <= 0) {
                performFallingAttack(player, target, event);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        AcrobatPlayerData data = playerData.computeIfAbsent(playerId, k -> new AcrobatPlayerData());

        boolean currentlyOnGround = player.isOnGround();
        
        // Detect when player leaves ground (starts jumping/falling)
        if (data.wasOnGround && !currentlyOnGround) {
            data.isInJump = true;
            data.hasUsedJumpAttack = false;
            data.jumpStartY = player.getLocation().getY();
            
            // Check if this is actually a jump (positive Y velocity)
            if (player.getVelocity().getY() > 0) {
                // Player is jumping
            }
        }
        
        // Detect when player lands on ground
        if (!data.wasOnGround && currentlyOnGround) {
            data.isInJump = false;
            data.hasUsedJumpAttack = false;
            data.jumpStartY = 0.0;
        }
        
        data.wasOnGround = currentlyOnGround;
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        AcrobatPlayerData data = playerData.computeIfAbsent(playerId, k -> new AcrobatPlayerData());
        
        data.isSneaking = event.isSneaking();
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        AcrobatPlayerData data = playerData.computeIfAbsent(playerId, k -> new AcrobatPlayerData());
        
        // Check if the new item has Acrobat rune
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        boolean hasAcrobat = newItem != null && RuneUtils.hasRune(newItem, new Acrobat());
        
        if (hasAcrobat && !data.hasJumpBoost) {
            // Add Jump Boost 1 effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 0, false, false));
            data.hasJumpBoost = true;
        } else if (!hasAcrobat && data.hasJumpBoost) {
            // Remove Jump Boost effect
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            data.hasJumpBoost = false;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // Schedule check after inventory action completes
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndUpdateJumpBoost(player);
            }
        }.runTaskLater(JavaPlugin.getPlugin(Aurum.class), 1L);
    }
    
    private void checkAndUpdateJumpBoost(Player player) {
        UUID playerId = player.getUniqueId();
        AcrobatPlayerData data = playerData.computeIfAbsent(playerId, k -> new AcrobatPlayerData());
        
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        boolean hasAcrobat = mainHand != null && RuneUtils.hasRune(mainHand, new Acrobat());
        
        if (hasAcrobat && !data.hasJumpBoost) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 0, false, false));
            data.hasJumpBoost = true;
        } else if (!hasAcrobat && data.hasJumpBoost) {
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            data.hasJumpBoost = false;
        }
    }

    private void performJumpAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        UUID playerId = player.getUniqueId();
        AcrobatPlayerData data = playerData.get(playerId);
        
        // Calculate the height the player would reach at the peak of their jump
        // With Jump Boost 1, the jump velocity multiplier is 1.2x (20% higher)
        double currentHeight = player.getLocation().getY();
        double jumpVelocity = player.getVelocity().getY();
        double jumpBoostMultiplier = 1.2; // Jump Boost 1 adds 20% height
        double peakHeight = currentHeight + (jumpVelocity * jumpVelocity * jumpBoostMultiplier) / (2 * 0.08); // 0.08 is gravity in Minecraft
        
        // Knock target up with flat 2-block height
        // Check if there is a block directly above target (2 block high space issue)
        Location targetLoc = target.getLocation();
        boolean hasBlockAbove = targetLoc.clone().add(0, 2, 0).getBlock().getType().isSolid();
        
        if (hasBlockAbove) {
            // Don't knockup if there are blocks above, just apply slow falling and effects
            player.sendMessage(ChatColor.AQUA + "Jump Attack! No room to knock up!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.2f);
        } else {
            // First lift entity slightly off ground to overcome ground friction
            targetLoc.setY(targetLoc.getY() + 1.0); // Raise off ground for knockup velocity
            target.teleport(targetLoc);
            
            // Now apply the velocity
            Vector targetVelocity = target.getVelocity();
            targetVelocity.setY(0.7); //  knockup
            target.setVelocity(targetVelocity);
            
            player.sendMessage(ChatColor.AQUA + "Jump Attack! Enemy knocked up!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.2f);
        }
        
        // Mark that jump attack has been used
        data.hasUsedJumpAttack = true;
        
        // Apply slow falling to both player and target for 2 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 0, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 0, false, false));
        
        // Visual effects
        targetLoc.getWorld().spawnParticle(Particle.CLOUD, targetLoc, 10, 0.5, 0.5, 0.5, 0.1);
        targetLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, targetLoc, 1);
        
        player.sendMessage(ChatColor.AQUA + "Jump Attack! Enemy knocked up!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.2f);
    }

    private void performFallingAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        double bonusDamage = 1.0; // Default bonus damage
        
        // Check if target is in the air (likely from jump knockup)
        if (!target.isOnGround()) {
            bonusDamage = 6.0; // Increased damage for airborne targets
            // Remove slow falling from the knock up
            target.removePotionEffect(PotionEffectType.SLOW_FALLING);
        }
        
        // Apply bonus damage
        event.setDamage(event.getDamage() + bonusDamage);
        
        // Strike enemy towards ground
        Vector targetVelocity = target.getVelocity();
        targetVelocity.setY(-2.5); // Strike downward
        target.setVelocity(targetVelocity);
        
        // Visual effects
        Location targetLoc = target.getLocation();
        targetLoc.setY(targetLoc.getY() + 0.6);
        targetLoc.getWorld().spawnParticle(Particle.CRIT, targetLoc, 8, 0.3, 0.3, 0.3, 0.1);
        targetLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, targetLoc, 1);
        
        if (bonusDamage == 6.0) {
            player.sendMessage(ChatColor.AQUA + "Falling Strike! +" + bonusDamage + " damage to airborne target!");
            targetLoc.getWorld().spawnParticle(Particle.CRIT, targetLoc, 6, 0.3, 0.3, 0.3, 0.2);
            targetLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, targetLoc, 2);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 0.7f);

        } else {
            player.sendMessage(ChatColor.AQUA + "Falling Strike! +" + bonusDamage + " damage!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.6f, 0.8f);
        }

    }

    private void performCrouchAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        double bonusDamage = 1.0;

        if (!target.isOnGround()) {
            bonusDamage = 3.0; // Increased damage for airborne targets
        }
        
        // Apply bonus damage
        event.setDamage(event.getDamage() + bonusDamage);
        
        // Calculate knockback direction (away from player)
        Vector knockbackDirection = target.getLocation().toVector()
                .subtract(player.getLocation().toVector())
                .normalize();
        knockbackDirection.setY(0.5); // Slight upward component
        knockbackDirection.multiply(2.5); // Strong knockback
        knockbackDirection.setY(0.2); // Slight upward component
        
        target.setVelocity(knockbackDirection);
        
        // Visual effects
        Location targetLoc = target.getLocation();
        targetLoc.getWorld().spawnParticle(Particle.EXPLOSION, targetLoc, 3, 0.3, 0.3, 0.3, 0.1);
        targetLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, targetLoc, 1);
        
        player.sendMessage(ChatColor.AQUA + "Crouch Thrust! +" + bonusDamage + " damage and strong knockback!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.7f, 0.9f);
    }
}

package BHML.aurum.runes.ender;

import BHML.aurum.Aurum;
import BHML.aurum.runes.core.Rune;
import BHML.aurum.runes.core.RuneRegistry;
import BHML.aurum.runes.core.RuneUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulStrikeListener implements Listener {
    
    private static final NamespacedKey SOUL_DAMAGE_KEY = 
            new NamespacedKey(JavaPlugin.getPlugin(Aurum.class), "soul_damage");
    private static final NamespacedKey SOUL_OWNER_KEY = 
            new NamespacedKey(JavaPlugin.getPlugin(Aurum.class), "soul_owner");
    
    // Track players who recently captured souls
    private final Map<UUID, Boolean> soulCaptureCooldown = new HashMap<>();
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        
        Player player = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();
        
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.getType().name().contains("SWORD")) return;
        
        Rune soulStrikeRune = RuneRegistry.getRune("soul_strike");
        if (soulStrikeRune == null || !RuneUtils.hasRune(weapon, soulStrikeRune)) return;
        
        // Check if sword has an existing soul
        Double existingSoulDamage = getSoulDamage(weapon);
        UUID soulOwner = getSoulOwner(weapon);
        boolean hasExistingSoul = existingSoulDamage != null && existingSoulDamage > 0 && 
                               soulOwner != null && soulOwner.equals(player.getUniqueId());
        
        double originalDamage = event.getDamage();
        double targetHealth = target.getHealth();
        
        // First, check if we can use soul strike (has soul and not on cooldown)
        if (hasExistingSoul) {
            // Apply bonus damage
            event.setDamage(originalDamage + existingSoulDamage);
            
            // Clear the soul after use
            clearSoulFromWeapon(weapon);
            
            // Particle effects scaled by damage (the main effect)
            Location targetLoc = target.getLocation();
            targetLoc.setY(targetLoc.getY() + 0.5); // Raise Y level by 1
            World world = targetLoc.getWorld();
            if (world != null) {
                int particleCount = (int) Math.min(existingSoulDamage * 2, 30); // Scale particles, max 30
                world.spawnParticle(Particle.END_ROD, targetLoc, particleCount, 0.3, 0.3, 0.3, 0.25);
                world.playSound(targetLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.6f, 0.8f);
                
                // Send message to player
                player.sendMessage(ChatColor.DARK_PURPLE + "Soul Strike! " + 
                                 ChatColor.LIGHT_PURPLE + "+" + String.format("%.1f", existingSoulDamage) + " bonus damage");
            }
            return; // Don't process soul capture since we used the soul
        }
        
        // Then, check if this hit will kill the target and we have no soul
        if (targetHealth <= originalDamage && !hasExistingSoul) {
            
            // This hit will kill the target - capture the soul
            double maxHealth = target.getMaxHealth();
            double soulDamage = maxHealth * 0.4;
            soulDamage = Math.min(soulDamage, 15.0);
            
            // Store soul data in the sword
            storeSoulInWeapon(weapon, soulDamage, player.getUniqueId());

            // Visual feedback for soul capture (minimal - just store the soul)
            Location loc = target.getLocation();
            World world = loc.getWorld();
            if (world != null) {
                // Subtle effect for soul capture - just a few particles and quiet sound
                world.spawnParticle(Particle.END_ROD, loc, 5, 0.2, 0.2, 0.2, 0.05);
                world.playSound(loc, Sound.ENTITY_ENDERMAN_AMBIENT, 0.3f, 1.5f);
                
                // Send message to player
                player.sendMessage(ChatColor.DARK_PURPLE + "Soul captured! " + 
                                 ChatColor.LIGHT_PURPLE + "+" + String.format("%.1f", soulDamage) + " damage stored");
            }
        }
    }
    
    private void storeSoulInWeapon(ItemStack weapon, double damage, UUID owner) {
        if (weapon == null || !weapon.hasItemMeta()) return;
        
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer()
                .set(SOUL_DAMAGE_KEY, PersistentDataType.DOUBLE, damage);
        meta.getPersistentDataContainer()
                .set(SOUL_OWNER_KEY, PersistentDataType.STRING, owner.toString());
        weapon.setItemMeta(meta); // Save the meta back to the item!
    }
    
    private Double getSoulDamage(ItemStack weapon) {
        if (weapon == null || !weapon.hasItemMeta()) return null;
        
        return weapon.getItemMeta().getPersistentDataContainer()
                .get(SOUL_DAMAGE_KEY, PersistentDataType.DOUBLE);
    }
    
    private UUID getSoulOwner(ItemStack weapon) {
        if (weapon == null || !weapon.hasItemMeta()) return null;
        
        String ownerStr = weapon.getItemMeta().getPersistentDataContainer()
                .get(SOUL_OWNER_KEY, PersistentDataType.STRING);
        return ownerStr != null ? UUID.fromString(ownerStr) : null;
    }
    
    private void clearSoulFromWeapon(ItemStack weapon) {
        if (weapon == null || !weapon.hasItemMeta()) return;
        
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer()
                .remove(SOUL_DAMAGE_KEY);
        meta.getPersistentDataContainer()
                .remove(SOUL_OWNER_KEY);
        weapon.setItemMeta(meta); // Save the meta back to the item!
    }
}

package BHML.aurum.scrolls.earth;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import org.bukkit.entity.Player;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static BHML.aurum.scrolls.core.ScrollUtils.*;


public class Rumble implements Scroll {

    @Override
    public Element getElement() {
        return Element.EARTH;
    }

    @Override
    public String getId() {
        return "rumble";
    }

    @Override
    public String getName() {
        return "Rumble";
    }

    @Override
    public int getMaxUses() {
        return 40;
    }

    @Override
    public String getDescription(){
        return "Manipulate and rumble the ground around you for 10 seconds, damaging enemies";
    }


    @Override
    public int getGoldCost() { return 1; }
    @Override
    public int getRestoreAmount() { return 10; }
    @Override
    public int getCooldown() { return 10000; }  //10 seconds?


    @Override
    public void cast(Player player) {
        int durationTicks = 200; // 10 seconds total
        int interval = 10; // every 0.5 seconds
        double radius = 5.0;

        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                Location center = player.getLocation();
                Set<UUID> hitEntities = new HashSet<>();
                // --- Damage + knock nearby enemies ---
                for (Entity entity : player.getWorld().getNearbyEntities(center, radius, 3, radius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (target.hasMetadata("em_spell_damage")) continue;

                    //if (!handleBlockedTargetFeedback(player, target)) continue;

                    hitEntities.add(target.getUniqueId());


                    // Light vertical knock
                    Vector velocity = target.getVelocity();
                    velocity.setY(0.05);
                    target.setVelocity(velocity);

                    // Apply spell damage with metadata guard
                    //target.setMetadata("em_spell_damage", new FixedMetadataValue(JavaPlugin.getPlugin(ElementalMagicTesting.class), true));

                    applySpellDamage(player, target, 2.5);

                    //Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () ->
                    //target.removeMetadata("em_spell_damage", JavaPlugin.getPlugin(ElementalMagicTesting.class)), 1L);

                    // Particle burst under enemies
                    target.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, target.getLocation(), 10, 0.3, 0.1, 0.3, Material.DIRT.createBlockData());

                    // --- New: Play hit sound at enemy ---
                    target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.2f);

                    // --- New: Camera shake effect for players hit ---
                    if (target instanceof Player targetPlayer) {
                        // Apply a brief slowness effect (level 1, 5 ticks) to simulate camera jitter
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 1, 1, false, false, false));
                    }
                }

                // --- Play quake sound centered on player ---
                center.getWorld().playSound(center, Sound.BLOCK_STONE_BREAK, 1.2f, 0.7f);

                // --- Visual circle around player to mark quake zone ---
                int points = 30; // how many particles in the ring
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLoc = center.clone().add(x, 0.1, z);
                    center.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, particleLoc, 10, 0.2, 0.2, 0.2, 0, Material.DIRT.createBlockData());
                }

                // --- Slight camera shake for caster ---
                //player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2, 1, false, false, false));



                List<Entity> hitList = hitEntities.stream()
                        .map(Bukkit::getEntity)
                        .filter(Objects::nonNull)
                        .filter(e -> e instanceof LivingEntity)
                        .toList();



                clearBlockedTargets(player);





                ticksRun += interval;
                if (ticksRun >= durationTicks) {
                    clearBlockedTargets(player);
                    cancel();
                }

            }
        }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, interval);
    }

}

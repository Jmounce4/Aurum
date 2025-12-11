package BHML.aurum.scrolls.air;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.scrolls.core.ScrollUtils;
import org.bukkit.entity.Player;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static BHML.aurum.scrolls.core.ScrollUtils.*;

public class Gust implements Scroll {

    @Override
    public Element getElement() {
        return Element.AIR;
    }

    @Override
    public String getId() {
        return "gust";
    }

    @Override
    public String getName() {
        return "Gust";
    }

    @Override
    public int getMaxUses() {
        return 40;
    }

    @Override
    public String getDescription(){
        return "Emit a sweeping gust, producing significant knockback";
    }


    @Override
    public int getGoldCost() { return 1; }
    @Override
    public int getRestoreAmount() { return 20; }
    @Override
    public int getCooldown() { return 500; }


    @Override
    public void cast(Player player) {

        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize();
        World world = player.getWorld();

        double maxDistance = 15.0;
        int steps = 5; //Essentially speed, Higher = Slower
        double initialRadius = 1.5;
        double finalRadius = 9.0;

        Set<UUID> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            int currentStep = 0;

            @Override
            public void run() {
                if (currentStep > steps) {
                    // Clear blocked targets now that the spell cast is done
                    clearBlockedTargets(player);
                    cancel();
                    return;
                }

                double progress = currentStep / (double) steps;
                double forwardDistance = maxDistance * progress;
                double currentRadius = initialRadius + (finalRadius - initialRadius) * progress;

                Location stepCenter = origin.clone().add(direction.clone().multiply(forwardDistance));

                Vector perp = new Vector(0, 1, 0).crossProduct(direction).normalize();

                int particleCount = (int) (currentRadius * 1);

                for (int i = -particleCount; i <= particleCount; i++) {
                    double fraction = i / (double) particleCount;
                    Vector offset = perp.clone().multiply(fraction * currentRadius);
                    Location particleLocation = stepCenter.clone().add(offset).add(0, 1, 0);
                    world.spawnParticle(Particle.SWEEP_ATTACK, particleLocation, 1, 0, 0, 0, 0);
                }

                // Hit detection
                for (Entity entity : world.getNearbyEntities(stepCenter, currentRadius, 1, currentRadius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (hitEntities.contains(target.getUniqueId())) continue;

                    //if(!handleBlockedTargetFeedback(player, target)) continue;

                    //My hit detection
                    if (!hasClearShot(player, target)) continue;

                    if (!canHit(player, target, true, JavaPlugin.getPlugin(Aurum.class))) {
                        continue; // skip this target - will not deal damage nor spawn beam
                    }

                    Vector toTarget = target.getLocation().toVector().subtract(origin.toVector());
                    double angle = Math.toDegrees(direction.angle(toTarget));
                    if (angle > 60.0) continue; // Only entities roughly in front
                    if (toTarget.length() > maxDistance) continue; // Past max range

                    hitEntities.add(target.getUniqueId());

                    // Knockback away from player
                    Vector knockback = toTarget.normalize().multiply(7.5);
                    knockback.setY(2.5);
                    target.setVelocity(knockback);

                    // Apply damage (tweak damage as needed)
                    applySpellDamage(player, target, 4.0);

                    // Play hit sound
                    world.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.6f);
                }


                //Attempt to debug XP gain calced many times.
                if (currentStep == steps-1) {
                    List<Entity> hitList = hitEntities.stream()
                            .map(Bukkit::getEntity)
                            .filter(Objects::nonNull)
                            .filter(e -> e instanceof LivingEntity)
                            .toList();


                    clearBlockedTargets(player);
                }

                currentStep++;
            }







        }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, 1L);

        // Initial cast effect
        world.spawnParticle(Particle.CLOUD, origin, 20, 0.5, 0.5, 0.5, 0.05);
        world.playSound(origin, Sound.ENTITY_PHANTOM_FLAP, 1.2f, 2.0f);

    }


}

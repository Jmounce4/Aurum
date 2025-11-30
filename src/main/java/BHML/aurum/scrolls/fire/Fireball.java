package BHML.aurum.scrolls.fire;

import BHML.aurum.Aurum;
import BHML.aurum.scrolls.core.Scroll;

import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static BHML.aurum.scrolls.core.ScrollUtils.*;

public class Fireball implements Scroll {
    @Override
    public Element getElement() {
        return Element.FIRE;
    }

    @Override
    public String getId() {
        return "fireball";
    }

    @Override
    public String getName() {
        return "Fireball";
    }

    @Override
    public int getMaxUses() {
        return 40;
    }

    @Override
    public String getDescription(){
        return "Shoot a fireball that explodes on impact, dealing AoE damage and igniting targets";
    }


    @Override
    public int getGoldCost() { return 1; }
    @Override
    public int getRestoreAmount() { return 10; }
    @Override
    public int getCooldown() { return 500; }


    @Override
    public void cast(Player player) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize().multiply(1.0); // Launch velocity
        World world = player.getWorld();

        int maxTicks = 2500; // lifetime
        double directDamage = 8.0;      // damage on impact (explosion)
        double directRadius = 1.5;      // direct explosion radius
        double lingerRadius = 4.5;      // lingering AoE radius
        double lingerDamagePerSecond = 2.0;
        int lingerSeconds = 10;
        double gravity = -0.015;

        Vector currentVelocity = direction.clone();
        Location current = origin.clone();
        Set<UUID> hitEntities = new HashSet<>();
        AtomicBoolean impacted = new AtomicBoolean(false);

        // dark purple dust color
        Particle.DustOptions purple = new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0f);

        world.playSound(current, Sound.ENTITY_GHAST_SHOOT, 0.2f, 2.0f);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (impacted.get() || current.getY() < 1) {
                    clearBlockedTargets(player);
                    cancel();
                    return;
                }

                // step along projectile path (same stepping logic as your Fireball)
                Vector step = currentVelocity.clone().normalize().multiply(0.4);
                int steps = (int) Math.ceil(currentVelocity.length() / 0.2);

                for (int i = 0; i < steps; i++) {
                    current.add(step);

                    // Block collision
                    if (!world.getBlockAt(current).isPassable()) {
                        Vector offset = step.clone().multiply(-0.8);
                        Location impactLoc = current.clone().add(offset);
                        triggerImpact(impactLoc);
                        return;
                    }

                    // Travel visuals (small purple trail)
                    world.spawnParticle(Particle.DUST, current, 2,
                            0.05, 0.05, 0.05,
                            0.01f, purple);

                    world.playSound(current, Sound.ENTITY_ENDER_DRAGON_SHOOT, 0.08f, 1.8f);

                    // Entity hit check (direct hit)
                    for (Entity entity : world.getNearbyEntities(current, 1.2, 1.2, 1.2)) { // slightly generous
                        if (!(entity instanceof LivingEntity target)) continue;

                        if (target.equals(player)) continue;
                        if (hitEntities.contains(target.getUniqueId())) continue;

                        // require clear shot like your fireball
                        if (!hasClearShot(player, target)) continue;

                        // mark and apply direct-hit damage & effects
                        hitEntities.add(target.getUniqueId());
                        target.setFireTicks(0); // no fire for ender shot, but leave placeholder if you want

                        // Immediate explosion: damage nearby living entities within directRadius
                        triggerImpact(target.getLocation());
                        return;
                    }
                }

                // gravity
                currentVelocity.setY(currentVelocity.getY() + gravity);

                tick++;
                if (tick > maxTicks) {
                    clearBlockedTargets(player);
                    cancel();
                }
            }

            private void triggerImpact(Location loc) {
                impacted.set(true);

                // impact visual burst (denser purple)
                world.spawnParticle(Particle.DUST, loc, 120,
                        0.8, 0.8, 0.8,
                        0.05f, purple);
                world.spawnParticle(Particle.DUST, loc, 30,
                        1.8, 1.8, 1.8,
                        0.02f, purple);
                world.playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, 0.9f, 0.6f);

                // IMMEDIATE EXPLOSION DAMAGE (directRadius)
                for (Entity aoe : world.getNearbyEntities(loc, directRadius, directRadius, directRadius)) {
                    if (!(aoe instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (hitEntities.contains(target.getUniqueId())) continue;
                    if (!isExposedTo(loc, target)) continue; // respect visibility
                    hitEntities.add(target.getUniqueId());
                    // direct hit damage
                    applySpellDamage(player, target, directDamage);
                }

                // Start manual lingering AoE (no AreaEffectCloud)
                startLingeringAoE(loc.clone());
                clearBlockedTargets(player);
            }

            // Lingering AoE: spawns particles and applies damage every 20 ticks for lingerSeconds
            private void startLingeringAoE(Location center) {
                final int intervalTicks = 20; // damage once per second
                final int totalTicks = lingerSeconds * 20;
                final int particlePoints = 36; // circle density
                new BukkitRunnable() {
                    int lived = 0;

                    @Override
                    public void run() {
                        if (lived >= totalTicks) {
                            cancel();
                            return;
                        }

                        // Damage on this tick (every intervalTicks)
                        if (lived % intervalTicks == 0) {
                            for (Entity e : center.getWorld().getNearbyEntities(center, lingerRadius, 2, lingerRadius)) {
                                if (!(e instanceof LivingEntity le)) continue;
                                if (le.equals(player)) continue;
                                if (!isExposedTo(center, le)) continue; // only if exposed
                                applySpellDamage(player, le, lingerDamagePerSecond);
                            }
                        }

                        // Spawn ring particles (light fill)
                        for (int p = 0; p < particlePoints; p++) {
                            double angle = 2 * Math.PI * p / particlePoints;
                            double x = center.getX() + Math.cos(angle) * lingerRadius;
                            double z = center.getZ() + Math.sin(angle) * lingerRadius;
                            Location particleLoc = new Location(center.getWorld(), x, center.getY() + 0.5, z);
                            center.getWorld().spawnParticle(Particle.DUST,
                                    particleLoc,
                                    1,
                                    0.0, 0.0, 0.0,
                                    0.01f,
                                    purple);
                        }

                        lived += 2; // tick every 2 ticks for particles/efficiency
                    }
                }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, 2L);
            }

        }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, 1L);
    }


}

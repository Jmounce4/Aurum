package BHML.aurum.scrolls.ender;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.scrolls.core.ScrollUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
//import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static BHML.aurum.scrolls.core.ScrollUtils.*;

public class EndShot implements Scroll {

    @Override
    public Element getElement() {
        return Element.ENDER;
    }

    @Override
    public String getId() {
        return "endshot";
    }

    @Override
    public String getName() {
        return "End Shot";
    }

    @Override
    public int getMaxUses() {
        return 40;
    }

    @Override
    public String getDescription(){
        return "Emit a dark, lingering energy";
    }


    @Override
    public int getGoldCost() { return 1; }
    @Override
    public int getRestoreAmount() { return 10; }
    @Override
    public int getCooldown() { return 10000; } // 1 sec


    @Override
    public void cast(Player player) {

        World world = player.getWorld();
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize().multiply(1.0);

        double hitRadius = 1.5;
        double gravity = -0.01;
        int maxTicks = 2500;

        double lingerRadius = 4.5;
        double onHitDamage = 8.0;
        double lingerDamage = 2.0;

        Set<UUID> hitEntities = new HashSet<>();
        Location current = origin.clone();
        Vector velocity = direction.clone();

        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.2f);

        // Launch sound
        world.playSound(current, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.4f);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {

                // Safety
                if (current.getY() < 1) {
                    cancel();
                    return;
                }

                // Step movement
                Vector step = velocity.clone().normalize().multiply(0.35);
                int steps = (int) Math.ceil(velocity.length() / 0.25);

                for (int i = 0; i < steps; i++) {
                    current.add(step);

                    // Block hit
                    if (!world.getBlockAt(current).isPassable()) {
                        triggerExplosion(current.clone());
                        cancel();
                        return;
                    }

                    // Visual travel particles
                    world.spawnParticle(Particle.DUST, current, 3, 0.06, 0.06, 0.06, dust);
                    world.spawnParticle(Particle.PORTAL, current, 2, 0.06, 0.06, 0.06, 0.05);

                    // Sound (light)
                    if (tick % 6 == 0)
                        world.playSound(current, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.15f, 1.6f);

                    // Entity hit
                    for (Entity e : world.getNearbyEntities(current, hitRadius, hitRadius, hitRadius)) {

                        if (!(e instanceof LivingEntity target)) continue;
                        if (target.equals(player)) continue;

                        if (hitEntities.contains(target.getUniqueId())) continue;

                        if (!hasClearShot(player, target)) continue;

                        hitEntities.add(target.getUniqueId());
                        applySpellDamage(player, target, onHitDamage);

                        triggerExplosion(target.getLocation());

                        cancel();
                        return;
                    }
                }

                // Gravity
                velocity.setY(velocity.getY() + gravity);

                tick++;
                if (tick > maxTicks) cancel();
            }

            /**
             * Handles the initial impact explosion AND starts the lingering effect.
             */
            private void triggerExplosion(Location loc) {

                // DRAMATIC FAST EXPLOSION PARTICLES
                for (int i = 0; i < 60; i++) {
                    Vector v = new Vector(
                            (Math.random() - 0.5) * 1.8,
                            (Math.random() - 0.2) * 1.2,
                            (Math.random() - 0.5) * 1.8
                    );

                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, dust);
                    world.spawnParticle(Particle.PORTAL, loc, 1, 0, 0, 0, v.length() * 0.15);
                }

                // Shockwave ring burst
                for (int i = 0; i < 40; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double speed = 0.7 + Math.random() * 0.6;

                    Vector dir = new Vector(Math.cos(angle), 0, Math.sin(angle)).multiply(speed);

                    world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, speed * 1.5);
                }

                // SOUND BLAST
                world.playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.4f);
                //world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.7f);

                // AOE INSTANT HIT (1.5 block radius like the fireball)
                for (Entity e : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {

                    if (!(e instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (hitEntities.contains(target.getUniqueId())) continue;

                    if (!isExposedTo(loc, target)) continue;

                    hitEntities.add(target.getUniqueId());
                    applySpellDamage(player, target, onHitDamage);
                }

                // Start lingering dark pulse
                startLingerPulse(loc.clone(), player);
            }

            private void startLingerPulse(Location center, Player player) {
                World world = center.getWorld();
                double radius = 5.5;
                double damagePerSecond = 4.0;
                int durationSeconds = 10;
                int ticksPerPulse = 20; // 1 second per tick pulse

                Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.2f);

                Set<UUID> hitEntities = new HashSet<>();

                new BukkitRunnable() {
                    int ticksLived = 0;

                    @Override
                    public void run() {
                        if (ticksLived >= durationSeconds * 20) {
                            cancel();
                            return;
                        }

                        // 1️⃣ Damage entities in radius
                        for (Entity e : center.getNearbyEntities(radius, 1.0, radius)) {
                            if (!(e instanceof LivingEntity le)) continue;
                            if (le.equals(player)) continue;
                            //if (!ScrollUtils.isExposedTo(center, le)) continue;

                            applySpellDamage(player, le, damagePerSecond);
                        }

                        // 2️⃣ Spawn inner fill particles
                        for (int i = 0; i < 80; i++) {
                            double angle = Math.random() * 2 * Math.PI;
                            double r = Math.random() * radius;
                            double x = center.getX() + r * Math.cos(angle);
                            double z = center.getZ() + r * Math.sin(angle);
                            double y = center.getY() + 0.2 + Math.random() * 1.2;

                            world.spawnParticle(Particle.DUST, new Location(world, x, y, z), 3, 0, 0, 0, dust);
                        }

                        // 3️⃣ Spawn perimeter circle
                        int segments = 60; // More segments = smoother circle
                        for (int i = 0; i < segments; i++) {
                            double angle = 2 * Math.PI * i / segments;
                            double x = center.getX() + radius * Math.cos(angle);
                            double z = center.getZ() + radius * Math.sin(angle);
                            double y = center.getY() + 0.5; // slightly above ground

                            world.spawnParticle(Particle.DUST, new Location(world, x, y, z), 1, 0, 0, 0, dust);
                        }

                        ticksLived += ticksPerPulse;
                    }
                }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, ticksPerPulse);
            }

        }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, 1L);
    }





}

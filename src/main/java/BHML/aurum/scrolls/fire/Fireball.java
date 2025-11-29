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
    public int getMaxUses() {
        return 20;
    }

    @Override
    public String getDescription(){
        return "Shoots a fireball that explodes on impact, " +
                "dealing damage and igniting targets";
    }


    @Override
    public int getGoldCost() { return 1; }
    @Override
    public int getRestoreAmount() { return 2; }


    @Override
    public void cast(Player player) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize().multiply(1.0); // Launch velocity
        World world = player.getWorld();

        int maxTicks = 2000; // How long until it disappears
        double damage = 10.0;
        double hitRadius = 1.8;
        double gravity = -0.015;

        Vector currentVelocity = direction.clone();
        Location current = origin.clone();
        Set<UUID> hitEntities = new HashSet<>();
        AtomicBoolean exploded = new AtomicBoolean(false);

        world.playSound(current, Sound.ENTITY_GHAST_SHOOT, 0.2f, 2.0f);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (exploded.get() || current.getY() < 1) {
                    clearBlockedTargets(player);
                    cancel();
                    return;
                }

                Vector step = currentVelocity.clone().normalize().multiply(0.4);
                int steps = (int) Math.ceil(currentVelocity.length() / 0.2);

                for (int i = 0; i < steps; i++) {
                    current.add(step);

                    // Block collision
                    if (!world.getBlockAt(current).isPassable()) {
                        Vector offset = step.clone().multiply(-0.8);
                        Location safeExplosionLoc = current.clone().add(offset);
                        triggerExplosion(safeExplosionLoc);
                        return;
                    }

                    // Visuals
                    world.spawnParticle(Particle.FLAME, current, 2, 0.05, 0.05, 0.05, 0.02);
                    world.spawnParticle(Particle.SMOKE, current, 1, 0.05, 0.05, 0.05, 0.01);
                    world.playSound(current, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.1f, 2.0f);

                    // Entity hit check
                    for (Entity entity : world.getNearbyEntities(current, hitRadius, hitRadius, hitRadius)) {
                        if (!(entity instanceof LivingEntity target)) continue;

                        //Targetting Check
                        //if (!handleBlockedTargetFeedback(player, target)) continue;
                        //Dont hit self
                        if (target.equals(player)) continue;
                        if (hitEntities.contains(target.getUniqueId())) continue;
                        //Block Detection
                        if (!hasClearShot(player, target)) continue;

                        hitEntities.add(target.getUniqueId());
                        target.setFireTicks(100);


                        applySpellDamage(player, target, damage);



                        triggerExplosion(target.getLocation());
                        return;
                    }
                }

                // Apply gravity after stepping
                currentVelocity.setY(currentVelocity.getY() + gravity);

                tick++;


                if (tick > maxTicks) {


                    clearBlockedTargets(player);
                    cancel();
                }
            }

            private void triggerExplosion(Location loc) {
                exploded.set(true);

                world.spawnParticle(Particle.FLAME, loc, 120, 0.8, 0.8, 0.8, 0.1);
                world.spawnParticle(Particle.FLAME, loc, 30, 1.8, 1.8, 1.8, 0.03);
                world.spawnParticle(Particle.LAVA, loc, 25, 0.4, 0.4, 0.4, 0.02);
                world.spawnParticle(Particle.SMOKE, loc, 25, 0.9, 0.9, 0.9, 0.05);
                world.playSound(loc, Sound.ITEM_FIRECHARGE_USE, 0.8f, 1.5f);

                for (Entity aoe : world.getNearbyEntities(loc, 2.3, 2.3, 2.3)) {
                    if (!(aoe instanceof LivingEntity target)) continue;
                    //Targetting Check
                    //if (!handleBlockedTargetFeedback(player, target)) continue;
                    if (target.equals(player)) continue;
                    if (hitEntities.contains(target.getUniqueId())) continue;

                    if (!isExposedTo(loc, target)) continue;

                    hitEntities.add(target.getUniqueId());
                    target.setFireTicks(100);

                    //for explosion hit rather than direct
                    applySpellDamage(player, target, damage);





                    world.spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 35, 0.3, 0.3, 0.3, 0.03);
                }


                List<Entity> hitList = hitEntities.stream()
                        .map(Bukkit::getEntity)
                        .filter(Objects::nonNull)
                        .filter(e -> e instanceof LivingEntity)
                        .toList();



                clearBlockedTargets(player);
            }
        }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, 1L);





    }


}

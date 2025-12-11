package BHML.aurum.scrolls.lightning;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

import static BHML.aurum.scrolls.core.ScrollUtils.*;

public class Zap implements Scroll {
    @Override
    public Element getElement() {
        return Element.LIGHTNING;
    }

    @Override
    public String getId() {
        return "zap";
    }

    @Override
    public String getName() {
        return "Zap";
    }

    @Override
    public int getMaxUses() {
        return 80;
    }

    @Override
    public String getDescription() {
        return "Emit a single targeted bolt of lightning";
    }


    @Override
    public int getGoldCost() {
        return 1;
    }

    @Override
    public int getRestoreAmount() {
        return 20;
    }
    @Override
    public int getCooldown() { return 100; } // 0.1 seconds?


    @Override
    public void cast(Player player) {
        Location eye   = player.getEyeLocation();
        World world    = player.getWorld();
        Vector baseDir = eye.getDirection().normalize();

        double maxRange    = 24.0;
        double segmentLen  = 1.0;
        double stepSubdiv  = 0.2;   // how fine the interpolation is
        double jitterAmp   = 1.4;   // wider swing (switched from 1.2 for testing)
        double hitRadius   = 1.2;
        double damage      = 10.0;

        int segments = (int)(maxRange / segmentLen);
        Random rand = new Random();

        Set<UUID> hit = new HashSet<>();
        LivingEntity struck     = null;
        Location struckLocation = null;

        // start point
        Vector prev = eye.toVector();

        // build jagged bolt in one tick
        for (int i = 1; i <= segments; i++) {
            // along the straight line
            Vector along = eye.toVector().add(baseDir.clone().multiply(i * segmentLen));

            // random jitter
            Vector jitter = new Vector(
                    (rand.nextDouble() * 2 - 1) * jitterAmp,
                    (rand.nextDouble() * 2 - 1) * jitterAmp,
                    (rand.nextDouble() * 2 - 1) * jitterAmp
            );

            Vector next = along.add(jitter);

            // draw continuous line from prev to next
            Vector delta = next.clone().subtract(prev);
            double dist = delta.length();
            Vector step = delta.clone().normalize().multiply(stepSubdiv);
            int   points = (int)(dist / stepSubdiv);

            for (int p = 0; p < points; p++) {
                Vector point = prev.clone().add(step.clone().multiply(p));
                Location loc = point.toLocation(world);

                /* Previous block detection
                if (!world.getBlockAt(loc).isPassable()) {
                    world.spawnParticle(Particle.SMOKE_NORMAL, loc, 8, 0.2, 0.2, 0.2, 0.01);
                    world.playSound(loc, Sound.BLOCK_STONE_HIT, 0.8f, 1.2f);
                    return;
                }*/


                world.spawnParticle(Particle.ELECTRIC_SPARK, loc, 4, 0,0,0, 0);
                world.playSound(loc, Sound.ENTITY_BEE_STING, 0.05f, 0.4f);


                // hit check
                for (Entity e : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (!(e instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (hit.contains(target.getUniqueId())) continue;

                    //if (!handleBlockedTargetFeedback(player, target)) continue;

                    //Block Hit Detection
                    if (!hasClearShot(player, target)) continue;
                    if (!canHit(player, target, true, JavaPlugin.getPlugin(Aurum.class))) {
                        continue; // skip this target - will not deal damage nor spawn beam
                    }
                    //(!player.hasLineOfSight(target)) continue;

                    hit.add(target.getUniqueId());
                    struck = target;
                    struckLocation = target.getEyeLocation().add(0, 0.5, 0);

                    // you can break out of p‑loop, but let’s finish drawing this segment
                    break;
                }
            }

            prev = next;

            if (struck != null) break;
        }

        // final burst and damage
        if (struck != null) {
            applySpellDamage(player, struck, damage);
            world.spawnParticle(Particle.ELECTRIC_SPARK, struckLocation, 12, 0.3,0.3,0.3, 0);
            world.playSound(struckLocation, Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.6f, 1.8f);
        }

        List<Entity> hitList = hit.stream()
                .map(Bukkit::getEntity)
                .filter(Objects::nonNull)
                .filter(e -> e instanceof LivingEntity)
                .toList();



        // Clear blocked targets now that the spell cast is done
        clearBlockedTargets(player);
    }


}

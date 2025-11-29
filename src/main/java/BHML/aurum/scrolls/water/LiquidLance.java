package BHML.aurum.scrolls.water;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static BHML.aurum.scrolls.core.ScrollUtils.*;

public class LiquidLance implements Scroll {
    @Override
    public Element getElement() {
        return Element.WATER;
    }

    @Override
    public String getId() {
        return "liquidlance";
    }

    @Override
    public String getName() {
        return "Liquid Lance";
    }

    @Override
    public int getMaxUses() {
        return 80;
    }

    @Override
    public String getDescription() {
        return "Project a lance of water, piercing and slowing all enemies";
    }


    @Override
    public int getGoldCost() {
        return 1;
    }

    @Override
    public int getRestoreAmount() {
        return 20;
    }

    public int getCooldown() { return 100; } // 0.1 seconds?


    @Override
    public void cast(Player player) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize();

        // --- CONFIGURABLE STATS ---
        int range = 35; // how far the lance travels
        double damage = 5.0; // base damage
        double hitRadius = 1.2; // how close an entity must be to be hit

        World world = player.getWorld();

        // Track already-hit entities so we don't hit the same one multiple times
        Set<UUID> hitEntities = new HashSet<>();

        // --- TRAVEL EFFECT ---
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {

                double speed = 2.0; // blocks per tick — 2 = twice as fast
                Location point = origin.clone().add(direction.clone().multiply(tick * speed));

                if (tick*speed > range) {
                    endSpell();
                    cancel();
                    return;
                }



                //BLOCK HIT DETECTION
                if (!world.getBlockAt(point).isPassable()) {
                    endSpell(); // ✅ Spell hit a block early
                    cancel();
                    return;
                }

                // Visual effects
                world.spawnParticle(Particle.SPLASH, point, 30, 0.2, 0.2, 0.2, 0.01);
                world.spawnParticle(Particle.SPLASH, point, 50, 0.1, 0.1, 0.1, 0.01);
                //Particle.DustOptions waterColor = new Particle.DustOptions(Color.fromRGB(0, 100, 255), 1.0f);
                //world.spawnParticle(Particle.DUST, point, 2, 0.2, 0.2, 0.2, 0.01, waterColor);
                world.playSound(point, Sound.ITEM_BUCKET_EMPTY, 0.2f, 2f);

                for (Entity entity : world.getNearbyEntities(point, hitRadius, hitRadius, hitRadius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (hitEntities.contains(target.getUniqueId())) continue;
                    //if (!handleBlockedTargetFeedback(player, target)) continue;
                    if (!hasClearShot(player, target)) continue;

                    applySpellDamage(player, target, damage);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2)); //40 ticks = 2 sec

                    hitEntities.add(target.getUniqueId());

                    // Hit effects
                    world.spawnParticle(Particle.SPLASH, target.getLocation().add(0, 1, 0), 10, 0.4, 0.5, 0.4, 0.05);
                    world.playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.4f);
                }

                tick++;
            }

            // ✅ This runs once when the spell ends or hits a wall
            private void endSpell() {
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

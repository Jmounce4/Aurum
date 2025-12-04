package BHML.aurum.scrolls.lightning;

import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

import static BHML.aurum.scrolls.core.ScrollUtils.applySpellDamage;
import static BHML.aurum.scrolls.core.ScrollUtils.handleBlockedTargetFeedback;

public class Flash implements Scroll {

    @Override
    public Element getElement() {
        return Element.LIGHTNING;
    }

    @Override
    public String getId() {
        return "flash";
    }

    @Override
    public String getName() {
        return "Flash";
    }

    @Override
    public int getMaxUses() {
        return 50;
    }

    @Override
    public String getDescription() {
        return "Enhance your body to dash at the speed of lightning";
    }


    @Override
    public int getGoldCost() {
        return 1;
    }

    @Override
    public int getRestoreAmount() {
        return 10;
    }
    @Override
    public int getCooldown() { return 100; } // 0.1 seconds?


    private static final double MAX_DISTANCE = 11.0;
    private static final double DAMAGE = 5.0;
    private static final double STEP_SIZE = 0.2;
    private static final double HIT_RADIUS = 0.8;

    @Override
    public void cast(Player player) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize();
        World world = player.getWorld();

        Location lastSafe = origin.clone(); // Start from eye location
        Location checkPoint;

        // STEP 1: Raycast forward
        for (double d = 0; d <= MAX_DISTANCE; d += STEP_SIZE) {
            checkPoint = origin.clone().add(direction.clone().multiply(d));

            if (isLocationSafe(checkPoint)) {
                lastSafe = checkPoint.clone();
            } else {
                break; // Found a wall — stop and use last safe
            }
        }

        // STEP 2: Particle trail + damage
        Set<LivingEntity> damaged = new HashSet<>();
        double distance = origin.distance(lastSafe);
        for (double d = 0; d <= distance; d += STEP_SIZE) {
            Location point = origin.clone().add(direction.clone().multiply(d));
            world.spawnParticle(Particle.ELECTRIC_SPARK, point, 2, 0.1, 0.1, 0.1, 0);

            for (Entity entity : point.getWorld().getNearbyEntities(point, HIT_RADIUS, HIT_RADIUS, HIT_RADIUS)) {
                if (entity instanceof LivingEntity target && target != player && !damaged.contains(target)) {
                    //if (!handleBlockedTargetFeedback(player, target)) continue;
                    applySpellDamage(player, target, DAMAGE);
                    damaged.add(target);
                }
            }
        }

        // STEP 3: Finalize and teleport
        Location finalDestination = lastSafe.clone().subtract(direction.clone().multiply(0.8));
        if (!isLocationSafe(finalDestination)) {
            finalDestination = lastSafe.clone(); // Fallback if too close to wall
        }
        finalDestination.setYaw(player.getLocation().getYaw());
        finalDestination.setPitch(player.getLocation().getPitch());

        player.teleport(finalDestination);
        world.playSound(finalDestination, Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 2.0f);
    }

    private boolean isLocationSafe(Location loc) {
        World world = loc.getWorld();
        Location feet = loc.clone();
        Location head = loc.clone().add(0, 1, 0);
        return !feet.getBlock().getType().isSolid() && !head.getBlock().getType().isSolid();
    }



}

package BHML.aurum.scrolls.air;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Windstride implements Scroll {

    // Configurable parameters
    private final int liftHeight = 3;          // Initial vertical boost
    private final int glideDurationTicks = 200; // 10 seconds
    private final double horizontalSpeedNormal = 15.0;
    private final double verticalSpeedNormal = -0.05;  // slow descent
    private final double horizontalSpeedCrouch = 0.1;
    private final double verticalSpeedCrouch = -15.0;   // faster descent when crouching

    @Override
    public Element getElement() { return Element.AIR; }

    @Override
    public String getId() { return "windstride"; }

    @Override
    public String getName() { return "Windstride"; }

    @Override
    public int getMaxUses() { return 20; }

    @Override
    public String getDescription() {
        return "Enhance your body to lift into the air and glide like wind. Crouch to descend faster.";
    }

    @Override
    public int getGoldCost() { return 1; }

    @Override
    public int getRestoreAmount() { return 5; }

    @Override
    public int getCooldown() { return 10000; }

    @Override
    public void cast(Player player) {

        World world = player.getWorld();
        Location location = player.getLocation();

        if (player.isOnGround()) {
            // Give a small upward nudge to lift off
            Vector v = player.getVelocity();
            v.setY(0.5); // small vertical boost to get off the ground
            player.setVelocity(v);
        }

        // Initial lift
        Vector velocity = player.getVelocity();
        velocity.setY(liftHeight / 2.0);
        player.setVelocity(velocity);

        // Apply temporary slow-falling potion effect for smooth descent
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
                glideDurationTicks, 0, false, false, true));


        // Glide handler
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks > glideDurationTicks) {
                    cancel();
                    return;
                }

                Location location = player.getLocation();

                Vector vel = player.getVelocity();

                // get the horizontal movement direction
                Vector dir = player.getLocation().getDirection().setY(0).normalize();

                if (!player.isSneaking()) {
                    // normal glide: fast horizontal, slow vertical
                    vel.setX(dir.getX() * 0.8); // adjust multiplier for horizontal speed
                    vel.setZ(dir.getZ() * 0.8);
                    if (vel.getY() < -0.05) vel.setY(-0.05); // slow fall
                } else {
                    // crouching: slower horizontal, faster vertical
                    vel.setX(dir.getX() * 0.2);
                    vel.setZ(dir.getZ() * 0.2);
                    if (vel.getY() > -0.3) vel.setY(-0.5); // faster fall
                }

                player.setVelocity(vel);
                ticks++;

                world.spawnParticle(Particle.CLOUD, location, 1, 0, 0, 0, 0);

            }
        }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, 1L);
    }


}

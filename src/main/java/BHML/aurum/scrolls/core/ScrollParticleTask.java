package BHML.aurum.scrolls.core;

import BHML.aurum.scrolls.core.ScrollUtils;
import BHML.aurum.scrolls.core.ScrollRegistry;
import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.elements.Element;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ScrollParticleTask extends BukkitRunnable{
    private final JavaPlugin plugin;

    public ScrollParticleTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            ItemStack item = player.getInventory().getItemInMainHand();
            Scroll scroll = ScrollUtils.getScrollForItem(item);

            // If not holding a scroll → no particles
            if (scroll == null) {
                continue;
            }

            Element element = scroll.getElement();

            // === CHOOSE PARTICLE ============================
            Particle particle;
            Particle.DustOptions earthColor = new Particle.DustOptions(Color.fromRGB(79, 65, 33), 1.0f);
            Particle.DustOptions airColor   = new Particle.DustOptions(Color.fromRGB(230, 230, 255), 1.0f);

            switch (element) {
                case FIRE      -> particle = Particle.FLAME;
                case WATER     -> particle = Particle.SPLASH;
                case EARTH     -> particle = Particle.DUST;
                case AIR       -> particle = Particle.DUST;
                case LIGHTNING -> particle = Particle.ELECTRIC_SPARK;
                default        -> particle = Particle.HAPPY_VILLAGER;
            }

            // === CAMERA MATH (unchanged from your code) ===
            Location eye = player.getEyeLocation();
            Vector forward = eye.getDirection().clone().normalize();

            Vector worldUp = new Vector(0, 1, 0);
            Vector right = forward.clone().crossProduct(worldUp).normalize();

            if (right.lengthSquared() < 1e-6) {
                float yawRad = (float) Math.toRadians(player.getLocation().getYaw());
                right = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
            }

            Vector upCam = forward.clone().crossProduct(right).normalize();

            double rightDist   = 0.40;
            double upDist      = 0.25;
            double forwardDist = 0.28;

            Vector offset = right.multiply(rightDist)
                    .add(upCam.multiply(upDist))
                    .add(forward.multiply(forwardDist));

            Location spawnLoc = eye.clone().add(offset);

            // == SPAWN PARTICLES =============================
            if (element == Element.LIGHTNING) {
                player.getWorld().spawnParticle(particle, spawnLoc, 1, 0.1, 0.1, 0.1, 0.05);
            }
            else if (element == Element.EARTH) {
                player.getWorld().spawnParticle(Particle.DUST, spawnLoc, 1, 0.1, 0.1, 0.1, earthColor);
            }
            else if (element == Element.WATER) {
                player.getWorld().spawnParticle(particle, spawnLoc, 4, 0.1, 0.1, 0.1, 0.005);
            }
            else if (element == Element.AIR) {
                player.getWorld().spawnParticle(Particle.DUST, spawnLoc, 1, 0.1, 0.1, 0.1, airColor);
            }
            else {
                player.getWorld().spawnParticle(particle, spawnLoc, 1, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }




}

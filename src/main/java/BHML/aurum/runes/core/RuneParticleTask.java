package BHML.aurum.runes.core;

import BHML.aurum.elements.Element;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Particle effect task for items with elemental runes.
 * Displays particles around held items that have elemental runes.
 */
public class RuneParticleTask extends BukkitRunnable {
    private final JavaPlugin plugin;

    // Configurable distances for each item type
    private static final double SWORD_RIGHT_DIST = 0.40;
    private static final double SWORD_UP_DIST = 0.25;
    private static final double SWORD_FORWARD_DIST = 0.30;
    
    private static final double BOW_RIGHT_DIST = 0.45;
    private static final double BOW_UP_DIST = 0.15;
    private static final double BOW_FORWARD_DIST = 0.30;
    
    private static final double PICKAXE_RIGHT_DIST = 0.40;
    private static final double PICKAXE_UP_DIST = 0.25;
    private static final double PICKAXE_FORWARD_DIST = 0.28;

    public RuneParticleTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            Element element = getElementalRuneElement(item);

            // If no elemental rune → no particles
            if (element == null) {
                continue;
            }

            // === CHOOSE PARTICLE ============================
            Particle particle;
            Particle.DustOptions earthColor = new Particle.DustOptions(Color.fromRGB(79, 65, 33), 1.0f);
            Particle.DustOptions airColor = new Particle.DustOptions(Color.fromRGB(0, 255, 185), 1.0f);
            Particle.DustOptions enderColor = new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0f);
            
            switch (element) {
                case FIRE      -> particle = Particle.FLAME;
                case WATER     -> particle = Particle.SPLASH;
                case EARTH     -> particle = Particle.DUST;
                case AIR       -> particle = Particle.DUST;
                case LIGHTNING -> particle = Particle.ELECTRIC_SPARK;
                case ENDER     -> particle = Particle.DUST;
                default        -> particle = Particle.HAPPY_VILLAGER;
            }

            // === CAMERA MATH ============================
            Location eye = player.getEyeLocation();
            Vector forward = eye.getDirection().clone().normalize();

            Vector worldUp = new Vector(0, 1, 0);
            Vector right = forward.clone().crossProduct(worldUp).normalize();

            if (right.lengthSquared() < 1e-6) {
                float yawRad = (float) Math.toRadians(player.getLocation().getYaw());
                right = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
            }

            Vector upCam = forward.clone().crossProduct(right).normalize();

            // Get distances based on item type
            String itemType = getItemType(item);
            double rightDist = getRightDistance(itemType);
            double upDist = getUpDistance(itemType);
            double forwardDist = getForwardDistance(itemType);

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
            else if (element == Element.ENDER) {
                player.getWorld().spawnParticle(particle, spawnLoc, 1, 0.1, 0.1, 0.1, enderColor);
            }
            else {
                player.getWorld().spawnParticle(particle, spawnLoc, 1, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    /**
     * Gets the element of the first elemental rune on the item
     * @param item The item to check
     * @return Element if found, null if no elemental rune
     */
    private Element getElementalRuneElement(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        for (String id : RuneUtils.getRuneIds(item)) {
            Rune rune = RuneRegistry.getRune(id);
            if (rune != null && RuneLimits.isElemental(rune.getElement())) {
                return rune.getElement();
            }
        }
        return null;
    }

    /**
     * Gets the item type for distance configuration
     * @param item The item to check
     * @return Item type string
     */
    private String getItemType(ItemStack item) {
        if (item == null) return "unknown";
        
        String type = item.getType().name();
        if (type.contains("SWORD")) return "sword";
        if (type.contains("BOW")) return "bow";
        if (type.contains("PICKAXE")) return "pickaxe";
        return "unknown";
    }

    /**
     * Gets the right distance for the given item type
     */
    private double getRightDistance(String itemType) {
        return switch (itemType.toLowerCase()) {
            case "sword" -> SWORD_RIGHT_DIST;
            case "bow" -> BOW_RIGHT_DIST;
            case "pickaxe" -> PICKAXE_RIGHT_DIST;
            default -> 0.40;
        };
    }

    /**
     * Gets the up distance for the given item type
     */
    private double getUpDistance(String itemType) {
        return switch (itemType.toLowerCase()) {
            case "sword" -> SWORD_UP_DIST;
            case "bow" -> BOW_UP_DIST;
            case "pickaxe" -> PICKAXE_UP_DIST;
            default -> 0.25;
        };
    }

    /**
     * Gets the forward distance for the given item type
     */
    private double getForwardDistance(String itemType) {
        return switch (itemType.toLowerCase()) {
            case "sword" -> SWORD_FORWARD_DIST;
            case "bow" -> BOW_FORWARD_DIST;
            case "pickaxe" -> PICKAXE_FORWARD_DIST;
            default -> 0.28;
        };
    }
}

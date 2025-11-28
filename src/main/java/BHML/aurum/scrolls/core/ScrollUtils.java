package BHML.aurum.scrolls.core;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.utils.Keys;

import BHML.aurum.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;


public class ScrollUtils {
    // ----- CREATE SCROLL ITEM -----
    public static ItemStack createScrollItem(Scroll scroll) {

        ItemStack item = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta meta = item.getItemMeta();


        // Hide everything from the tooltip
        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,   // hides attack damage, armor, etc.
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_DYE,          // hides pattern/dye info
                ItemFlag.HIDE_PLACED_ON,    // hides can-place-on info
                ItemFlag.HIDE_DESTROYS,       // hides can-destroy info
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP

        );

        Element element = scroll.getElement();
        TextColor elementColor = element.getColor();

        meta.displayName(Component.text(scroll.getId().substring(0,1).toUpperCase()
                + scroll.getId().substring(1), elementColor).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));

        List<Component> lore = new ArrayList<>();
        lore.addAll(TextUtils.wrapLore(scroll.getDescription(), 50, NamedTextColor.WHITE));
        //lore.add(Component.empty());
        lore.add(Component.text(  scroll.getMaxUses() + "/" + scroll.getMaxUses() + " Uses", NamedTextColor.GOLD).decoration((TextDecoration.ITALIC), false));

        meta.lore(lore);

        // Persistent data
        meta.getPersistentDataContainer().set(Keys.SCROLL_ID, PersistentDataType.STRING, scroll.getId());
        meta.getPersistentDataContainer().set(Keys.SCROLL_USES, PersistentDataType.INTEGER, scroll.getMaxUses());
        meta.getPersistentDataContainer().set(Keys.SCROLL_MAX_USES, PersistentDataType.INTEGER, scroll.getMaxUses());

        item.setItemMeta(meta);
        return item;
    }

    // ----- GET CURRENT USES -----
    public static int getUses(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(Keys.SCROLL_USES, PersistentDataType.INTEGER, 0);
    }

    // ----- SET USES -----
    public static void setUses(ItemStack item, int amount) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int max = meta.getPersistentDataContainer().get(Keys.SCROLL_MAX_USES, PersistentDataType.INTEGER);
        amount = Math.max(0, Math.min(amount, max));

        meta.getPersistentDataContainer().set(Keys.SCROLL_USES, PersistentDataType.INTEGER, amount);

        // get existing lore safely
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        if (lore.isEmpty()) {
            // if lore somehow vanished, rebuild it so we don't index crash
            lore.add(Component.text(""));
            lore.add(Component.text(""));
        }

        // ALWAYS update the LAST line
        int index = lore.size() - 1;

        lore.set(index,
                Component.text(amount + "/" + max + " Uses", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    // ----- GET SCROLL ID -----
    public static String getScrollId(ItemStack item) {
        if (item == null) return null;
        if (!item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        if (!meta.getPersistentDataContainer().has(Keys.SCROLL_ID, PersistentDataType.STRING))
            return null;

        return meta.getPersistentDataContainer().get(Keys.SCROLL_ID, PersistentDataType.STRING);
    }

    public static Scroll getScrollForItem(ItemStack item) {
        String id = getScrollId(item);
        return id == null ? null : ScrollRegistry.get(id);
    }







    // OLD MAGIC HIT DETECTION STUFF


    public static void applySpellDamage(Player caster, LivingEntity target, double damage) {


        target.setNoDamageTicks(0);  // ensures spell hits reliably
        target.damage(damage);

    }

    public static boolean hasClearShot(Player player, LivingEntity target) {
        World world = player.getWorld();
        Location from = player.getEyeLocation();

        BoundingBox box = target.getBoundingBox();
        double inset = 0.05;

        double minX = box.getMinX() + inset;
        double maxX = box.getMaxX() - inset;
        double minY = box.getMinY() + inset;
        double maxY = box.getMaxY() - inset;
        double minZ = box.getMinZ() + inset;
        double maxZ = box.getMaxZ() - inset;

        int samplesX = 3;
        int samplesY = 5;
        int samplesZ = 3;

        for (int x = 0; x < samplesX; x++) {
            double sampleX = minX + (x / (double)(samplesX - 1)) * (maxX - minX);
            for (int y = 0; y < samplesY; y++) {
                double sampleY = minY + (y / (double)(samplesY - 1)) * (maxY - minY);
                for (int z = 0; z < samplesZ; z++) {
                    double sampleZ = minZ + (z / (double)(samplesZ - 1)) * (maxZ - minZ);

                    Location to = new Location(world, sampleX, sampleY, sampleZ);

                    RayTraceResult result = world.rayTrace(
                            from,
                            to.toVector().subtract(from.toVector()),
                            from.distance(to),
                            FluidCollisionMode.NEVER,
                            true, // only collidable blocks (ignores grass, flowers, etc)
                            0.1,
                            (e) -> false // no entity checks
                    );

                    if (result == null) {
                        return true; // No block hit = clear shot
                    } else {
                        Block hitBlock = result.getHitBlock();
                        if (hitBlock == null || hitBlock.isPassable()) {
                            continue; // Still acceptable
                        }
                    }
                }
            }
        }

        return false; // All points blocked
    }


    //Hit Detection for Explosive AoE
    public static boolean isExposedTo(Location origin, LivingEntity target) {
        World world = origin.getWorld();
        if (world == null) return false;

        Location targetLoc = target.getEyeLocation();
        Vector direction = targetLoc.toVector().subtract(origin.toVector());
        double distance = direction.length();

        RayTraceResult result = world.rayTraceBlocks(
                origin,
                direction.normalize(),
                distance,
                FluidCollisionMode.NEVER,
                true // ignore passable blocks like grass, flowers, air
        );

        return result == null; // if null, nothing blocked the view = exposed
    }

    private static boolean isActuallyPassable(Block block) {
        if (block == null) return true;

        if (block.isPassable()) return true;

        Material type = block.getType();
        String name = type.name();

        // Check if block is any type of sign or wall sign
        if (name.endsWith("_SIGN") || name.endsWith("_WALL_SIGN")) {
            return true;
        }

        return switch (type) {
            case TALL_GRASS, SHORT_GRASS, FLOWER_POT, SUNFLOWER, LILY_PAD, VINE,
                 DEAD_BUSH, FERN, SWEET_BERRY_BUSH, CARROTS, WHEAT, POTATO, BEETROOTS, LARGE_FERN -> true;
            default -> false;
        };
    }


    // Keeps track of which targets each caster has already been notified about
    private static final Map<UUID, Set<UUID>> blockedTargetsMap = new HashMap<>();

    /**
     * Checks targeting rules and, if blocked, sends a single notification per target per cast.
     * Returns true if the target may be hit.
     */
    public static boolean handleBlockedTargetFeedback(Player caster, LivingEntity target) {


        // If blocked, notify only once per cast
        UUID casterId = caster.getUniqueId();
        blockedTargetsMap.putIfAbsent(casterId, new HashSet<>());
        Set<UUID> blocked = blockedTargetsMap.get(casterId);

        UUID targetId = target.getUniqueId();
        if (!blocked.contains(targetId)) {
            blocked.add(targetId);
            if (target != caster)
                caster.sendMessage("§cBlocked hit on: §7" + target.getType().name());
        }

        return false;
    }

    /**
     * Clears the blocked-targets memory for this caster.
     * Call this once at the end of each spell cast.
     */
    public static void clearBlockedTargets(Player caster) {
        blockedTargetsMap.remove(caster.getUniqueId());
    }





}

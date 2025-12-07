package BHML.aurum.scrolls.ender;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.scrolls.core.ScrollUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static BHML.aurum.scrolls.earth.TerraPath.REPLACEABLE;

public class Tether implements Scroll {


    private static final int RADIUS = 10;
    private static final long DESPAWN_TIME_TICKS = 3600; // 3 minutes
    private static final int BUFF_TASK_PERIOD = 2; // run every 2 ticks (0.1s)
    private static final int BUFF_DURATION_TICKS = 6; // short duration so removal is essentially immediate
    private static final int REGEN_AMPLIFIER = 4; // Regeneration V
    private static final int STRENGTH_AMPLIFIER = 0; // Strength I

    @Override
    public Element getElement() { return Element.ENDER; }

    @Override
    public String getId() { return "tether"; }

    @Override
    public String getName() { return "Tether"; }

    @Override
    public int getMaxUses() { return 5; }

    @Override
    public int getGoldCost() { return 2; }

    @Override
    public int getRestoreAmount() { return 1; }

    @Override
    public int getCooldown() { return 10000; }

    @Override
    public String getDescription() {
        return "Summon a tether that links to you while in range, giving massive buffs";
    }

    @Override
    public void cast(Player player) {

        Block target = player.getTargetBlockExact(8);
        UUID ownerId = player.getUniqueId();

        if (target == null) {
            player.sendMessage(ChatColor.RED + "You must target a block.");
            return;
        }

        // If clicking short grass / thin passables, treat the block below as the target
        if (target.getType() == Material.SHORT_GRASS
                || target.getType() == Material.TALL_GRASS
                || target.getType() == Material.FERN
                || target.getType() == Material.DEAD_BUSH) {
            target = target.getRelative(BlockFace.DOWN);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "You must target a block.");
                return;
            }
        }

        if (!target.getType().isSolid()) {
            player.sendMessage(ChatColor.RED + "You must target solid ground.");
            return;
        }

        // positions:
        // obs1 = y+1, obs2 = y+2, crystal sits at y+3 (entity), there must be an empty block at y+4 for hitbox
        Block obs1 = target.getRelative(BlockFace.UP, 1);
        Block obs2 = target.getRelative(BlockFace.UP, 2);
        Block crystalSpace = target.getRelative(BlockFace.UP, 3); // block where crystal entity occupies
        Block crystalSpaceAbove = target.getRelative(BlockFace.UP, 4); // must be empty for hitbox

        // disallow placement if any of these are liquids
        if (obs1.isLiquid() || obs2.isLiquid() || crystalSpace.isLiquid() || crystalSpaceAbove.isLiquid()) {
            player.sendMessage(ChatColor.RED + "You cannot place a Tether Tower in or under liquid.");
            return;
        }

        // ensure replaceable for block positions and empty for crystal spaces
        if (!canPlace(obs1) || !canPlace(obs2) || !canPlace(crystalSpace) || !canPlace(crystalSpaceAbove)) {
            player.sendMessage(ChatColor.RED + "No space to create the Tether Tower!");
            return;
        }

        // Place obsidian blocks and register them
        List<Block> towerBlocks = new ArrayList<>();
        placeBlock(obs1, Material.OBSIDIAN, towerBlocks);
        placeBlock(obs2, Material.OBSIDIAN, towerBlocks);

        // spawn end crystal entity slightly centered
        Location crystalLoc = crystalSpace.getLocation().add(0.5, 0.0, 0.5);
        EnderCrystal crystal = (EnderCrystal) player.getWorld().spawnEntity(crystalLoc, EntityType.END_CRYSTAL);

        // make crystal invulnerable and non-damaging (optional)
        crystal.setInvulnerable(true);
        crystal.setPersistent(true);

        // track crystal as well via block location (for cleanup we store the entity)
        // We'll store the crystal reference separately
        // protect the obsidian blocks (end crystal is an entity, so no block protection needed)
        // registerProtected was called inside placeBlock already

        // Start tether task (buffs & particle tether to owner)
        int taskId = startTetherTask(ownerId, crystal);

        // Schedule despawn after 3 minutes (remove blocks & crystal, cancel task)
        scheduleDespawn(towerBlocks, crystal, taskId);

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.2f);
    }

    // Validate placement BEFORE casting (mirrors cast checks). Use in your listener to avoid consuming uses.
    public boolean wasLastTowerPlacedSuccessfully(Player player) {
        Block target = player.getTargetBlockExact(8);
        if (target == null) return false;

        if (target.getType() == Material.SHORT_GRASS
                || target.getType() == Material.TALL_GRASS
                || target.getType() == Material.FERN
                || target.getType() == Material.DEAD_BUSH) {
            target = target.getRelative(BlockFace.DOWN);
            if (target == null) return false;
        }

        if (!target.getType().isSolid()) return false;

        Block obs1 = target.getRelative(BlockFace.UP, 1);
        Block obs2 = target.getRelative(BlockFace.UP, 2);
        Block crystalSpace = target.getRelative(BlockFace.UP, 3);
        Block crystalSpaceAbove = target.getRelative(BlockFace.UP, 4);

        if (obs1.isLiquid() || obs2.isLiquid() || crystalSpace.isLiquid() || crystalSpaceAbove.isLiquid()) return false;

        return canPlace(obs1) && canPlace(obs2) && canPlace(crystalSpace) && canPlace(crystalSpaceAbove);
    }

    private boolean canPlace(Block block) {
        if (block == null) return false;

        Material t = block.getType();

        if (block.isLiquid()) return false;

        if (block.isEmpty() || block.isPassable() || REPLACEABLE.contains(t)) return true;

        return false;
    }

    private void placeBlock(Block block, Material material, List<Block> list) {
        block.setType(material);
        list.add(block);
        registerProtected(block);
    }

    private int startTetherTask(UUID ownerId, EnderCrystal crystal) {

        return new BukkitRunnable() {
            @Override
            public void run() {
                if (crystal == null || crystal.isDead()) {
                    cancel();
                    return;
                }

                World world = crystal.getWorld();
                if (world == null) {
                    cancel();
                    return;
                }

                // find owner player
                Player owner = Bukkit.getPlayer(ownerId);
                if (owner == null || !owner.isOnline()) {
                    // owner offline -> no tether visuals / buffs (task continues until despawn)
                    return;
                }

                Location crystalEye = crystal.getLocation().clone().add(0.0, 1.0, 0.0); // small lift for better beam origin
                Location ownerEye = owner.getEyeLocation().clone().add(0.0, -1.0, 0.0); // Lowered so doesnt block vision!

                // distance check
                if (owner.getLocation().distanceSquared(crystal.getLocation()) > (long) RADIUS * RADIUS) {
                    // out of range -> do not apply buffs and do not draw beam
                    return;
                }

                // line-of-sight check (rely on your ScrollUtils)
                if (!ScrollUtils.isExposedTo(crystalEye, owner)) {
                    return;
                }

                // draw tether beam from crystal to owner eye
                drawBeam(world, crystalEye, ownerEye, Particle.END_ROD);

                // apply short buffs (refreshed each tick)
                owner.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, BUFF_DURATION_TICKS, REGEN_AMPLIFIER, false, false, true));
                owner.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, BUFF_DURATION_TICKS, STRENGTH_AMPLIFIER, false, false, true));
            }
        }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 0L, BUFF_TASK_PERIOD).getTaskId();
    }

    private void scheduleDespawn(List<Block> towerBlocks, EnderCrystal crystal, int taskId) {

        new BukkitRunnable() {
            @Override
            public void run() {

                // cancel tether task
                Bukkit.getScheduler().cancelTask(taskId);

                // remove crystal entity safely
                if (crystal != null && !crystal.isDead()) {
                    crystal.remove();
                }

                // unregister protection & remove blocks we placed (only if they still match)
                unregisterProtected(towerBlocks);
                for (Block block : towerBlocks) {
                    if (block.getType() == Material.OBSIDIAN) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(JavaPlugin.getPlugin(Aurum.class), DESPAWN_TIME_TICKS);
    }

    // beam drawing method (reusable) - supports choosing particle type
    private void drawBeam(World world, Location from, Location to, Particle particle) {

        Vector dir = to.toVector().subtract(from.toVector());
        double dist = dir.length();
        dir.normalize();

        double step = 0.2;

        for (double d = 0; d < dist; d += step) {
            Location point = from.clone().add(dir.clone().multiply(d));
            world.spawnParticle(Particle.DUST, point, 0, new Particle.DustOptions(Color.PURPLE, 1.0f));
        }
    }

    // Protected tower block tracking
    private static final Set<Location> PROTECTED_TOWER_BLOCKS = new HashSet<>();

    private void registerProtected(Block block) {
        PROTECTED_TOWER_BLOCKS.add(block.getLocation());
    }

    private void unregisterProtected(List<Block> blocks) {
        for (Block b : blocks) {
            PROTECTED_TOWER_BLOCKS.remove(b.getLocation());
        }
    }

    public static boolean isProtectedTowerBlock(Block block) {
        return PROTECTED_TOWER_BLOCKS.contains(block.getLocation());
    }


}

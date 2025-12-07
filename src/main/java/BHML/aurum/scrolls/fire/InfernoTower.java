package BHML.aurum.scrolls.fire;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.scrolls.core.ScrollUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static BHML.aurum.scrolls.earth.TerraPath.REPLACEABLE;

public class InfernoTower implements Scroll {
    private static final int RADIUS = 10;
    private static final int DAMAGE_PER_TICK = 2; // 2 damage every 10 ticks = 4/sec
    private static final long DESPAWN_TIME_TICKS = 3600; // 3 minutes

    @Override
    public Element getElement() {
        return Element.FIRE;
    }

    @Override
    public String getId() {
        return "infernotower";
    }

    @Override
    public String getName() {
        return "Inferno Tower";
    }

    @Override
    public int getMaxUses() {
        return 10;
    }

    @Override
    public int getGoldCost() { return 1; }
    @Override
    public int getRestoreAmount() { return 2; }

    @Override
    public int getCooldown() {
        return 10000; // 10s
    }

    @Override
    public String getDescription(){
        return "Summon a tower that emits fire at all enemies within range";
    }

    @Override
    public void cast(Player player) {

        Block target = player.getTargetBlockExact(8);
        UUID playerID = player.getUniqueId();

        //Allow on short grass
        if (target.getType() == Material.SHORT_GRASS) {
            target = target.getRelative(BlockFace.DOWN);
        }

        //not allowed in water
        Block above = target.getRelative(BlockFace.UP);
        if (above.isLiquid()) {
            player.sendMessage(ChatColor.RED + "You cannot place a Fire Tower underwater or in lava.");
            return;
        }

        if (target == null || !target.getType().isSolid()) {
            player.sendMessage(ChatColor.RED + "You must target a solid block.");
            return;
        }


        Block b1 = target.getRelative(BlockFace.UP, 1);
        Block b2 = target.getRelative(BlockFace.UP, 2);
        Block b3 = target.getRelative(BlockFace.UP, 3);
        Block candleBlock = target.getRelative(BlockFace.UP, 4);

        if (b1.isLiquid() || b2.isLiquid() || b3.isLiquid() || candleBlock.isLiquid()) {
            player.sendMessage(ChatColor.RED + "There is liquid blocking the tower.");
            return;
        }

        if (!canPlace(b1) || !canPlace(b2) || !canPlace(b3) || !canPlace(candleBlock)) {
            player.sendMessage(ChatColor.RED + "No space to create the Fire Tower!");
            return;
        }

        // Keep track of placed blocks to remove later
        List<Block> towerBlocks = new ArrayList<>();

        // Place nether fences
        placeBlock(b1, Material.NETHER_BRICK_FENCE, towerBlocks);
        placeBlock(b2, Material.NETHER_BRICK_FENCE, towerBlocks);
        placeBlock(b3, Material.NETHER_BRICK_FENCE, towerBlocks);

        // Place lit candle
        candleBlock.setType(Material.RED_CANDLE);
        Candle candleData = (Candle) candleBlock.getBlockData();
        candleData.setLit(true);
        candleBlock.setBlockData(candleData);
        towerBlocks.add(candleBlock);
        registerProtected(candleBlock);

        Location candleLoc = candleBlock.getLocation().add(0.5, 0.5, 0.5);

        // Start damage/beam task
        int taskId = startDamageTask(player, playerID, candleLoc);

        // Schedule despawn after 3 minutes
        scheduleDespawn(towerBlocks, taskId);
    }


    public boolean wasLastTowerPlacedSuccessfully(Player player) {
        Block target = player.getTargetBlockExact(8);

        if (target == null) return false;

        // If player clicked on a thin/passable surface like SHORT_GRASS, treat the block below as the "ground"
        if (target.getType() == Material.SHORT_GRASS
                || target.getType() == Material.FERN
                || target.getType() == Material.DEAD_BUSH) {
            target = target.getRelative(BlockFace.DOWN);
            if (target == null) return false;
        }

        // Now target must be a solid block we can build on
        if (!target.getType().isSolid()) return false;

        // Compute the positions the tower will occupy
        Block b1 = target.getRelative(BlockFace.UP, 1);
        Block b2 = target.getRelative(BlockFace.UP, 2);
        Block b3 = target.getRelative(BlockFace.UP, 3);
        Block candleBlock = target.getRelative(BlockFace.UP, 4);

        // Ensure none of these blocks are liquids (no underwater placement)
        if (b1.isLiquid() || b2.isLiquid() || b3.isLiquid() || candleBlock.isLiquid()) return false;

        // Ensure all positions are replaceable (air, grass, flowers, etc.) — rely on your REPLACEABLE set
        return canPlace(b1) && canPlace(b2) && canPlace(b3) && canPlace(candleBlock);
    }


    private boolean canPlace(Block block) {
        if (block == null) return false;

        Material t = block.getType();

        if (block.isLiquid()) return false;

        // Allow anything that is empty, passable, or in the REPLACEABLE list
        if (block.isEmpty() || block.isPassable() || REPLACEABLE.contains(t)) {
            return true;
        }

        return false;
    }

    private void placeBlock(Block block, Material material, List<Block> list) {
        block.setType(material);
        list.add(block);
        registerProtected(block);
    }

    private int startDamageTask(Player player, UUID ownerId, Location candleLoc) {

        return new BukkitRunnable() {
            @Override
            public void run() {
                World world = candleLoc.getWorld();
                Location eye = candleLoc.clone().add(0.0, 2.0, 0.0);
                Location eye2 = candleLoc.clone().add(0.1, 0.3, 0.1);
                Location eye3 = candleLoc.clone().add(-0.1, 0.3, 0.1);
                Location eye4 = candleLoc.clone().add(0.1, 0.3, -0.1);
                Location eye5 = candleLoc.clone().add(-0.1, 0.3, -0.1);
                if (world == null) return;

                for (LivingEntity target : world.getLivingEntities()) {
                    if (target.getUniqueId().equals(ownerId)) continue;
                    if (target.isDead()) continue;
                    if (target.getLocation().distanceSquared(candleLoc) > RADIUS * RADIUS) continue;

                    // clear shot check
                    if (!ScrollUtils.isExposedTo(eye, target) && !ScrollUtils.isExposedTo(eye2, target)
                    && !ScrollUtils.isExposedTo(eye3, target) && !ScrollUtils.isExposedTo(eye4, target)
                    && !ScrollUtils.isExposedTo(eye5, target))
                        {
                            continue;
                        }

                    // draw visual beam
                    drawBeam(world, candleLoc, target.getEyeLocation().clone().add(0.0, -0.5, 0.0));

                    // damage
                    target.damage(DAMAGE_PER_TICK, player);
                }
            }

        }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 10, 10).getTaskId();
    }

    private void scheduleDespawn(List<Block> towerBlocks, int damageTaskId) {

        new BukkitRunnable() {
            @Override
            public void run() {
                unregisterProtected(towerBlocks);

                // Stop the recurring damage/beam task
                Bukkit.getScheduler().cancelTask(damageTaskId);

                // Remove tower blocks IF they are still the expected blocks
                for (Block block : towerBlocks) {
                    if (block.getType() == Material.NETHER_BRICK_FENCE ||
                            block.getType() == Material.RED_CANDLE) {
                        block.setType(Material.AIR);
                    }
                }
            }

        }.runTaskLater(JavaPlugin.getPlugin(Aurum.class), DESPAWN_TIME_TICKS); // 3 minutes

    }

    private void drawBeam(World world, Location from, Location to) {

        Vector dir = to.toVector().subtract(from.toVector());
        double dist = dir.length();
        dir.normalize();

        double step = 0.2;

        for (double d = 0; d < dist; d += step) {
            Location point = from.clone().add(dir.clone().multiply(d));
            world.spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0);
        }
    }


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

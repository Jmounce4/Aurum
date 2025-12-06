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

import java.util.ArrayList;
import java.util.List;

public class FireTower implements Scroll {
    private static final int RADIUS = 10;
    private static final int DAMAGE_PER_TICK = 2; // 2 damage every 10 ticks = 4/sec
    private static final long DESPAWN_TIME_TICKS = 3600; // 3 minutes

    @Override
    public Element getElement() {
        return Element.FIRE;
    }

    @Override
    public String getId() {
        return "firetower";
    }

    @Override
    public String getName() {
        return "Fire Tower";
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

        if (target == null || !target.getType().isSolid()) {
            player.sendMessage(ChatColor.RED + "You must target a solid block.");
            return;
        }

        Block b1 = target.getRelative(BlockFace.UP, 1);
        Block b2 = target.getRelative(BlockFace.UP, 2);
        Block b3 = target.getRelative(BlockFace.UP, 3);
        Block candleBlock = target.getRelative(BlockFace.UP, 4);

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

        Location candleLoc = candleBlock.getLocation().add(0.5, 0.5, 0.5);

        // Start damage/beam task
        int taskId = startDamageTask(player, candleLoc);

        // Schedule despawn after 3 minutes
        scheduleDespawn(towerBlocks, taskId);
    }


    public boolean wasLastTowerPlacedSuccessfully(Player player) {
        Block target = player.getTargetBlockExact(8);

        if (target == null || !target.getType().isSolid()) {
            return false;
        }

        Block b1 = target.getRelative(BlockFace.UP, 1);
        Block b2 = target.getRelative(BlockFace.UP, 2);
        Block b3 = target.getRelative(BlockFace.UP, 3);
        Block candleBlock = target.getRelative(BlockFace.UP, 4);

        // Must be actual AIR/PASSABLE, not replaced yet
        return canPlace(b1) && canPlace(b2) && canPlace(b3) && canPlace(candleBlock);
    }


    private boolean canPlace(Block block) {
        return block.isEmpty() || block.isPassable();
    }

    private void placeBlock(Block block, Material material, List<Block> list) {
        block.setType(material);
        list.add(block);
    }

    private int startDamageTask(Player owner, Location candleLoc) {

        return new BukkitRunnable() {
            @Override
            public void run() {
                World world = candleLoc.getWorld();
                Location eye = candleLoc.clone().add(0.0, 2.7, 0.0);
                if (world == null) return;

                for (LivingEntity target : world.getLivingEntities()) {
                    if (target == owner) continue;
                    if (target.isDead()) continue;
                    if (target.getLocation().distanceSquared(candleLoc) > RADIUS * RADIUS) continue;

                    // clear shot check
                    if (!ScrollUtils.isExposedTo(eye, target)) continue;

                    // draw visual beam
                    drawBeam(world, candleLoc, target.getEyeLocation());

                    // damage
                    target.damage(DAMAGE_PER_TICK, owner);
                }
            }

        }.runTaskTimer(JavaPlugin.getPlugin(Aurum.class), 10, 10).getTaskId();
    }

    private void scheduleDespawn(List<Block> towerBlocks, int damageTaskId) {

        new BukkitRunnable() {
            @Override
            public void run() {

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


}

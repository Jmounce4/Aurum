package BHML.aurum.scrolls.water;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Geyser implements Scroll {

    private final int maxHeight = 15;
    private final int cleanupDelayTicks = 100; // 5 seconds

    @Override
    public Element getElement() { return Element.WATER; }

    @Override
    public String getId() { return "geyser"; }

    @Override
    public String getName() { return "Geyser"; }

    @Override
    public int getMaxUses() { return 20; }

    @Override
    public String getDescription() {
        return "Creates a vertical water geyser that launches you upward";
    }

    @Override
    public int getGoldCost() { return 1; }

    @Override
    public int getRestoreAmount() { return 5; }

    @Override
    public int getCooldown() { return 1000; }


    @Override
    public void cast(Player player) {

        Location base = player.getLocation().getBlock().getLocation();
        World world = base.getWorld();

        List<Block> geyserBlocks = new ArrayList<>();

        // Direction vectors for the plus shape
        Vector[] dirs = new Vector[]{
                new Vector(1, 0, 0),
                new Vector(-1, 0, 0),
                new Vector(0, 0, 1),
                new Vector(0, 0, -1)
        };

        boolean[] openDir = new boolean[]{true, true, true, true};

        int highest = 0;

        for (int y = 1; y <= maxHeight; y++) {

            Location centerLoc = base.clone().add(0, y, 0);
            Block centerBlock = centerLoc.getBlock();

            // If center is blocked, the entire geyser stops here
            if (!isAirOrWater(centerBlock)) {
                break;
            }

            // Place center water
            centerBlock.setType(Material.WATER);
            geyserBlocks.add(centerBlock);

            // Place directional water arms
            /*for (int i = 0; i < dirs.length; i++) {

                if (!openDir[i]) continue;

                Location sideLoc = base.clone()
                        .add(dirs[i].getX(), y, dirs[i].getZ());

                Block sideBlock = sideLoc.getBlock();

                if (!isAirOrWater(sideBlock)) {
                    openDir[i] = false; // stop this arm
                    continue;
                }

                sideBlock.setType(Material.WATER);
                geyserBlocks.add(sideBlock);
            }*/

            highest = y;
        }

        // Launch player upward to top
        Location launchLoc = base.clone().add(0, highest - 0.5, 0);
        //player.teleport(launchLoc);
        player.setVelocity(new Vector(0, 5, 0)); // little extra hop

        // Cleanup geyser after delay
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Aurum.class), () -> {
            for (Block b : geyserBlocks) {
                if (b.getType() == Material.WATER) {
                    b.setType(Material.AIR);
                }
            }
        }, cleanupDelayTicks);
    }

    private boolean isAirOrWater(Block b) {
        return b.getType() == Material.AIR || b.getType() == Material.WATER;
    }



}

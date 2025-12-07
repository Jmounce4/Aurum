package BHML.aurum.listeners;

import BHML.aurum.scrolls.ender.Tether;
import BHML.aurum.scrolls.fire.InfernoTower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TowerListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (InfernoTower.isProtectedTowerBlock(event.getBlock())) {
            event.setCancelled(true);
        }
        if (Tether.isProtectedTowerBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(InfernoTower::isProtectedTowerBlock);
        event.blockList().removeIf(Tether::isProtectedTowerBlock);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(InfernoTower::isProtectedTowerBlock);
        event.blockList().removeIf(Tether::isProtectedTowerBlock);
    }


}

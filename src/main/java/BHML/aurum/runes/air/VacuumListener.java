package BHML.aurum.runes.air;

import BHML.aurum.runes.core.RuneUtils;
import BHML.aurum.runes.core.RuneRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VacuumListener implements Listener {
    
    private static final int VACUUM_RADIUS = 5;
    private static final double PULL_STRENGTH = 0.3;
    
    private final Map<UUID, BukkitRunnable> vacuumTasks = new HashMap<>();
    
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        // Cancel existing task
        BukkitRunnable existingTask = vacuumTasks.get(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
            vacuumTasks.remove(player.getUniqueId());
            player.removePotionEffect(PotionEffectType.HASTE);
        }
        
        // Check if new item has vacuum rune
        if (hasVacuumRune(newItem)) {
            // Add haste
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 0, false, false, false));
            
            // Start vacuum task
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    // Still holding the pickaxe?
                    ItemStack current = player.getInventory().getItemInMainHand();
                    if (!hasVacuumRune(current)) {
                        this.cancel();
                        vacuumTasks.remove(player.getUniqueId());
                        player.removePotionEffect(PotionEffectType.HASTE);
                        return;
                    }
                    
                    // Pull nearby items
                    for (Item item : player.getWorld().getEntitiesByClass(Item.class)) {
                        if (item.getLocation().distance(player.getLocation()) <= VACUUM_RADIUS) {
                            Vector direction = player.getLocation().toVector()
                                .subtract(item.getLocation().toVector())
                                .normalize()
                                .multiply(PULL_STRENGTH);
                            item.setVelocity(direction);
                        }
                    }
                }
            };
            
            task.runTaskTimer(org.bukkit.Bukkit.getPluginManager().getPlugin("Aurum"), 0L, 2L);
            vacuumTasks.put(player.getUniqueId(), task);
        }
    }
    
    private boolean hasVacuumRune(ItemStack item) {
        if (item == null) return false;
        if (!item.getType().name().contains("PICKAXE")) return false;
        return RuneUtils.hasRune(item, RuneRegistry.getRune("vacuum"));
    }
}

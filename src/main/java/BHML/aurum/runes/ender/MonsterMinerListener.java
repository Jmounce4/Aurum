package BHML.aurum.runes.ender;

import BHML.aurum.runes.core.RuneUtils;
import BHML.aurum.runes.core.RuneRegistry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Handles the Monster Miner rune functionality:
 * - Allows mining mob spawners with silk touch effect
 * - Drops the spawner as an item
 * - Breaks the pickaxe after one use
 * - Only works with pickaxes that have Monster Miner rune
 */
public class MonsterMinerListener implements Listener {
    
    private final NamespacedKey spawnerTypeKey;
    
    // Allowed overworld mob types for Monster Miner
    private static final EntityType[] ALLOWED_MOBS = {
        EntityType.ZOMBIE,
        EntityType.SKELETON,
        EntityType.SPIDER,
        EntityType.CAVE_SPIDER,
        EntityType.SILVERFISH
    };
    
    public MonsterMinerListener(JavaPlugin plugin) {
        this.spawnerTypeKey = new NamespacedKey(plugin, "spawner_type");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack placedItem = event.getItemInHand();
        
        // Check if placed item is a spawner with our custom data
        if (placedItem.getType() != Material.SPAWNER) return;
        if (!placedItem.hasItemMeta()) return;
        
        PersistentDataContainer pdc = placedItem.getItemMeta().getPersistentDataContainer();
        String mobTypeString = pdc.get(spawnerTypeKey, PersistentDataType.STRING);
        
        if (mobTypeString != null) {
            // Convert to EntityType
            EntityType mobType;
            try {
                mobType = EntityType.valueOf(mobTypeString);
            } catch (IllegalArgumentException e) {
                return; // Invalid mob type, skip
            }
            
            // Get the placed spawner block and set its type
            Block block = event.getBlockPlaced();
            if (block.getState() instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                spawner.setSpawnedType(mobType);
                spawner.update(); // Apply changes to the block
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        
        // Check if player is holding a pickaxe with Monster Miner rune
        if (!hasMonsterMinerRune(pickaxe)) return;
        
        Block block = event.getBlock();
        
        // Only work on mob spawners
        if (block.getType() != Material.SPAWNER) return;
        
        // Get the spawner state to check mob type
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        EntityType mobType = spawner.getSpawnedType();
        
        // Check if mob type is allowed
        if (!isAllowedMobType(mobType)) {
            player.sendMessage("§cMonster Miner cannot mine this type of spawner!");
            return;
        }
        
        // Cancel the normal break event to prevent default behavior
        event.setCancelled(true);
        
        // Create a new spawner with the correct mob type
        ItemStack spawnerItem = createSpawnerItem(mobType);
        
        // Drop the specific spawner as an item
        block.getWorld().dropItem(block.getLocation(), spawnerItem);
        
        // Remove the block
        block.setType(Material.AIR);
        
        // Completely remove the pickaxe from inventory
        completelyRemovePickaxe(player, pickaxe);
        
        // Send message to player
        player.sendMessage("§5Your pickaxe shattered from the power of the Monster Miner!");
    }
    
    /**
     * Checks if the pickaxe has Monster Miner rune
     */
    private boolean hasMonsterMinerRune(ItemStack pickaxe) {
        if (pickaxe == null) return false;
        
        // Check if it's a pickaxe
        if (!pickaxe.getType().name().contains("PICKAXE")) return false;
        
        // Check for Monster Miner rune
        return RuneUtils.hasRune(pickaxe, RuneRegistry.getRune("monster_miner"));
    }
    
    /**
     * Checks if the mob type is allowed for Monster Miner
     */
    private boolean isAllowedMobType(EntityType mobType) {
        for (EntityType allowed : ALLOWED_MOBS) {
            if (allowed == mobType) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Creates a spawner item with correct mob type and colored name
     */
    private ItemStack createSpawnerItem(EntityType mobType) {
        ItemStack spawner = new ItemStack(Material.SPAWNER);
        ItemMeta meta = spawner.getItemMeta();
        
        if (meta != null) {
            // Set the mob type in NBT data
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(spawnerTypeKey, PersistentDataType.STRING, mobType.name());
            
            // Set colored display name (no italics)
            String mobName = mobType.name().charAt(0) + mobType.name().substring(1).toLowerCase();
            Component displayName = Component.text(mobName + " Spawner", NamedTextColor.DARK_PURPLE)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
            meta.displayName(displayName);
            
            spawner.setItemMeta(meta);
        }
        
        return spawner;
    }
    
    /**
     * Completely removes the pickaxe from player's inventory
     */
    private void completelyRemovePickaxe(Player player, ItemStack pickaxe) {
        // Remove one pickaxe from the player's main hand
        if (pickaxe.getAmount() > 1) {
            pickaxe.setAmount(pickaxe.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }
}

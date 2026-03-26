package BHML.aurum.runes.earth;

import BHML.aurum.runes.core.RuneUtils;
import BHML.aurum.runes.core.RuneRegistry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the Trench rune functionality:
 * - Breaks all blocks within a 3x3 radius around the mined block
 * - Works on all mineable blocks (dirt, gravel, stone, ores, etc.)
 * - Only works with pickaxes that have Trench rune
 * - Respects silk touch enchantment behavior
 */
public class TrenchListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        
        // Check if player is holding a pickaxe with Trench rune
        if (!hasTrenchRune(pickaxe)) return;
        
        Block centerBlock = event.getBlock();
        
        // Don't apply trench effect to certain blocks to avoid issues
        if (isBlacklistedBlock(centerBlock.getType())) return;
        
        // Get the blocks in 3x3 radius (1 block radius in all directions)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    // Skip the center block (already being broken by the original event)
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Block targetBlock = centerBlock.getRelative(x, y, z);
                    
                    // Skip if block is blacklisted
                    if (isBlacklistedBlock(targetBlock.getType())) continue;
                    
                    // Check if player can break this block (same permissions as original)
                    if (!canBreakBlock(player, targetBlock)) continue;
                    
                    // Break the block using the same method as the original
                    breakBlock(pickaxe, targetBlock, player);
                }
            }
        }
    }
    
    /**
     * Checks if the pickaxe has Trench rune
     */
    private boolean hasTrenchRune(ItemStack pickaxe) {
        if (pickaxe == null) return false;
        
        // Check if it's a pickaxe
        if (!pickaxe.getType().name().contains("PICKAXE")) return false;
        
        // Check for Trench rune
        return RuneUtils.hasRune(pickaxe, RuneRegistry.getRune("trench"));
    }
    
    /**
     * Checks if a block type should be excluded from trench effect
     */
    private boolean isBlacklistedBlock(Material material) {
        // Prevent breaking bedrock, barriers, spawners, obsidian, etc.
        return material == Material.BEDROCK ||
               material == Material.BARRIER ||
               material == Material.SPAWNER ||
               material == Material.END_PORTAL_FRAME ||
               material == Material.END_PORTAL ||
               material == Material.NETHER_PORTAL ||
               material == Material.OBSIDIAN ||
               material == Material.AIR;
    }
    
    /**
     * Checks if player can break a block (simplified permission check)
     */
    private boolean canBreakBlock(Player player, Block block) {
        // For now, just check if the block is not air
        // More complex permission checks can be added if needed
        return block.getType() != Material.AIR;
    }
    
    /**
     * Breaks a block using the appropriate method
     */
    private void breakBlock(ItemStack pickaxe, Block block, Player player) {
        // Check if pickaxe has silk touch
        boolean hasSilkTouch = pickaxe.containsEnchantment(Enchantment.SILK_TOUCH);
        
        if (hasSilkTouch) {
            // Drop the block itself with silk touch
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
        } else {
            // Drop the normal drops
            block.breakNaturally(pickaxe);
        }
        
        // Set the block to air
        block.setType(Material.AIR);
    }
}

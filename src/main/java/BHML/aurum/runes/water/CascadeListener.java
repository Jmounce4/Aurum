package BHML.aurum.runes.water;

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
 * Handles the Cascade rune functionality:
 * - Also mines the block directly below the mined block
 * - Perfect for digging down or strip mining
 * - Only works with pickaxes that have Cascade rune
 * - Respects silk touch enchantment behavior
 */
public class CascadeListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        
        // Check if player is holding a pickaxe with Cascade rune
        if (!hasCascadeRune(pickaxe)) return;
        
        Block centerBlock = event.getBlock();
        
        // Don't apply cascade effect to certain blocks to avoid issues
        if (isBlacklistedBlock(centerBlock.getType())) return;
        
        // Get the block directly below the mined block
        Block blockBelow = centerBlock.getRelative(0, -1, 0);
        
        // Skip if block below is blacklisted or air
        if (isBlacklistedBlock(blockBelow.getType())) return;
        if (blockBelow.getType() == Material.AIR) return;
        
        // Check if player can break this block
        if (!canBreakBlock(player, blockBelow)) return;
        
        // Break the block below using the same method as the original
        breakBlock(pickaxe, blockBelow, player);
    }
    
    /**
     * Checks if the pickaxe has Cascade rune
     */
    private boolean hasCascadeRune(ItemStack pickaxe) {
        if (pickaxe == null) return false;
        
        // Check if it's a pickaxe
        if (!pickaxe.getType().name().contains("PICKAXE")) return false;
        
        // Check for Cascade rune
        return RuneUtils.hasRune(pickaxe, RuneRegistry.getRune("cascade"));
    }
    
    /**
     * Checks if a block type should be excluded from cascade effect
     */
    private boolean isBlacklistedBlock(Material material) {
        // Prevent breaking bedrock, barriers, spawners, obsidian, etc.
        return material == Material.BEDROCK ||
               material == Material.BARRIER ||
               material == Material.SPAWNER ||
               material == Material.END_PORTAL_FRAME ||
               material == Material.END_PORTAL ||
               material == Material.NETHER_PORTAL ||
               material == Material.OBSIDIAN;
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

package BHML.aurum.runes.fire;

import BHML.aurum.runes.core.RuneUtils;
import BHML.aurum.runes.core.RuneRegistry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the Smelt rune functionality:
 * - Automatically smelts ores that need smelting (iron, gold, copper, etc.)
 * - Grants +1 XP for ALL ores mined (including diamonds, coal, lapis, etc.)
 * - Only works with pickaxes that have Smelt rune
 */
public class SmeltListener implements Listener {
    
    // Mapping of ores to their smelted versions
    private static final Map<Material, Material> SMELTING_MAP = new HashMap<>();
    
    static {
        // Iron and Gold
        SMELTING_MAP.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELTING_MAP.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELTING_MAP.put(Material.RAW_IRON, Material.IRON_INGOT);
        SMELTING_MAP.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELTING_MAP.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELTING_MAP.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        SMELTING_MAP.put(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT);
        
        // Copper
        SMELTING_MAP.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELTING_MAP.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        SMELTING_MAP.put(Material.RAW_COPPER, Material.COPPER_INGOT);
        
        // Ancient Debris -> Netherite Scrap
        SMELTING_MAP.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
        
        // Sand -> Glass
        SMELTING_MAP.put(Material.SAND, Material.GLASS);
        SMELTING_MAP.put(Material.RED_SAND, Material.GLASS);
        
        // Cobblestone -> Stone
        SMELTING_MAP.put(Material.COBBLESTONE, Material.STONE);
        
        // Clay -> Brick
        SMELTING_MAP.put(Material.CLAY, Material.BRICK);
        
        // Netherrack -> Nether Brick
        SMELTING_MAP.put(Material.NETHERRACK, Material.NETHER_BRICK);
    }
    
    // List of all ores that should grant XP
    private static final Material[] ORES = {
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD, Material.NETHER_GOLD_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER,
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
        Material.NETHER_QUARTZ_ORE,
        Material.ANCIENT_DEBRIS
    };
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        
        // Check if player is holding a pickaxe with Smelt rune
        if (!hasSmeltRune(pickaxe)) return;
        
        Block block = event.getBlock();
        Material blockType = block.getType();
        
        // Check if this is an ore that should grant XP
        if (isOre(blockType)) {
            // Drop +1 XP orb at the block location
            block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), org.bukkit.entity.ExperienceOrb.class)
                .setExperience(1);
            
            // Check if this ore can be smelted
            Material smeltedResult = SMELTING_MAP.get(blockType);
            if (smeltedResult != null) {
                // Cancel the original drop
                event.setDropItems(false);
                
                // Drop the smelted item instead
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smeltedResult));
            }
            // Ores are handled - don't process further
            return;
        }
        
        // For non-ores, let vanilla behavior handle everything normally
        // Silk touch will work on non-ore blocks as expected
    }
    
    /**
     * Checks if the pickaxe has Smelt rune
     */
    private boolean hasSmeltRune(ItemStack pickaxe) {
        if (pickaxe == null) return false;
        
        // Check if it's a pickaxe
        if (!pickaxe.getType().name().contains("PICKAXE")) return false;
        
        // Check for Smelt rune
        return RuneUtils.hasRune(pickaxe, RuneRegistry.getRune("smelt"));
    }
    
    /**
     * Checks if a material is an ore that should grant XP
     */
    private boolean isOre(Material material) {
        for (Material ore : ORES) {
            if (ore == material) {
                return true;
            }
        }
        return false;
    }
}

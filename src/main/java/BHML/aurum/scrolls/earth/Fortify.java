package BHML.aurum.scrolls.earth;

import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import static BHML.aurum.scrolls.earth.TerraPath.*;

public class Fortify implements Scroll {


    @Override
    public Element getElement() {
        return Element.EARTH;
    }

    @Override
    public String getId() {
        return "fortify";
    }

    @Override
    public String getName() {
        return "Fortify";
    }

    @Override
    public int getMaxUses() {
        return 20;
    }

    @Override
    public String getDescription(){
        return "Manipulate the earth to create a safe structure for safety";
    }

    @Override
    public int getGoldCost() { return 1; }
    @Override
    public int getRestoreAmount() { return 5; }
    @Override
    public int getCooldown() { return 1000; }  //0.10 seconds?

    private static final int HEIGHT = 4; // 4-block-high walls
    private static final int RADIUS = 1; // creates 3x3 exterior with center hollow

    private static final double KNOCKBACK_STRENGTH = 1.2;
    private static final double KNOCKBACK_RADIUS = 3.5;

    private static final int ABSORPTION_TIME = 20 * 4; // 4 seconds
    private static final int ABSORPTION_LEVEL = 1; // 4 extra hearts

    @Override
    public void cast(Player player) {
        World world = player.getWorld();

        // 1) SMALL AOE KNOCKBACK
        knockbackNearby(player);

        // 2) GIVE ABSORPTION HEARTS
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, ABSORPTION_TIME, ABSORPTION_LEVEL - 1));

        // 3) BUILD THE EARTH FORT
        buildFort(player);

        world.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1f, 1f);
    }

    private void knockbackNearby(Player player) {
        Location origin = player.getLocation();

        for (Entity entity : player.getNearbyEntities(KNOCKBACK_RADIUS, KNOCKBACK_RADIUS, KNOCKBACK_RADIUS)) {
            if (entity instanceof LivingEntity && entity != player) {
                Vector push = entity.getLocation().toVector()
                        .subtract(origin.toVector())
                        .normalize()
                        .multiply(KNOCKBACK_STRENGTH);
                entity.setVelocity(push);
            }
        }
    }

    private void buildFort(Player player) {
        Location base = player.getLocation().getBlock().getLocation();
        World world = player.getWorld();

        Material material = findMaterialFromHotbar(player);

        for (int y = 0-1; y < HEIGHT-1; y++) {
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {



                    Location placeLoc = base.clone().add(x, y, z);
                    Block block = placeLoc.getBlock();

                    if (x == 0 && z == 0 && y == 0){
                        placeTorch(placeLoc);
                        player.teleport(placeLoc);
                    }
                    // Skip the center column (player space)
                    if (x == 0 && z == 0 && y != -1 && y != HEIGHT-2) continue;

                    if (canReplace(block)) {
                        block.setType(material);
                        consumeMaterialIfNeeded(player, material);
                    }
                }
            }
        }
    }

    private void placeTorch(Location loc) {
        Block target = loc.getBlock();
        Block below = target.getRelative(BlockFace.DOWN);

        // Must place torch on a solid surface
        if (!below.getType().isSolid()) return;

        // Torch must replace a non-solid block
        if (!REPLACEABLE.contains(target.getType())) return;

        target.setType(Material.TORCH);
    }

    // ----------------------------
    // HOTBAR EARTH MATERIAL LOGIC
    // ----------------------------

    private boolean canReplace(Block block) {
        return REPLACEABLE.contains(block.getType());
    }

    private Material findMaterialFromHotbar(Player player) {
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && EARTH_MATERIALS.contains(item.getType())) {
                return item.getType();
            }
        }

        return DEFAULT_MATERIAL;
    }

    private void consumeMaterialIfNeeded(Player player, Material mat) {
        if (mat == DEFAULT_MATERIAL) return;

        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == mat) {
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) inv.setItem(i, null);
                return;
            }
        }
    }


}

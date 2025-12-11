package BHML.aurum.runes.core;

import BHML.aurum.Aurum;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class RuneAnvilListener implements Listener {

    private final Aurum plugin;

    public RuneAnvilListener(Aurum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {

        ItemStack left = event.getInventory().getItem(0);
        ItemStack right = event.getInventory().getItem(1);

        if (left == null || right == null) return;

        // Check if right item is a rune
        Rune rune = getRuneFromItem(right);
        if (rune == null) return;

        // Validate item type
        if (!RuneUtils.canAddRune(left, rune)) return;

        // Validate rune count
        if (RuneUtils.getRuneIds(left).size() >= RuneUtils.getMaxRunes(left)) return;

        // Create result item
        ItemStack result = left.clone();
        RuneUtils.applyRune(result, rune);

        event.setResult(result);

        // Force cost to 10 levels
        event.getInventory().setRepairCost(10);
    }

    private Rune getRuneFromItem(ItemStack item) {
        if (!item.hasItemMeta()) return null;
        if (!item.getItemMeta().hasDisplayName()) return null;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();

        for (Rune r : RuneRegistry.getAllRunes()) {
            if (name.contains(r.getName().toLowerCase())) {
                return r;
            }
        }
        return null;
    }


}

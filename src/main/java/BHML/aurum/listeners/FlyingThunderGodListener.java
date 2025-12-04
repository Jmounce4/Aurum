package BHML.aurum.listeners;

import BHML.aurum.Aurum;
import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.scrolls.core.ScrollUtils;
import BHML.aurum.scrolls.lightning.FlyingThunderGod;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import static BHML.aurum.utils.Keys.SCROLL_ID;

public class FlyingThunderGodListener implements Listener {

    private final Plugin plugin;
    private final FlyingThunderGod scroll;

    public FlyingThunderGodListener(Plugin plugin, FlyingThunderGod scroll) {
        this.plugin = plugin;
        this.scroll = scroll;
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        // Check if this IS the Flying Thunder God scroll
        String id = item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(plugin, "scroll_id"),
                PersistentDataType.STRING
        );

        if (id == null || !id.equals(scroll.getId())) return;

        // Valid scroll → mark location
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Location loc = clicked.getLocation().add(0.5, 1, 0.5);

        // Use plugin here for metadata
        scroll.setMark(player, loc, plugin);
    }
}
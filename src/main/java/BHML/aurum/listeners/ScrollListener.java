package BHML.aurum.listeners;
import BHML.aurum.Aurum;
import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.scrolls.core.ScrollRegistry;
import BHML.aurum.scrolls.core.ScrollUtils;
import BHML.aurum.scrolls.fire.Fireball;
import BHML.aurum.utils.Keys;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;

public class ScrollListener implements Listener{
    private final Aurum plugin;

    public ScrollListener(Aurum plugin) {
        this.plugin = plugin;
    }

    Scroll fireball = new Fireball();

    @EventHandler
    public void onScrollUse(PlayerInteractEvent e) {

        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;

        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Cancel banner behavior
        if (item.getType() == Material.FLOWER_BANNER_PATTERN &&
                meta.getPersistentDataContainer().has(Keys.SCROLL_ID, PersistentDataType.STRING)) {
            e.setCancelled(true);
        }

        String id = ScrollUtils.getScrollId(item);
        if (id == null) return;

        Scroll scroll = ScrollRegistry.get(id);
        if (scroll == null) return; // Scroll ID exists in item but not in registry? Bad data.

        castSelectedScroll(p, item, scroll);
    }

    private void castSelectedScroll(Player p, ItemStack item, Scroll scroll) {

        ItemMeta meta = item.getItemMeta();
        long now = System.currentTimeMillis();

        long cd = meta.getPersistentDataContainer()
                .getOrDefault(Keys.SCROLL_COOLDOWN, PersistentDataType.LONG, 0L);

        if (now < cd) {
            long left = (cd - now) / scroll.getCooldown();
            p.sendMessage("Scroll cooling down (" + left + "s)");
            return;
        }

        int uses = ScrollUtils.getUses(item);
        if (uses <= 0) {
            p.sendMessage("This scroll is depleted!");
            return;
        }

        // cast the actual spell
        scroll.cast(p);

        // reduce uses
        ScrollUtils.setUses(item, uses - 1);

        // apply cooldown
        meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Keys.SCROLL_COOLDOWN,
                PersistentDataType.LONG, now + scroll.getCooldown());
        item.setItemMeta(meta);
    }


}

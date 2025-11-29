package BHML.aurum.scrolls.Lectern;

import BHML.aurum.Aurum;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LecternListener implements Listener {

    private final Aurum plugin;

    public LecternListener(Aurum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLecternInteract(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block.getType() != Material.LECTERN) return;

        Player player = event.getPlayer();
        BlockState state = block.getState();



        // This is correct — block state is org.bukkit.block.Lectern
        if (!(state instanceof org.bukkit.block.Lectern lectern)) {
            return;
        }

        ItemStack book = lectern.getInventory().getItem(0);

        // FIXED: A lectern accepts BOOK or WRITTEN_BOOK
        boolean hasBook =
                book != null &&
                        (book.getType() == Material.WRITABLE_BOOK ||
                                book.getType() == Material.WRITTEN_BOOK);

        boolean isSneaking = player.isSneaking();


        // What the player is holding
        Material held = player.getInventory().getItemInMainHand().getType();

        // Allow placing a book normally:
        if (!hasBook && (held == Material.WRITABLE_BOOK || held == Material.WRITTEN_BOOK)) {
            return; // allow normal behavior, don't cancel event
        }

        // Your rules:
        // - If no book: always open GUI
        // - If has book: only open GUI when sneaking
        if ((!hasBook || (hasBook && isSneaking))) {
            event.setCancelled(true);
            player.openInventory(RefillGUI.createGUI());
        }
    }

}

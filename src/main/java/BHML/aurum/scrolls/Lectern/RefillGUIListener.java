package BHML.aurum.scrolls.Lectern;

import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.scrolls.core.ScrollUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RefillGUIListener implements Listener {

    private boolean isRefillGUI(InventoryView view) {
        return view.title().equals(Component.text(RefillGUI.TITLE));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!isRefillGUI(event.getView())) return;

        Player player = (Player) event.getWhoClicked();
        Inventory gui = event.getInventory();
        int slot = event.getRawSlot();
        ClickType click = event.getClick();

        // -----------------------------
        // 1. Handle shift-click from player inventory
        // -----------------------------
        if (slot >= 9) { // clicked in PLAYER inventory
            if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {

                ItemStack moved = event.getCurrentItem();
                if (moved == null) return;

                Material type = moved.getType();

                // Scrolls ONLY go into slot 2
                if (ScrollUtils.getScrollForItem(moved) != null) {
                    shiftMoveIntoSlot(gui, 1, moved);
                }

                // Gold ONLY goes into slot 4
                else if (type == Material.GOLD_INGOT) {
                    shiftMoveIntoSlot(gui, 3, moved);
                }

                event.setCancelled(true);

                // Update output slot after shift-move
                Bukkit.getScheduler().runTaskLater(
                        Bukkit.getPluginManager().getPlugin("Aurum"),
                        () -> updateRefillOutput(gui),
                        1
                );
                return;
            }
        }

        // --------------------------------
        // 2. Click inside GUI (top inventory)
        // --------------------------------
        if (slot < 9) {

            // Only allow slots 2, 4, 8
            if (slot != 1 && slot != 3 && slot != 7) {
                event.setCancelled(true);
                return;
            }

            // Output slot special logic
            if (slot == 7) {
                event.setCancelled(true);
                handleTakeOutput(gui, player);
                return;
            }

            // Allow clicks normally in 2 & 4, but update after
            Bukkit.getScheduler().runTaskLater(
                    Bukkit.getPluginManager().getPlugin("Aurum"),
                    () -> updateRefillOutput(gui),
                    1
            );
        }
    }

    private void shiftMoveIntoSlot(Inventory gui, int targetSlot, ItemStack moved) {
        ItemStack slotItem = gui.getItem(targetSlot);

        if (slotItem == null) {
            gui.setItem(targetSlot, moved.clone());
            moved.setAmount(0); // remove from player
        } else if (slotItem.isSimilar(moved)) {
            int space = slotItem.getMaxStackSize() - slotItem.getAmount();
            int move = Math.min(space, moved.getAmount());

            slotItem.setAmount(slotItem.getAmount() + move);
            moved.setAmount(moved.getAmount() - move);
        }
    }



    // -------------------------------------------------------
    // DRAG HANDLING
    // -------------------------------------------------------
    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!isRefillGUI(event.getView())) return;

        Inventory top = event.getView().getTopInventory();

        for (int slot : event.getRawSlots()) {
            if (slot < top.getSize()) {
                // Only allow dragging into 2 or 4 (never 8)
                if (slot != 1 && slot != 3) {
                    event.setCancelled(true);
                    return;
                }

                // Must follow item rules
                if (slot == 1 && ScrollUtils.getScrollForItem(event.getOldCursor()) == null) {
                    event.setCancelled(true);
                    return;
                }
                if (slot == 3 && event.getOldCursor().getType() != Material.GOLD_INGOT) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("Aurum"),
                () -> updateRefillOutput(event.getInventory()),
                1
        );
    }



    // -------------------------------------------------------
    // ON CLOSE — return scroll + gold
    // -------------------------------------------------------
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!isRefillGUI(event.getView())) return;

        Player p = (Player) event.getPlayer();
        Inventory inv = event.getInventory();

        for (int slot : new int[]{1, 3}) {
            ItemStack item = inv.getItem(slot);
            if (item != null) {
                p.getInventory().addItem(item);
            }
        }
    }



    // -------------------------
// UPDATE OUTPUT (preview)
// -------------------------
    private void updateRefillOutput(Inventory inv) {
        ItemStack scrollItem = inv.getItem(1);
        ItemStack goldItem   = inv.getItem(3);

        inv.setItem(7, null); // clear output by default

        if (scrollItem == null) return;

        Scroll scroll = ScrollUtils.getScrollForItem(scrollItem);
        if (scroll == null) return;

        if (goldItem == null || goldItem.getType() != Material.GOLD_INGOT) return;

        int gold = goldItem.getAmount();
        int cost = scroll.getGoldCost();
        int restore = scroll.getRestoreAmount();
        int maxUses = scroll.getMaxUses();

        int currentUses = ScrollUtils.getUses(scrollItem);
        int missing = maxUses - currentUses;
        if (missing <= 0) return;

        // Max refills possible by gold
        int refillsByGold = gold / cost;
        // Max refills required to fully restore
        int refillsByMissing = (int) Math.ceil((double) missing / restore);

        int refills = Math.min(refillsByGold, refillsByMissing);
        if (refills <= 0) return;

        int totalRestore = refills * restore;
        int newUses = Math.min(currentUses + totalRestore, maxUses);

        ItemStack result = scrollItem.clone();
        ScrollUtils.setUses(result, newUses);

        // Optional: add a small lore line showing cost preview
        ItemMeta rm = result.getItemMeta();
        if (rm != null) {
            List<Component> lore = rm.hasLore() ? new ArrayList<>(rm.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Preview: +" + totalRestore + " uses", NamedTextColor.GREEN));
            lore.add(Component.text("Will cost: " + (refills * cost) + " gold", NamedTextColor.YELLOW));
            rm.lore(lore);
            result.setItemMeta(rm);
        }

        inv.setItem(7, result);
    }


    // -------------------------
// HANDLE TAKE OUTPUT (transaction)
// -------------------------
    private void handleTakeOutput(Inventory inv, Player player) {
        ItemStack output = inv.getItem(7);
        if (output == null) return;

        ItemStack goldItem = inv.getItem(3);
        ItemStack scrollItem = inv.getItem(1);

        if (scrollItem == null || goldItem == null) return;

        Scroll scroll = ScrollUtils.getScrollForItem(scrollItem);
        if (scroll == null) return;

        int cost = scroll.getGoldCost();
        int restore = scroll.getRestoreAmount();
        int maxUses = scroll.getMaxUses();

        int gold = goldItem.getAmount();
        int currentUses = ScrollUtils.getUses(scrollItem);
        int missing = maxUses - currentUses;
        if (missing <= 0) return;

        // compute refills and gold required (same logic as preview)
        int refillsByGold = gold / cost;
        int refillsByMissing = (int) Math.ceil((double) missing / restore);
        int refills = Math.min(refillsByGold, refillsByMissing);
        if (refills <= 0) return;

        int totalRestore = refills * restore;
        int goldRequired = refills * cost;

        // Deduct gold
        int remainingGold = gold - goldRequired;
        if (remainingGold <= 0) {
            inv.setItem(3, null);
        } else {
            ItemStack newGold = goldItem.clone();
            newGold.setAmount(remainingGold);
            inv.setItem(3, newGold);
        }

        // Create the refilled scroll to give to player (clone of input with updated uses)
        ItemStack refilled = scrollItem.clone();
        int newUses = Math.min(currentUses + totalRestore, maxUses);
        ScrollUtils.setUses(refilled, newUses);

        // Give player the refilled item
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(refilled);
        // If player's inventory full, drop leftovers at their feet
        if (!leftovers.isEmpty()) {
            for (ItemStack s : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), s);
            }
        }

        // Remove input scroll from GUI (consumed)
        inv.setItem(1, null);

        // Clear output slot
        inv.setItem(7, null);

        // Play magical sound
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.2f, 1.8f);

        // Spawn lingering particle effect around the lectern position (3 seconds)
        spawnRefillParticles(player);

        // Recompute output (in case more gold remains and the player can refill again)
        updateRefillOutput(inv);
    }

    private void spawnRefillParticles(Player player) {

        Location center = player.getLocation().add(0, 1, 0);

        int durationTicks = 40;
        int interval = 5;

        final int[] taskId = new int[1]; // <-- FIX

        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Bukkit.getPluginManager().getPlugin("Aurum"),
                new Runnable() {
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks >= durationTicks) {
                            Bukkit.getScheduler().cancelTask(taskId[0]);
                            return;
                        }

                        player.getWorld().spawnParticle(
                                Particle.ENCHANT,
                                center,
                                50,
                                1.0, 0.8, 1.0,
                                0.35
                        );

                        player.getWorld().spawnParticle(
                                Particle.END_ROD,
                                center, 6,
                                0.6, 0.4, 0.6,
                                0.04
                        );

                        ticks += interval;
                    }
                },
                0,
                interval
        );


    }


}

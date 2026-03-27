package BHML.aurum.runes.normal;

import BHML.aurum.elements.Element;
import BHML.aurum.runes.core.RuneUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class InstinctListener implements Listener {

    private static final double DODGE_CHANCE = 0.07; // 7% chance
    private final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if the entity being damaged is a player
        if (!(event.getEntity() instanceof Player player)) return;

        // Check if the player has a sword with Instinct rune
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;

        if (!RuneUtils.hasRune(weapon, new Instinct())) return;

        // Roll for dodge chance
        if (random.nextDouble() < DODGE_CHANCE) {
            // Cancel the damage
            event.setCancelled(true);

            player.sendMessage(Component.text("*dodge*").color(TextColor.color(221, 195, 162)));
            // Send dodge message in normal element color
            player.sendMessage(ChatColor.WHITE + "*dodge*");
            
            // Play dodge sound effect
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.5f, 2.0f);
        }
    }
}

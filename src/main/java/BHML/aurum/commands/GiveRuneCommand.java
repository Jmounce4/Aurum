package BHML.aurum.commands;


import BHML.aurum.runes.core.Rune;
import BHML.aurum.runes.core.RuneRegistry;
import BHML.aurum.runes.core.RuneUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor; // if needed

import java.util.List;


public class GiveRuneCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public GiveRuneCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /giverune <runeId>");
            return true;
        }

        String id = args[0].toLowerCase();
        Rune rune = RuneRegistry.getRune(id);

        if (rune == null) {
            player.sendMessage(ChatColor.RED + "Rune not found: " + id);
            return true;
        }

        // --- Build rune item ---
        Material mat = Material.matchMaterial(rune.getDisplayItem());
        if (mat == null) mat = Material.GOLD_NUGGET;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Use Adventure Component: name colored by element
            Component displayName = rune.getElement().coloredName(rune.getName());
            meta.displayName(displayName);

            // Lore: show description in gray (Component)
            Component desc = Component.text(rune.getDescription()).color(NamedTextColor.GRAY);
            meta.lore(List.of(desc));

            // Store rune ID in PDC as before (so anvil can read it)
            RuneUtils.setRuneId(meta, rune.getId());

            item.setItemMeta(meta);
        }

        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Given rune: " + rune.getName());

        return true;
    }




}

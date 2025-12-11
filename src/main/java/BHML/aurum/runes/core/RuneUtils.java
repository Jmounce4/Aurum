package BHML.aurum.runes.core;

import BHML.aurum.Aurum;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class RuneUtils {

    private static final NamespacedKey RUNES_KEY =
            new NamespacedKey(JavaPlugin.getPlugin(Aurum.class), "runes");

    public static List<String> getRuneIds(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return new ArrayList<>();

        String stored = item.getItemMeta().getPersistentDataContainer()
                .get(RUNES_KEY, PersistentDataType.STRING);

        if (stored == null || stored.isEmpty()) return new ArrayList<>();

        return new ArrayList<>(Arrays.asList(stored.split(";")));
    }

    public static boolean hasRune(ItemStack item, Rune rune) {
        return getRuneIds(item).contains(rune.getId());
    }

    public static boolean canAddRune(ItemStack item, Rune rune) {
        if (item == null) return false;

        String type = item.getType().name();

        // Validate item type matches rune.getItem()
        return switch (rune.getItem().toLowerCase()) {
            case "bow"       -> type.contains("BOW");
            case "sword"     -> type.contains("SWORD");
            case "pickaxe"   -> type.contains("PICKAXE");
            case "chestplate"-> type.contains("CHESTPLATE");
            default -> false;
        };
    }

    public static int getMaxRunes(ItemStack item) {
        return item.getType().name().contains("CHESTPLATE") ? 1 : 2;
    }

    public static void applyRune(ItemStack item, Rune rune) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> ids = getRuneIds(item);
        if (!ids.contains(rune.getId())) ids.add(rune.getId());

        // Cap count
        while (ids.size() > getMaxRunes(item)) {
            ids.remove(0);
        }

        // Save persistent data
        meta.getPersistentDataContainer().set(
                RUNES_KEY,
                PersistentDataType.STRING,
                String.join(";", ids)
        );

        // Add lore below enchants
        updateRuneLore(meta, ids);

        item.setItemMeta(meta);
    }

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private static void updateRuneLore(ItemMeta meta, List<String> runeIds) {
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        if (lore == null) lore = new ArrayList<>();

        // Remove any existing rune lines by checking plain-text equality to any known rune name
        Set<String> runeNamesLower = new HashSet<>();
        for (String id : runeIds) {
            Rune r = RuneRegistry.getRune(id);
            if (r != null) runeNamesLower.add(r.getName().toLowerCase());
        }

        // Build new lore that excludes old rune lines (by plain-text comparison)
        List<Component> newLore = new ArrayList<>();
        for (Component line : lore) {
            String plain = PLAIN.serialize(line).trim().toLowerCase();
            if (runeNamesLower.contains(plain)) {
                // skip old rune line
                continue;
            }
            newLore.add(line);
        }

        // Append rune lines (colored name only)
        for (String id : runeIds) {
            Rune r = RuneRegistry.getRune(id);
            if (r == null) continue;
            Component runeComp = r.getElement().coloredName(r.getName()); // uses Element.coloredName
            newLore.add(runeComp);
        }

        meta.lore(newLore);
    }

    public static void setRuneId(ItemMeta meta, String id) {
        meta.getPersistentDataContainer().set(
                new NamespacedKey(JavaPlugin.getPlugin(Aurum.class), "rune_id"),
                org.bukkit.persistence.PersistentDataType.STRING,
                id
        );
    }

    public static String getRuneId(ItemMeta meta) {
        return meta.getPersistentDataContainer().get(
                new NamespacedKey(JavaPlugin.getPlugin(Aurum.class), "rune_id"),
                org.bukkit.persistence.PersistentDataType.STRING
        );
    }

    public static boolean hasRune(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return getRuneId(item.getItemMeta()) != null;
    }


}

package BHML.aurum.listeners;

import BHML.aurum.scrolls.core.ScrollUtils;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class WorldGenListener implements Listener {
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public WorldGenListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static final Map<String, Double> CHEST_CHANCES = Map.ofEntries(
            Map.entry("VILLAGE", 1.00),
            Map.entry("ABANDONED_MINESHAFT", 0.08),
            Map.entry("JUNGLE_TEMPLE", 0.8),
            Map.entry("DESERT_PYRAMID", 0.2),
            Map.entry("BURIED_TREASURE", 0.8),
            Map.entry("SHIPWRECK_TREASURE", 0.08),
            Map.entry("SHIPWRECK_SUPPLY", 0.04),
            Map.entry("STRONGHOLD_CORRIDOR", 0.15),
            Map.entry("END_CITY_TREASURE", 0.25),
            Map.entry("NETHER_FORTRESS", 0.12),
            Map.entry("IGLOO", 0.03),
            Map.entry("PILLAGER_OUTPOST", 0.15),
            Map.entry("RUINED_PORTAL", 0.05)
    );

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {

        // EXACTLY like your emerald code
        InventoryHolder holder = event.getInventoryHolder();
        if (!(holder instanceof Chest)) return;

        Inventory inv = holder.getInventory();
        LootTable table = event.getLootTable();
        if (table == null || table.getKey() == null) return;

        String typeKey = getChestTypeKey(table.getKey().getKey());
        if (typeKey == null) return;

        double chance = CHEST_CHANCES.getOrDefault(typeKey, 0.0);
        if (random.nextDouble() > chance) return;

        Bukkit.getLogger().info("PUTTING SCROLL IN CHEST (" + typeKey + ")");

        // pick random empty slot
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                emptySlots.add(i);
            }
        }
        if (emptySlots.isEmpty()) return;

        int slot = emptySlots.get(random.nextInt(emptySlots.size()));

        ItemStack scroll = ScrollUtils.getRandomScroll();
        inv.setItem(slot, scroll);
    }

    private String getChestTypeKey(String rawKey) {
        rawKey = rawKey.toUpperCase();

        if (rawKey.contains("VILLAGE")) return "VILLAGE";
        if (rawKey.contains("ABANDONED_MINESHAFT")) return "ABANDONED_MINESHAFT";
        if (rawKey.contains("JUNGLE_TEMPLE")) return "JUNGLE_TEMPLE";
        if (rawKey.contains("DESERT_PYRAMID")) return "DESERT_PYRAMID";
        if (rawKey.contains("BURIED_TREASURE")) return "BURIED_TREASURE";
        if (rawKey.contains("SHIPWRECK_TREASURE")) return "SHIPWRECK_TREASURE";
        if (rawKey.contains("SHIPWRECK_SUPPLY")) return "SHIPWRECK_SUPPLY";
        if (rawKey.contains("STRONGHOLD")) return "STRONGHOLD_CORRIDOR";
        if (rawKey.contains("END_CITY")) return "END_CITY_TREASURE";
        if (rawKey.contains("NETHER_FORTRESS")) return "NETHER_FORTRESS";
        if (rawKey.contains("IGLOO")) return "IGLOO";
        if (rawKey.contains("PILLAGER_OUTPOST")) return "PILLAGER_OUTPOST";
        if (rawKey.contains("RUINED_PORTAL")) return "RUINED_PORTAL";

        return null;
    }
}

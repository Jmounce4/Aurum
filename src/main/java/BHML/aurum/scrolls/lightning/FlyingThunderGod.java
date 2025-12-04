package BHML.aurum.scrolls.lightning;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;

import BHML.aurum.utils.Keys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class FlyingThunderGod implements Scroll {

    private final NamespacedKey keyX = new NamespacedKey("aurum", "ftg_x");
    private final NamespacedKey keyY = new NamespacedKey("aurum", "ftg_y");
    private final NamespacedKey keyZ = new NamespacedKey("aurum", "ftg_z");
    private final NamespacedKey keyWorld = new NamespacedKey("aurum", "ftg_world");

    /* I am not doing this!!!
    private Plugin getPlugin() {
        return Aurum.getInstance();   // or YourMainClass.getInstance()
    }*/

    @Override
    public Element getElement() { return Element.LIGHTNING; }

    @Override
    public String getId() { return "flyingthundergod"; }

    @Override
    public String getName() { return "Flying Thunder God"; }

    @Override
    public int getMaxUses() { return 1; }

    @Override
    public String getDescription() {

                return "Summon yourself to the target location   " +  "Left-Click to mark location";

        }

    public void updateLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore == null) lore = new ArrayList<>();

        String markLine = getMarkString(item);

        boolean replaced = false;

        // search for an existing mark line
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);

            // detect old mark lines
            if (line.startsWith("   ") || line.startsWith("Left-Click")) {
                lore.set(i, markLine);
                replaced = true;
                break;
            }
        }

        // if no existing mark line, add a new one at the *end*
        if (!replaced) {
            lore.set(1, markLine);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }


    @Override
    public int getGoldCost() { return 10; }

    @Override
    public int getRestoreAmount() { return 1; }

    @Override
    public int getCooldown() { return 1000; }



    public void setMark(Player player, ItemStack item, Location loc) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        data.set(keyX, PersistentDataType.DOUBLE, loc.getX());
        data.set(keyY, PersistentDataType.DOUBLE, loc.getY());
        data.set(keyZ, PersistentDataType.DOUBLE, loc.getZ());
        data.set(keyWorld, PersistentDataType.STRING, loc.getWorld().getName());

        item.setItemMeta(meta);
        updateLore(item);
    }

    public String getMarkString(ItemStack item) {

        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        if (!data.has(keyX, PersistentDataType.DOUBLE)) {
            return null; // no mark stored
        }

        double x = data.get(keyX, PersistentDataType.DOUBLE);
        double z = data.get(keyZ, PersistentDataType.DOUBLE);

        return "Mark: " + (int) x + ", " + (int) z;
    }

    public boolean hasValidMark(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();

        return data.has(keyX, PersistentDataType.DOUBLE); // only need X to know it's set
    }

    @Override
    public void cast(Player player) {

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        if (!data.has(keyX, PersistentDataType.DOUBLE)) {
            player.sendMessage(ChatColor.RED + "This scroll has no mark!");
            return;
        }

        double x = data.get(keyX, PersistentDataType.DOUBLE);
        double y = data.get(keyY, PersistentDataType.DOUBLE);
        double z = data.get(keyZ, PersistentDataType.DOUBLE);
        String worldName = data.get(keyWorld, PersistentDataType.STRING);

        World w = Bukkit.getWorld(worldName);
        if (w == null) {
            player.sendMessage(ChatColor.RED + "Marked world no longer exists.");
            return;
        }

        Location mark = new Location(w, x, y, z);

        // Visual + sound effects
        w.playSound(mark, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3, 1);
        w.spawnParticle(Particle.ELECTRIC_SPARK, mark, 60, 0.5, 1, 0.5, 1.2);
        w.spawnParticle(Particle.ELECTRIC_SPARK, mark, 60, 0.5, 1, 0.5, 3);

        player.teleport(mark);
        w.spawnParticle(Particle.EXPLOSION, mark, 1);
    }
}

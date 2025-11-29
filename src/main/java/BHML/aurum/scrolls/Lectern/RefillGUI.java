package BHML.aurum.scrolls.Lectern;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RefillGUI {

    public static final String TITLE = "Scroll Refill";

    public static Inventory createGUI() {

        Inventory inv = Bukkit.createInventory(null, 9, Component.text(TITLE));

        // Decorative filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack filler2 = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.text(" "));
        filler.setItemMeta(meta);

        for (int i = 0; i < 9; i++) inv.setItem(i, filler);

        // usable slots
        inv.setItem(1, null); // scroll input
        inv.setItem(3, null); // gold input
        inv.setItem(7, null); // output
        inv.setItem(6, filler2);
        inv.setItem(8, filler2);

        return inv;
    }
}

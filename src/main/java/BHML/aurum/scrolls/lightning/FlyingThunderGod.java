package BHML.aurum.scrolls.lightning;

import BHML.aurum.Aurum;
import BHML.aurum.elements.Element;
import BHML.aurum.scrolls.core.Scroll;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class FlyingThunderGod implements Scroll {

    private static final String MARK_KEY = "ftg_mark";

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
        return "Summon yourself to the target location - Left-Click to mark";
    }

    @Override
    public int getGoldCost() { return 10; }

    @Override
    public int getRestoreAmount() { return 1; }

    @Override
    public int getCooldown() { return 1000; }

    public void onLeftClickBlock(Player player, PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Location loc = clicked.getLocation().add(0.5, 1, 0.5);


        /*We need to change this.
        player.setMetadata(MARK_KEY, new FixedMetadataValue(getPlugin(), loc));

        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2f);
    */
    }


    public void setMark(Player player, Location loc, Plugin plugin) {
        player.setMetadata(MARK_KEY, new FixedMetadataValue(plugin, loc));
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2f);
    }


    @Override
    public void cast(Player player) {

        if (!player.hasMetadata(MARK_KEY)) {
            player.sendMessage(ChatColor.RED + "No marked location");
            return;
        }

        Location mark = (Location) player.getMetadata(MARK_KEY).get(0).value();

        World w = player.getWorld();
        w.playSound(mark, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3, 1);
        w.spawnParticle(Particle.ELECTRIC_SPARK, mark, 60, 0.5, 1, 0.5, 0.1);

        player.teleport(mark);

        w.spawnParticle(Particle.EXPLOSION, mark, 1);
    }
}

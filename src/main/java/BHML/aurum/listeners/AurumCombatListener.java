package BHML.aurum.listeners;

import BHML.aurum.Aurum;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AurumCombatListener implements Listener {

    private final Aurum plugin;

    public AurumCombatListener(Aurum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamagePet(EntityDamageByEntityEvent e) {


            Entity victim = e.getEntity();

            if (victim instanceof Tameable tame && tame.isTamed()) {
                // always block damage to tamed pets
                e.setCancelled(true);
            }
        }



        @EventHandler
        public void onQuit (PlayerQuitEvent event){
            plugin.resetPvP(event.getPlayer());
        }



    }



package BHML.aurum.listeners;

import BHML.aurum.Aurum;
import BHML.aurum.scrolls.core.ScrollUtils;
import BHML.aurum.utils.Keys;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


    public class TradeListener implements Listener {


        private final JavaPlugin plugin;

        // Constructor — pass your main plugin instance here
        public TradeListener(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        private final Random random = new Random();

        // CLERIC — Guaranteed scroll at LEVEL 3

        @EventHandler
        public void onClericLevelUp(VillagerAcquireTradeEvent event) {
            if (!(event.getEntity() instanceof Villager villager)) return;

            if (villager.getProfession() != Villager.Profession.CLERIC) return;
            if (villager.getVillagerLevel() != 3) return;

            // Only add ONCE per villager
            if (villager.getPersistentDataContainer().has(Keys.CLERIC_SCROLL_ADDED)) return;

            villager.getPersistentDataContainer().set(
                    Keys.CLERIC_SCROLL_ADDED,
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            // Add a trade on top of the existing ones (do NOT replace)
            Bukkit.getScheduler().runTask(plugin, () -> {
                List<MerchantRecipe> trades = new ArrayList<>(villager.getRecipes());

                MerchantRecipe recipe = new MerchantRecipe(
                        ScrollUtils.getRandomScroll(), 1
                );
                recipe.addIngredient(new ItemStack(Material.EMERALD, 64));

                trades.add(recipe);
                villager.setRecipes(trades);
            });
        }


        // WANDERING TRADER — 50% chance to add scroll
        @EventHandler
        public void onTraderSpawn(CreatureSpawnEvent event) {
            if (!(event.getEntity() instanceof WanderingTrader trader)) return;
            if (Math.random() > 0.5) return; // 50% chance

            Bukkit.getScheduler().runTask(plugin, () -> {
                List<MerchantRecipe> offers = new ArrayList<>(trader.getRecipes());

                MerchantRecipe recipe = new MerchantRecipe(
                        ScrollUtils.getRandomScroll(), 1
                );
                recipe.addIngredient(new ItemStack(Material.EMERALD, 64));

                offers.add(recipe);
                trader.setRecipes(offers);
            });
        }
    }





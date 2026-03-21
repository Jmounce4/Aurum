package BHML.aurum.listeners;

import BHML.aurum.Aurum;
import BHML.aurum.runes.core.RuneRegistry;
import BHML.aurum.runes.core.RuneUtils;
import BHML.aurum.scrolls.core.ScrollUtils;
import BHML.aurum.utils.Keys;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

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

        // CLERIC — luck potion at LEVEL 3, rune at LEVEL 4, scroll at LEVEL 5

        @EventHandler
        public void onClericLevelUp(VillagerAcquireTradeEvent event) {
            if (!(event.getEntity() instanceof Villager villager)) return;

            if (villager.getProfession() != Villager.Profession.CLERIC) return;

            // Handle different levels
            if (villager.getVillagerLevel() == 3) {
                addLuckPotionTrade(villager);
            } else if (villager.getVillagerLevel() == 4) {
                addRuneTrade(villager);
            } else if (villager.getVillagerLevel() == 5) {
                addScrollTrade(villager);
            }
        }

        private void addRuneTrade(Villager villager) {
            // Only add ONCE per villager
            if (villager.getPersistentDataContainer().has(Keys.CLERIC_RUNE_ADDED)) return;

            villager.getPersistentDataContainer().set(
                    Keys.CLERIC_RUNE_ADDED,
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            // Add a random rune trade
            Bukkit.getScheduler().runTask(plugin, () -> {
                List<MerchantRecipe> trades = new ArrayList<>(villager.getRecipes());

                if (!RuneRegistry.getAllRunes().isEmpty()) {
                    ItemStack randomRune = RuneUtils.createRuneItem(
                        RuneRegistry.getAllRunes().toArray(new BHML.aurum.runes.core.Rune[0])[random.nextInt(RuneRegistry.getAllRunes().size())]
                    );
                    
                    MerchantRecipe recipe = new MerchantRecipe(randomRune, 1);
                    recipe.addIngredient(new ItemStack(Material.EMERALD, 50));

                    trades.add(recipe);
                    villager.setRecipes(trades);
                }
            });
        }

        private void addScrollTrade(Villager villager) {
            // Only add ONCE per villager
            if (villager.getPersistentDataContainer().has(Keys.CLERIC_SCROLL_ADDED)) return;

            villager.getPersistentDataContainer().set(
                    Keys.CLERIC_SCROLL_ADDED,
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            // Add a scroll trade
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

        private void addLuckPotionTrade(Villager villager) {
            // Only add ONCE per villager
            if (villager.getPersistentDataContainer().has(Keys.CLERIC_LUCK_ADDED)) return;

            villager.getPersistentDataContainer().set(
                    Keys.CLERIC_LUCK_ADDED,
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            // Add a luck potion or dragon's breath trade (50% chance each)
            Bukkit.getScheduler().runTask(plugin, () -> {
                List<MerchantRecipe> trades = new ArrayList<>(villager.getRecipes());

                ItemStack tradeItem;
                String displayName;
                
                if (random.nextBoolean()) {
                    // Luck potion
                    tradeItem = new ItemStack(Material.POTION);
                    PotionMeta meta = (PotionMeta) tradeItem.getItemMeta();
                    
                    if (meta != null) {
                        meta.addCustomEffect(
                                new PotionEffect(PotionEffectType.LUCK, 6000, 0), // 3 minutes
                                true
                        );
                        meta.setDisplayName("§aPotion of Luck");
                        meta.setColor(Color.GREEN);
                        tradeItem.setItemMeta(meta);
                    }
                    displayName = "Luck Potion";
                } else {
                    // Dragon's Breath
                    tradeItem = new ItemStack(Material.DRAGON_BREATH);
                    displayName = "Dragon's Breath";
                }

                int cost = 28 + random.nextInt(7); // 28-34 emeralds
                MerchantRecipe recipe = new MerchantRecipe(tradeItem, 4);
                recipe.addIngredient(new ItemStack(Material.EMERALD, cost));

                trades.add(recipe);
                villager.setRecipes(trades);
            });
        }


        // WANDERING TRADER — 35% chance for rune, 20% chance for scroll
        @EventHandler
        public void onTraderSpawn(CreatureSpawnEvent event) {
            if (!(event.getEntity() instanceof WanderingTrader trader)) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                List<MerchantRecipe> offers = new ArrayList<>(trader.getRecipes());

                // Calculate chances once per trader
                boolean shouldAddRune = Math.random() < 0.35;
                boolean shouldAddScroll = Math.random() < 0.20;

                // Add rune trade (35% chance)
                if (shouldAddRune && !RuneRegistry.getAllRunes().isEmpty()) {
                    ItemStack randomRune = RuneUtils.createRuneItem(
                        RuneRegistry.getAllRunes().toArray(new BHML.aurum.runes.core.Rune[0])[random.nextInt(RuneRegistry.getAllRunes().size())]
                    );
                    
                    MerchantRecipe runeRecipe = new MerchantRecipe(randomRune, 1);
                    runeRecipe.addIngredient(new ItemStack(Material.EMERALD, 32));
                    offers.add(runeRecipe);
                }

                // Add scroll trade (20% chance)
                if (shouldAddScroll) {
                    MerchantRecipe scrollRecipe = new MerchantRecipe(
                            ScrollUtils.getRandomScroll(), 1
                    );
                    scrollRecipe.addIngredient(new ItemStack(Material.EMERALD, 64));
                    offers.add(scrollRecipe);
                }

                trader.setRecipes(offers);
            });
        }
    }





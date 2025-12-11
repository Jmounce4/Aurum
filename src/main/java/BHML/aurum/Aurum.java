package BHML.aurum;

import BHML.aurum.commands.AurumCommand;
import BHML.aurum.commands.GiveRuneCommand;
import BHML.aurum.commands.GiveScrollCommand;
import BHML.aurum.listeners.*;
import BHML.aurum.runes.core.RuneAnvilListener;
import BHML.aurum.runes.core.RuneRegistry;
import BHML.aurum.runes.normal.SniperRuneListener;
import BHML.aurum.scrolls.Lectern.LecternListener;
import BHML.aurum.scrolls.Lectern.RefillGUIListener;
import BHML.aurum.scrolls.core.ScrollParticleTask;
import BHML.aurum.scrolls.core.ScrollRegistry;
import BHML.aurum.scrolls.fire.Fireball;
import BHML.aurum.scrolls.lightning.FlyingThunderGod;
import BHML.aurum.utils.Keys;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static BHML.aurum.scrolls.core.ScrollRegistry.register;

public final class Aurum extends JavaPlugin {

    // Players who have PVP enabled for this session
    private final Set<UUID> pvpEnabled = new HashSet<>();

    private void registerCommands() {
        GiveScrollCommand giveScroll = new GiveScrollCommand(this);
        getCommand("givescroll").setExecutor(giveScroll);
        getCommand("givescroll").setTabCompleter(giveScroll);
        getCommand("aurum").setExecutor(new AurumCommand(this));
        getCommand("giverune").setExecutor(new GiveRuneCommand(this));
        getServer().getPluginManager().registerEvents(new AurumCombatListener(this), this);
    }


    @Override
    public void onEnable() {
        Keys.init(this);



        //Use Scrolls
        getServer().getPluginManager().registerEvents(new ScrollListener(this), this);
        //Commands
        registerCommands();


        //Register Scrolls
        ScrollRegistry.registerDefaults();

        //Register passive particle effects
        new ScrollParticleTask(this).runTaskTimer(this, 0L, 5L);

        //Right Clicking Lecterns
        getServer().getPluginManager().registerEvents(new LecternListener(this), this);

        //Scroll Refills GUI
        getServer().getPluginManager().registerEvents(new RefillGUIListener(), this);

        //chest generation
        getServer().getPluginManager().registerEvents(new WorldGenListener(this), this);

        //Villager trades
        getServer().getPluginManager().registerEvents(new TradeListener(this), this);

        // FTG
        FlyingThunderGod ftg = new FlyingThunderGod();
        register(ftg);

        //Dont break towers
        getServer().getPluginManager().registerEvents(new TowerListener(), this);

        getServer().getPluginManager().registerEvents(
                new FlyingThunderGodListener(this, ftg),
                this
        );


        //PvP and pet protection

        //testing bow mechanics
        //getServer().getPluginManager().registerEvents(new BowListener(), this);



        //RUNES

        //Register Runes
        RuneRegistry.registerDefaults();

        //Rune Listeners
        getServer().getPluginManager().registerEvents(new RuneAnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new SniperRuneListener(), this);


        getLogger().info("***Aurum loaded***");
    }

    @Override
    public void onDisable() {
        getLogger().info("Aurum disabled.");
    }


    public boolean hasPvPEnabled(UUID uuid) {
        return pvpEnabled.contains(uuid);
    }

    public void enablePvP(Player player) {
        pvpEnabled.add(player.getUniqueId());
    }

    public void disablePvP(Player player) {
        pvpEnabled.remove(player.getUniqueId());
    }

    public void resetPvP(Player player) {
        pvpEnabled.remove(player.getUniqueId());
    }


}

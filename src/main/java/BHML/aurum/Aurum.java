package BHML.aurum;

import BHML.aurum.commands.GiveScrollCommand;
import BHML.aurum.listeners.*;
import BHML.aurum.scrolls.Lectern.LecternListener;
import BHML.aurum.scrolls.Lectern.RefillGUIListener;
import BHML.aurum.scrolls.core.ScrollParticleTask;
import BHML.aurum.scrolls.core.ScrollRegistry;
import BHML.aurum.scrolls.fire.Fireball;
import BHML.aurum.scrolls.lightning.FlyingThunderGod;
import BHML.aurum.utils.Keys;
import org.bukkit.plugin.java.JavaPlugin;

import static BHML.aurum.scrolls.core.ScrollRegistry.register;

public final class Aurum extends JavaPlugin {

    private void registerCommands() {
        GiveScrollCommand giveScroll = new GiveScrollCommand(this);
        getCommand("givescroll").setExecutor(giveScroll);
        getCommand("givescroll").setTabCompleter(giveScroll);
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

        getServer().getPluginManager().registerEvents(
                new FlyingThunderGodListener(this, ftg),
                this
        );

        
        //testing bow mechanics
        getServer().getPluginManager().registerEvents(new BowListener(), this);

        getLogger().info("***Aurum loaded***");
    }

    @Override
    public void onDisable() {
        getLogger().info("Aurum disabled.");
    }
}

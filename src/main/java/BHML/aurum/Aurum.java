package BHML.aurum;

import BHML.aurum.commands.GiveScrollCommand;
import BHML.aurum.listeners.BowListener;
import BHML.aurum.listeners.ScrollListener;
import BHML.aurum.listeners.TradeListener;
import BHML.aurum.listeners.WorldGenListener;
import BHML.aurum.scrolls.Lectern.LecternListener;
import BHML.aurum.scrolls.Lectern.RefillGUIListener;
import BHML.aurum.scrolls.core.ScrollParticleTask;
import BHML.aurum.scrolls.core.ScrollRegistry;
import BHML.aurum.scrolls.fire.Fireball;
import BHML.aurum.utils.Keys;
import org.bukkit.plugin.java.JavaPlugin;

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

        //testing bow mechanics
        getServer().getPluginManager().registerEvents(new BowListener(), this);

        getLogger().info("***Aurum loaded***");
    }

    @Override
    public void onDisable() {
        getLogger().info("Aurum disabled.");
    }
}

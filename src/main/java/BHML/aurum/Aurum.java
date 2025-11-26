package BHML.aurum;

import BHML.aurum.commands.GiveScrollCommand;
import BHML.aurum.listeners.ScrollListener;
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




        getServer().getPluginManager().registerEvents(new ScrollListener(this), this);
        registerCommands();


        //Register Scrolls
        ScrollRegistry.register(new Fireball());

        //Register passive particle effects
        new ScrollParticleTask(this).runTaskTimer(this, 0L, 5L);


        getLogger().info("***Aurum loaded***");
    }

    @Override
    public void onDisable() {
        getLogger().info("Aurum disabled.");
    }
}

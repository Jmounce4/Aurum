package BHML.aurum;

import BHML.aurum.commands.GiveScrollCommand;
import BHML.aurum.listeners.ScrollListener;
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
        getLogger().info("Aurum loaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Aurum disabled.");
    }
}

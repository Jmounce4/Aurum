package BHML.aurum.commands;

import BHML.aurum.Aurum;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AurumCommand implements CommandExecutor {

    private final Aurum plugin;

    public AurumCommand(Aurum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || !args[0].equalsIgnoreCase("pvp")) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /aurum pvp");
            return true;
        }

        // Toggle PvP
        if (plugin.hasPvPEnabled(player.getUniqueId())) {
            plugin.disablePvP(player);
            player.sendMessage(ChatColor.RED + "PvP disabled.");
        } else {
            plugin.enablePvP(player);
            player.sendMessage(ChatColor.GREEN + "PvP enabled.");
        }

        return true;
    }

}

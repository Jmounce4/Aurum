package BHML.aurum.commands;

import BHML.aurum.Aurum;
import BHML.aurum.scrolls.core.Scroll;
import BHML.aurum.scrolls.core.ScrollFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GiveScrollCommand implements CommandExecutor, TabCompleter {

    private final Aurum plugin;

    public GiveScrollCommand(Aurum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cUsage: /givescroll <id>");
            return true;
        }

        String id = args[0];
        Scroll scroll = ScrollFactory.getScroll(id);

        if (scroll == null) {
            player.sendMessage("§cUnknown scroll ID: " + id);
            return true;
        }

        ItemStack item = ScrollFactory.createScrollItem(scroll);
        player.getInventory().addItem(item);

        player.sendMessage("§aGiven scroll: §e" + scroll.getId());
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {

        if (args.length == 1) {
            return new ArrayList<>(ScrollFactory.getScrolls().keySet());
        }

        return null;
    }


}

package me.rhydium.rKitPvP.utils;

import me.rhydium.rKitPvP.commands.KitPvPCommand;
import me.rhydium.rKitPvP.commands.KitPvPCommand.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class KitPvPTabCompleter implements TabCompleter {

    private final KitPvPCommand commandHandler;

    public KitPvPTabCompleter(KitPvPCommand commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            return commandHandler.getSubCommands().entrySet().stream()
                    .filter(entry -> {
                        String perm = entry.getValue().getPermission();
                        return perm == null || sender.hasPermission(perm);
                    })
                    .map(Map.Entry::getKey)
                    .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        SubCommand sub = commandHandler.getSubCommands().get(args[0].toLowerCase());
        if (sub != null && (sub.getPermission() == null || sender.hasPermission(sub.getPermission()))) {
            return sub.onTabComplete(player, args);
        }

        return List.of();
    }
}

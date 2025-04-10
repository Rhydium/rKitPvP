package me.rhydium.rKitPvP.utils;

import me.rhydium.rKitPvP.commands.KitPvPCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class KitPvPTabCompleter implements TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("build", "setspawn");
    private final KitPvPCommand commandHandler;

    public KitPvPTabCompleter(KitPvPCommand commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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

        return List.of();
    }
}

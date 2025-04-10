package me.rhydium.rKitPvP.commands;

import me.rhydium.rKitPvP.rKitPvP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class KitPvPCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public KitPvPCommand(rKitPvP plugin) {
        subCommands.put("build", new BuildCommand());
        subCommands.put("setspawn", new SetSpawnCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Please specify a subcommand.");
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            if (sub.getPermission() != null && !player.hasPermission(sub.getPermission())) {
                player.sendMessage(sub.getNoPermissionMessage());
                return true;
            }

            return sub.execute(player, args);
        } else {
            player.sendMessage(args[0] + " is not a valid subcommand.");
            return true;
        }
    }

    public interface SubCommand {
        boolean execute(Player player, String[] args);
        String getPermission();
        default String getNoPermissionMessage() {
            return "You do not have permission to use this command.";
        }
    }

    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }
}

package me.rhydium.rKitPvP.commands;

import me.rhydium.rKitPvP.rKitPvP;
import me.rhydium.rKitPvP.utils.AntiGriefUtility;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BuildCommand implements KitPvPCommand.SubCommand {

    public BuildCommand(rKitPvP plugin) {}

    @Override
    public String getPermission() {
        return "kitpvp.admin.build";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        UUID playerId = player.getUniqueId();
        if (AntiGriefUtility.buildModePlayers.contains(playerId)) {
            AntiGriefUtility.buildModePlayers.remove(playerId);
            player.sendMessage("Build mode disabled.");
        } else {
            AntiGriefUtility.buildModePlayers.add(playerId);
            player.sendMessage("Build mode enabled.");
        }
        return true;
    }
}
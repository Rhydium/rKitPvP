package me.rhydium.rKitPvP.commands;

import me.rhydium.rKitPvP.rKitPvP;
import me.rhydium.rKitPvP.utils.WorldGuardUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

public class SetSpawnRegionCommand implements KitPvPCommand.SubCommand {

    private final rKitPvP plugin;

    public SetSpawnRegionCommand(rKitPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPermission() {
        return "kitpvp.admin.setspawnregion";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return WorldGuardUtils.getRegionsInPlayerWorld(player).stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return List.of();
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /kitpvp setspawnregion <region_name>");
            return false;
        }

        String regionName = args[1];

        plugin.getConfig().set("spawn-region", regionName);
        plugin.saveConfig();
        player.sendMessage(Component.text("Set spawn region to " + regionName).color(NamedTextColor.GREEN));

        return true;
    }
}

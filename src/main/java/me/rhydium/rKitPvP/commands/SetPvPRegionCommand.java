package me.rhydium.rKitPvP.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.rhydium.rKitPvP.rKitPvP;
import me.rhydium.rKitPvP.utils.WorldGuardUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class SetPvPRegionCommand implements KitPvPCommand.SubCommand {

    private final rKitPvP plugin;

    public SetPvPRegionCommand(rKitPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPermission() {
        return "kitpvp.admin.setpvpregion";
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

        plugin.getConfig().set("pvp-region", regionName);
        plugin.saveConfig();
        player.sendMessage(Component.text("Set spawn region to " + regionName).color(NamedTextColor.GREEN));

        return true;
    }
}

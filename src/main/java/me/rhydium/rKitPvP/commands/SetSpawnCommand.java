package me.rhydium.rKitPvP.commands;

import me.rhydium.rKitPvP.rKitPvP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements KitPvPCommand.SubCommand {

    private final rKitPvP plugin;

    public SetSpawnCommand(rKitPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPermission() {
        return "kitpvp.admin.setspawn";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        Location location = player.getLocation();
        FileConfiguration spawnConfig = plugin.getSpawnConfigManager().getConfig();
        spawnConfig.set("spawn.world", location.getWorld().getName());
        spawnConfig.set("spawn.x", location.getX());
        spawnConfig.set("spawn.y", location.getY());
        spawnConfig.set("spawn.z", location.getZ());
        spawnConfig.set("spawn.yaw", location.getYaw());
        spawnConfig.set("spawn.pitch", location.getPitch());
        plugin.getSpawnConfigManager().saveConfig();

        player.sendMessage(Component.text("Spawn set!").color(NamedTextColor.GREEN));

        return true;
    }
}
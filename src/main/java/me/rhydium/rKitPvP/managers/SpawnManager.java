package me.rhydium.rKitPvP.managers;

import me.rhydium.rKitPvP.rKitPvP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SpawnManager {

    private final rKitPvP plugin;
    private boolean useFallbackSpawn = false;
    private final Set<String> warnedPlayers = new HashSet<>();

    public SpawnManager(rKitPvP plugin) {
        this.plugin = plugin;
    }

    public Location getSpawnLocation() {
        FileConfiguration spawnConfig = plugin.getSpawnConfigManager().getConfig();

        if (!spawnConfig.contains("spawn.world") ||
                !spawnConfig.contains("spawn.x") ||
                !spawnConfig.contains("spawn.y") ||
                !spawnConfig.contains("spawn.z")) {

            useFallbackSpawn = true;

            World fallbackWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
            if (fallbackWorld == null) {
                plugin.getLogger().severe("No worlds loaded – fallback spawn impossible!");
                return new Location(new WorldCreator("world").createWorld(), 0, 100, 0);
            }

            plugin.getLogger().warning("Spawn location not set! As a fallback the world spawn is used.");
            return fallbackWorld.getSpawnLocation();
        }

        String worldName = spawnConfig.getString("spawn.world");
        if (worldName == null) {
            plugin.getLogger().warning("spawn.world is missing in spawn.yml. Fallback is being used.");
            useFallbackSpawn = true;
            return Bukkit.getWorlds().getFirst().getSpawnLocation();
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Spawn world '" + worldName + "' not found. Fallback is being used.");
            useFallbackSpawn = true;
            return Bukkit.getWorlds().getFirst().getSpawnLocation();
        }

        double x = spawnConfig.getDouble("spawn.x");
        double y = spawnConfig.getDouble("spawn.y");
        double z = spawnConfig.getDouble("spawn.z");
        float yaw = (float) spawnConfig.getDouble("spawn.yaw");
        float pitch = (float) spawnConfig.getDouble("spawn.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void notifyIfFallbackUsed(Player player) {
        if (useFallbackSpawn && player.hasPermission("kitpvp.admin.setspawn") && warnedPlayers.add(player.getUniqueId().toString())) {
            player.sendMessage(Component.text("No spawn location is set. A fallback is being used.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
        }
    }
}

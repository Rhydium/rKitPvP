package me.rhydium.rKitPvP.managers;

import me.rhydium.rKitPvP.rKitPvP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class SpawnManager {

    private final rKitPvP plugin;

    public SpawnManager(rKitPvP plugin) {
        this.plugin = plugin;
    }

    public Location getSpawnLocation() {
        FileConfiguration spawnConfig = plugin.getSpawnConfigManager().getConfig();
        String worldName = spawnConfig.getString("spawn.world");
        World world = Bukkit.getWorld(worldName != null ? worldName : "world");

        if (world == null) {
            throw new IllegalStateException("World not found: " + worldName);
        }

        double x = spawnConfig.getDouble("spawn.x");
        double y = spawnConfig.getDouble("spawn.y");
        double z = spawnConfig.getDouble("spawn.z");
        float pitch = (float) spawnConfig.getDouble("spawn.pitch");
        float yaw = (float) spawnConfig.getDouble("spawn.yaw");

        return new Location(world, x, y, z, yaw, pitch);
    }
}

package me.rhydium.rKitPvP.managers;

import me.rhydium.rKitPvP.rKitPvP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CustomConfigManager {
    private final rKitPvP plugin;
    private final String fileName;
    private File configFile;
    private FileConfiguration config;


    public CustomConfigManager(rKitPvP plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        createConfig();
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), fileName);

        if (!configFile.exists()) {
            File parentDir = configFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                plugin.getLogger().warning("Failed to create the parent directory for the custom config file: " + fileName);
                return;
            }

            try {
                plugin.saveResource(fileName, false);
            } catch (IllegalArgumentException e) {
                try {
                    if (!configFile.createNewFile()) {
                        plugin.getLogger().warning("Failed to create a new custom config file: " + fileName);
                    }
                } catch (IOException ioException) {
                    plugin.getLogger().severe("An error occurred while trying to create a custom config: " + ioException.getMessage());
                }
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("An error occurred while trying to save a custom config: " + e.getMessage());
        }
    }
}
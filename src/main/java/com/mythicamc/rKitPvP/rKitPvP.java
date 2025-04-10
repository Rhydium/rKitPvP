package com.mythicamc.rKitPvP;

import com.mythicamc.rKitPvP.commands.KitPvPCommand;
import com.mythicamc.rKitPvP.commands.SpawnCommand;
import com.mythicamc.rKitPvP.listeners.PlayerJoinListener;
import com.mythicamc.rKitPvP.listeners.PlayerQuitListener;
import com.mythicamc.rKitPvP.listeners.PvPListener;
import com.mythicamc.rKitPvP.managers.*;
import com.mythicamc.rKitPvP.utils.AntiGriefUtility;
import com.mythicamc.rKitPvP.utils.KitPvPTabCompleter;
import com.mythicamc.rKitPvP.utils.KitSelectorGUI;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class rKitPvP extends JavaPlugin {

    private CombatManager combatManager;
    private PlayerScoreboardManager scoreboardManager;
    private PlayerStatsManager statsManager;
    private DatabaseManager databaseManager;
    private CustomConfigManager spawnConfigManager;

    @Override
    public void onEnable() {
        // Save the default config if it doesn't exist
        saveDefaultConfig();

        // Initialize DatabaseManager
        DatabaseManager.DatabaseType dbType = getConfig().getString("database.type").equalsIgnoreCase("mysql") ? DatabaseManager.DatabaseType.MYSQL : DatabaseManager.DatabaseType.SQLITE;
        databaseManager = new DatabaseManager(this, dbType);
        databaseManager.connect();

        // Initialize other managers
        combatManager = new CombatManager(this);
        statsManager = new PlayerStatsManager(databaseManager);
        scoreboardManager = new PlayerScoreboardManager(this);

        // Initialize custom config managers
        spawnConfigManager = new CustomConfigManager(this, "spawn.yml");

        // Start the scoreboard updater
        scoreboardManager.startScoreboardUpdater();

        // Register commands
        registerCommands();

        // Register event listeners
        registerListeners();

        // Tell console we're running!
        getLogger().info("KitPvP has been enabled.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().info("KitPvP is disabled!");
    }

    @SuppressWarnings("DataFlowIssue")
    private void registerCommands() {
        KitPvPCommand kitPvPCommand = new KitPvPCommand(this);

        PluginCommand kitpvp = getCommand("kitpvp");
        if (kitpvp != null) {
            getCommand("kitpvp").setExecutor(kitPvPCommand);
            getCommand("kitpvp").setTabCompleter(new KitPvPTabCompleter(kitPvPCommand));
        } else {
            getLogger().warning("Command 'kitpvp' not found in plugin.yml. Please check your plugin.yml file.");
        }

        PluginCommand spawn = getCommand("spawn");
        if (spawn != null) {
            getCommand("spawn").setExecutor(new SpawnCommand(this));
        } else {
            getLogger().warning("Command 'spawn' not found in plugin.yml. Please check your plugin.yml file.");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPListener(this, new SpawnCommand(this)), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new KitSelectorGUI(this), this);
        getServer().getPluginManager().registerEvents(new AntiGriefUtility(), this);
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public PlayerScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public PlayerStatsManager getStatsManager() {
        return statsManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    // Custom configs
    public CustomConfigManager getSpawnConfigManager() {
        return spawnConfigManager;
    }
}

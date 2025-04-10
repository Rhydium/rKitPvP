package me.rhydium.rKitPvP;

import me.rhydium.rKitPvP.commands.KitPvPCommand;
import me.rhydium.rKitPvP.commands.SpawnCommand;
import me.rhydium.rKitPvP.listeners.PlayerJoinListener;
import me.rhydium.rKitPvP.listeners.PlayerQuitListener;
import me.rhydium.rKitPvP.listeners.PvPListener;
import me.rhydium.rKitPvP.managers.*;
import me.rhydium.rKitPvP.utils.AntiGriefUtility;
import me.rhydium.rKitPvP.utils.KitPvPTabCompleter;
import me.rhydium.rKitPvP.utils.KitSelectorGUI;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class rKitPvP extends JavaPlugin {

    private CombatManager combatManager;
    private PlayerScoreboardManager scoreboardManager;
    private PlayerStatsManager statsManager;
    private DatabaseManager databaseManager;
    private CustomConfigManager spawnConfigManager;
    private SpawnManager spawnManager;

    @Override
    public void onEnable() {
        // Save the default config if it doesn't exist
        saveDefaultConfig();

        // Initialize DatabaseManager
        String dbTypeString = getConfig().getString("database.type");
        DatabaseManager.DatabaseType dbType = (dbTypeString != null && dbTypeString.equalsIgnoreCase("mysql"))
                ? DatabaseManager.DatabaseType.MYSQL
                : DatabaseManager.DatabaseType.SQLITE;
        databaseManager = new DatabaseManager(this, dbType);
        databaseManager.connect();

        // Initialize other managers
        combatManager = new CombatManager(this);
        statsManager = new PlayerStatsManager(this, databaseManager);
        scoreboardManager = new PlayerScoreboardManager(this);
        spawnManager = new SpawnManager(this);

        // Initialize custom config managers
        spawnConfigManager = new CustomConfigManager(this, "spawn.yml");

        // Start the scoreboard updater
        scoreboardManager.startScoreboardUpdater();

        // Register commands
        registerCommands();

        // Register event listeners
        registerListeners();

        for (Player online : getServer().getOnlinePlayers()) {
            statsManager.initializePlayerStats(online);
            scoreboardManager.createScoreboard(online);
            scoreboardManager.updateScoreboard(online);
        }

        // Tell console we're running!
        getLogger().info("KitPvP has been enabled.");
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            statsManager.updateStatsInDatabase(player);
            scoreboardManager.removeScoreboard(player);
            combatManager.removeTag(player);
        }

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
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
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

    public CustomConfigManager getSpawnConfigManager() {
        return spawnConfigManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }
}

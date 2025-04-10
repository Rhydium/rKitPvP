package me.rhydium.rKitPvP.listeners;

import me.rhydium.rKitPvP.rKitPvP;
import me.rhydium.rKitPvP.utils.KitSelectorGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final rKitPvP plugin;

    public PlayerJoinListener(rKitPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Initialize stats
        plugin.getStatsManager().initializePlayerStats(player);

        // Create scoreboard
        plugin.getScoreboardManager().createScoreboard(player);

        // Update scoreboard
        plugin.getScoreboardManager().updateScoreboard(player);

        // Give player a chest item for kit selection
        KitSelectorGUI.givePlayerKitSelectorItem(player);
    }
}
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

        plugin.getStatsManager().initializePlayerStats(player);
        plugin.getScoreboardManager().createScoreboard(player);
        plugin.getScoreboardManager().updateScoreboard(player);

        KitSelectorGUI.givePlayerKitSelectorItem(player);
    }
}
package me.rhydium.rKitPvP.listeners;

import me.rhydium.rKitPvP.managers.CombatManager;
import me.rhydium.rKitPvP.managers.PlayerStatsManager;
import me.rhydium.rKitPvP.rKitPvP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final rKitPvP plugin;
    private final CombatManager combatManager;
    private final PlayerStatsManager playerStatsManager;

    public PlayerQuitListener(rKitPvP plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
        this.playerStatsManager = plugin.getStatsManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Check if player is tagged, if so punish them.
        if (combatManager.isTagged(player)) {
            punishPlayerForCombatLogging(player);
        }

        // Save PlayerStats to database
        playerStatsManager.updateStatsInDatabase(player);

        // Remove scoreboard
        plugin.getScoreboardManager().removeScoreboard(player);

        // Clear inventory
        player.getInventory().clear();

        // Clean up any combat tags
        combatManager.removeTag(player);
    }

    private void punishPlayerForCombatLogging(Player p) {
        // Add death to PlayerStats
        playerStatsManager.addDeath(p);

        // Remove 100 coins as punishment (or set coins to 0 if player has < 100 coins)
        if (playerStatsManager.getCoins(p) >= 100) {
            playerStatsManager.removeCoins(p, 100);
        } else if (playerStatsManager.getCoins(p) < 100) {
            playerStatsManager.setCoins(p, 0);
        }

        // Ban the player using LiteBans
        String banDuration = plugin.getConfig().getString("combat.ban-duration");
        String reason = "Combat Logging";

        // Execute LiteBans ban command
        String command = String.format("tempban %s %s %s", p.getName(), banDuration, reason);
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
    }
}
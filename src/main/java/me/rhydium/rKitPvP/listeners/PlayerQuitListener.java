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

        if (combatManager.isTagged(player)) {
            punishPlayerForCombatLogging(player);
        }

        playerStatsManager.updateStatsInDatabase(player);
        plugin.getScoreboardManager().removeScoreboard(player);
        player.getInventory().clear();
        combatManager.removeTag(player);
    }

    private void punishPlayerForCombatLogging(Player p) {
        playerStatsManager.addDeath(p);

        if (playerStatsManager.getCoins(p) >= 100) {
            playerStatsManager.removeCoins(p, 100);
        } else if (playerStatsManager.getCoins(p) < 100) {
            playerStatsManager.setCoins(p, 0);
        }

        String banDuration = plugin.getConfig().getString("combat.ban-duration");
        String reason = "Combat Logging";

        String command = String.format("tempban %s %s %s", p.getName(), banDuration, reason);
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
    }
}
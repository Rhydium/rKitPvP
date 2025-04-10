package me.rhydium.rKitPvP.managers;

import me.rhydium.rKitPvP.rKitPvP;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {

    private final rKitPvP plugin;
    private final HashMap<UUID, Long> combatTaggedPlayers;
    private final Map<UUID, BukkitTask> combatTagTimers = new HashMap<>();

    public CombatManager(rKitPvP plugin) {
        this.plugin = plugin;
        this.combatTaggedPlayers = new HashMap<>();
    }

    public boolean tagPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long tagDuration = plugin.getConfig().getLong("combat.tag-duration") * 1000;
        if (tagDuration <= 0) {
            plugin.getLogger().warning("Invalid combat tag duration: " + tagDuration + "ms. Check your config.yml.");
            tagDuration = 15000;
        }
        long tagExpiryTime = currentTime + tagDuration;

        boolean wasAlreadyTagged = isTagged(player);

        combatTaggedPlayers.put(playerId, tagExpiryTime);
        startCombatTagTimer(player);

        return wasAlreadyTagged;
    }

    public boolean isTagged(Player player) {
        UUID playerId = player.getUniqueId();
        Long tagExpiryTime = combatTaggedPlayers.get(playerId);
        if (tagExpiryTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > tagExpiryTime) {
            combatTaggedPlayers.remove(playerId);
            return false;
        }
        return true;
    }

    public void removeTag(Player player) {
        cancelCombatTagTimer(player);
        combatTaggedPlayers.remove(player.getUniqueId());
    }

    public long getRemainingTagTime(Player player) {
        UUID playerId = player.getUniqueId();
        Long tagExpiryTime = combatTaggedPlayers.get(playerId);
        if (tagExpiryTime == null) {
            return 0;
        }
        long remainingTime = tagExpiryTime - System.currentTimeMillis();
        return Math.max(remainingTime, 0);
    }

    private void cancelCombatTagTimer(Player player) {
        UUID playerId = player.getUniqueId();
        if (combatTagTimers.containsKey(playerId)) {
            combatTagTimers.get(playerId).cancel();
            combatTagTimers.remove(playerId);
        }
    }

    private void startCombatTagTimer(Player player) {
        UUID playerId = player.getUniqueId();

        if (combatTagTimers.containsKey(playerId)) {
            combatTagTimers.get(playerId).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    combatTagTimers.remove(playerId);
                    return;
                }

                long remainingTime = getRemainingTagTime(player);
                int secondsLeft = (int) Math.ceil(remainingTime / 1000.0);

                if (remainingTime <= 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                    String actionBarMessage = "&aYou are no longer in combat!";
                    player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(actionBarMessage));

                    String untagMessage = plugin.getConfig().getString("messages.combat-untagged", "&aYou are no longer in combat!");
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(untagMessage));

                    removeTag(player);

                    plugin.getScoreboardManager().updateScoreboard(player);

                    cancel();
                    combatTagTimers.remove(playerId);
                    return;
                }

                if (secondsLeft <= 3) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }

                String actionBarMessage = "&cIn combat: " + secondsLeft + " seconds!";
                player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(actionBarMessage));
            }
        }.runTaskTimer(plugin, 0L, 20L);

        combatTagTimers.put(playerId, task);
    }
}
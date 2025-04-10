package me.rhydium.rKitPvP.listeners;

import me.rhydium.rKitPvP.commands.SpawnCommand;
import me.rhydium.rKitPvP.managers.CombatManager;
import me.rhydium.rKitPvP.rKitPvP;
import me.rhydium.rKitPvP.utils.KitSelectorGUI;
import me.rhydium.rKitPvP.utils.WorldGuardUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class PvPListener implements Listener {

    private final rKitPvP plugin;
    private final CombatManager combatManager;
    private final SpawnCommand spawnCommand;

    public PvPListener(rKitPvP plugin, SpawnCommand spawnCommand) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
        this.spawnCommand = spawnCommand;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player damaged) {
            // Determine the source of the damage
            Entity damagerEntity = event.getDamager();
            Player damager = null;

            // If the damager is a player, assign directly
            if (damagerEntity instanceof Player) {
                damager = (Player) damagerEntity;
            }
            // If the damager is a projectile get its shooter
            else if (damagerEntity instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                }
            }

            // Check if event happens in spawn
            if (WorldGuardUtils.isInRegion(damager, "spawn") || WorldGuardUtils.isInRegion(damaged, "spawn")) {
                event.setCancelled(true);
                if (damager != null) {
                    damager.sendMessage("PvP is not allowed in the spawn area.");
                }
                return;
            }

            // Tag both players only if the damager is a player
            if (damager != null) {
                boolean damagedWasTagged = combatManager.tagPlayer(damaged);
                boolean damagerWasTagged = combatManager.tagPlayer(damager);

                // Notify players only if they were not already tagged
                String tagMessage = plugin.getConfig().getString("messages.combat-tagged", "&cYou are now in combat!");
                if (!damagerWasTagged) {
                    damager.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(tagMessage));
                }
                if (!damagedWasTagged) {
                    damaged.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(tagMessage));
                }
            }

            // Fake death behavior
            double finalDamage = event.getFinalDamage();
            double health = damaged.getHealth();

            // Check if the player would die from this damage
            if (health - finalDamage <= 0) {
                event.setCancelled(true); // Prevent actual death
                simulateDeath(damaged, damager); // Simulate death, passing damager (null if not player)
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        // Skip handling if this is an EntityDamageByEntityEvent to avoid duplication
        if (e instanceof EntityDamageByEntityEvent) {
            return;
        }

        // Check if the damaged entity is a player
        if (e.getEntity() instanceof Player victim) {
            double finalDamage = e.getFinalDamage();
            double health = victim.getHealth();

            // Check if the player would die from this damage
            if (health - finalDamage <= 0) {
                e.setCancelled(true); // Prevent actual death
                simulateDeath(victim, null); // Simulate death with no specific killer
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // TO DO: Check if player tries to leave spawn area without a kit selected.
    }

    private void startRespawnCountdown(Player player) {
        int countdownTime = plugin.getConfig().getInt("respawn.countdown", 3);

        // Set player to Spectator mode
        player.setGameMode(GameMode.SPECTATOR);

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = countdownTime;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    String title = "&cYOU DIED!";
                    String subtitle = "&eRespawning in " + timeLeft + "...";
                    player.showTitle(Title.title(LegacyComponentSerializer.legacyAmpersand().deserialize(title), LegacyComponentSerializer.legacyAmpersand().deserialize(subtitle), Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(1))));

                    timeLeft--;
                } else {
                    // Set player back to Survival mode
                    player.setGameMode(GameMode.SURVIVAL);

                    // Teleport player to spawn location
                    Location respawnLocation = spawnCommand.getSpawnLocation();
                    player.teleport(respawnLocation);

                    // Give player the kit selector
                    KitSelectorGUI.givePlayerKitSelectorItem(player);

                    // Send a title upon respawn
                    String respawnTitle = "&aReady to fight.";
                    player.showTitle(Title.title(LegacyComponentSerializer.legacyAmpersand().deserialize(respawnTitle), Component.text(" "), Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(1000))));

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void simulateDeath(Player victim, Player killer) {
        // Remove combat tag and cancel timers
        combatManager.removeTag(victim);

        // Send action bar and chat message
        String endOfCombatMessage = "&aYou are no longer in combat!";
        victim.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(endOfCombatMessage));
        victim.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(endOfCombatMessage));

        // Clear effects and set health
        victim.setHealth(20);
        victim.setFoodLevel(20);
        victim.setFireTicks(0);
        victim.getActivePotionEffects().forEach(potionEffect -> victim.removePotionEffect(potionEffect.getType()));

        // Handle inventory and experience
        victim.getInventory().clear();
        victim.setExp(0);
        victim.setLevel(0);

        // Update stats
        plugin.getStatsManager().addDeath(victim);
        if (killer != null) {
            // Send messages to victim and killer
            sendDeathMessages(victim, killer);

            plugin.getStatsManager().addKill(killer);

            // Reward killer with coins
            int killReward = plugin.getConfig().getInt("kill-reward", 10);
            plugin.getStatsManager().addCoins(killer, killReward);

            // Notify killer
            killer.sendMessage(Component.text("You received " + killReward + " coins for killing " + victim.getName() + "!").color(NamedTextColor.GREEN));
        }

        // Update the scoreboards
        plugin.getScoreboardManager().updateScoreboard(victim);
        if (killer != null) {
            plugin.getScoreboardManager().updateScoreboard(killer);
        }

        // Start respawn countdown
        startRespawnCountdown(victim);
    }

    private void sendDeathMessages(Player victim, Player killer) {
        TextComponent victimMessage;
        TextComponent killerMessage;

        if (killer != null) {
            victimMessage = Component.text("You were killed by " + killer.getName() + "!").color(NamedTextColor.RED);
            killerMessage = Component.text("You killed " + victim.getName() + "!").color(NamedTextColor.GREEN);
        } else {
            // If no killer (e.g., environmental damage)
            victimMessage = Component.text("You died.").color(NamedTextColor.RED);
            killerMessage = null;
        }

        // Send message to victim
        victim.sendMessage(victimMessage);

        // Send message to killer if applicable
        if (killerMessage != null) {
            killer.sendMessage(killerMessage);
        }
    }
}
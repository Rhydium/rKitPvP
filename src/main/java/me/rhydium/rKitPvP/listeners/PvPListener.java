package me.rhydium.rKitPvP.listeners;

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

import java.time.Duration;

public class PvPListener implements Listener {

    private final rKitPvP plugin;
    private final CombatManager combatManager;

    public PvPListener(rKitPvP plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player damaged) {
            Entity damagerEntity = event.getDamager();
            Player damager = null;

            if (damagerEntity instanceof Player) {
                damager = (Player) damagerEntity;
            } else if (damagerEntity instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                }
            }

            if (WorldGuardUtils.isInRegion(damager, "spawn") || WorldGuardUtils.isInRegion(damaged, "spawn")) {
                event.setCancelled(true);
                if (damager != null) {
                    damager.sendMessage("PvP is not allowed in the spawn area.");
                }
                return;
            }

            if (damager != null) {
                boolean damagedWasTagged = combatManager.tagPlayer(damaged);
                boolean damagerWasTagged = combatManager.tagPlayer(damager);

                String tagMessage = plugin.getConfig().getString("messages.combat-tagged", "&cYou are now in combat!");
                if (!damagerWasTagged) {
                    damager.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(tagMessage));
                }
                if (!damagedWasTagged) {
                    damaged.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(tagMessage));
                }
            }

            double finalDamage = event.getFinalDamage();
            double health = damaged.getHealth();

            if (health - finalDamage <= 0) {
                event.setCancelled(true);
                simulateDeath(damaged, damager);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e instanceof EntityDamageByEntityEvent) {
            return;
        }

        if (e.getEntity() instanceof Player victim) {
            double finalDamage = e.getFinalDamage();
            double health = victim.getHealth();

            if (health - finalDamage <= 0) {
                e.setCancelled(true);
                simulateDeath(victim, null);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // TO DO: Check if player tries to leave spawn area without a kit selected.
    }

    private void startRespawnCountdown(Player player) {
        int countdownTime = plugin.getConfig().getInt("respawn.countdown", 3);

        player.setGameMode(GameMode.SPECTATOR);

        new BukkitRunnable() {
            int timeLeft = countdownTime;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    String title = "&cYOU DIED!";
                    String subtitle = "&eRespawning in " + timeLeft + "...";
                    player.showTitle(Title.title(LegacyComponentSerializer.legacyAmpersand().deserialize(title), LegacyComponentSerializer.legacyAmpersand().deserialize(subtitle), Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(1))));

                    timeLeft--;
                } else {
                    player.setGameMode(GameMode.SURVIVAL);

                    Location respawnLocation = plugin.getSpawnManager().getSpawnLocation();
                    player.teleport(respawnLocation);

                    KitSelectorGUI.givePlayerKitSelectorItem(player);

                    String respawnTitle = "&aReady to fight.";
                    player.showTitle(Title.title(LegacyComponentSerializer.legacyAmpersand().deserialize(respawnTitle), Component.text(" "), Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(1000))));

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void simulateDeath(Player victim, Player killer) {
        combatManager.removeTag(victim);

        String endOfCombatMessage = "&aYou are no longer in combat!";
        victim.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(endOfCombatMessage));
        victim.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(endOfCombatMessage));

        victim.setHealth(20);
        victim.setFoodLevel(20);
        victim.setFireTicks(0);
        victim.getActivePotionEffects().forEach(potionEffect -> victim.removePotionEffect(potionEffect.getType()));

        victim.getInventory().clear();
        victim.setExp(0);
        victim.setLevel(0);

        plugin.getStatsManager().addDeath(victim);
        if (killer != null) {
            sendDeathMessages(victim, killer);

            plugin.getStatsManager().addKill(killer);

            int killReward = plugin.getConfig().getInt("kill-reward", 10);
            plugin.getStatsManager().addCoins(killer, killReward);

            killer.sendMessage(Component.text("You received " + killReward + " coins for killing " + victim.getName() + "!").color(NamedTextColor.GREEN));
        }

        plugin.getScoreboardManager().updateScoreboard(victim);
        if (killer != null) {
            plugin.getScoreboardManager().updateScoreboard(killer);
        }

        startRespawnCountdown(victim);
    }

    private void sendDeathMessages(Player victim, Player killer) {
        TextComponent victimMessage;
        TextComponent killerMessage;

        if (killer != null) {
            victimMessage = Component.text("You were killed by " + killer.getName() + "!").color(NamedTextColor.RED);
            killerMessage = Component.text("You killed " + victim.getName() + "!").color(NamedTextColor.GREEN);
        } else {
            victimMessage = Component.text("You died.").color(NamedTextColor.RED);
            killerMessage = null;
        }

        victim.sendMessage(victimMessage);

        if (killerMessage != null) {
            killer.sendMessage(killerMessage);
        }
    }
}
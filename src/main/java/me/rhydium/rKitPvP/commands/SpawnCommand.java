package me.rhydium.rKitPvP.commands;

import me.rhydium.rKitPvP.rKitPvP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    private final rKitPvP plugin;

    public SpawnCommand(rKitPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (sender instanceof Player player) {
            if (plugin.getCombatManager().isTagged(player)) {
                player.sendMessage("You cannot use this command while in combat.");
                return true;
            } else {
                player.sendMessage("Teleporting to spawn in 5 seconds...");
                new BukkitRunnable() {
                    int countdown = 5;

                    @Override
                    public void run() {
                        if (plugin.getCombatManager().isTagged(player)) {
                            player.sendMessage("Teleportation canceled due to combat tag.");
                            cancel();
                            return;
                        }

                        if (countdown > 0) {
                            player.sendMessage("Teleporting in " + countdown + "...");
                            player.sendActionBar(Component.text("Teleporting in " + countdown + "...").color(NamedTextColor.DARK_GREEN));
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                            countdown--;
                        } else {
                            Location spawnLocation = plugin.getSpawnManager().getSpawnLocation();
                            player.teleport(spawnLocation);
                            player.sendMessage("Teleported to spawn.");
                            player.sendActionBar(Component.text("You have been teleported to spawn.").color(NamedTextColor.GREEN));
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
            }
        }

        return true;
    }
}
package com.mythicamc.rKitPvP.listeners;

import com.mythicamc.rKitPvP.managers.CombatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class PlayerCommandListener implements Listener {

    private final CombatManager combatManager;

    public PlayerCommandListener(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (combatManager.isTagged(player)) {
            List<String> blockedCommands = List.of("/spawn");
            String command = e.getMessage().split(" ")[0].toLowerCase();

            if (blockedCommands.contains(command)) {
                player.sendMessage(Component.text("You cannot use that command while in combat!").color(NamedTextColor.RED));
                e.setCancelled(true);
            }
        }
    }
}
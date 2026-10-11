package com.dario.controlpanel.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener de ejemplo: saluda a cada jugador al entrar.
 */
public final class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(
                Component.text("¡Bienvenido, " + event.getPlayer().getName() + "!", NamedTextColor.GOLD));
    }
}

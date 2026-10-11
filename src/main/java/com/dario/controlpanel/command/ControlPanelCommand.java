package com.dario.controlpanel.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.Plugin;

/**
 * Comando /controlpanel: muestra la versión del plugin.
 * Añade subcomandos con .then(Commands.literal("nombre")...).
 */
public final class ControlPanelCommand {

    private ControlPanelCommand() {
    }

    public static LiteralCommandNode<CommandSourceStack> create(Plugin plugin) {
        return Commands.literal("controlpanel")
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(Component.text(
                            plugin.getName() + " v" + plugin.getPluginMeta().getVersion(), NamedTextColor.GREEN));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}

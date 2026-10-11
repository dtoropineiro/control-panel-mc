package com.dario.controlpanel.command;

import com.dario.controlpanel.difficulty.DifficultySwitch;
import com.dario.controlpanel.lever.BindingStore;
import com.dario.controlpanel.lever.LeverBinding;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comando /leverctl bind|unbind|info: elige qué palanca controla la dificultad.
 */
public final class LeverCommand {

    private static final int REACH = 5;

    private final BindingStore store;
    private final DifficultySwitch difficultySwitch;
    private final Logger logger;

    public LeverCommand(BindingStore store, DifficultySwitch difficultySwitch, Logger logger) {
        this.store = store;
        this.difficultySwitch = difficultySwitch;
        this.logger = logger;
    }

    public LiteralCommandNode<CommandSourceStack> create() {
        return Commands.literal("leverctl")
                .requires(source -> source.getSender().hasPermission("leverctl.admin"))
                .then(Commands.literal("bind").executes(this::bind))
                .then(Commands.literal("unbind").executes(this::unbind))
                .then(Commands.literal("info").executes(this::info))
                .build();
    }

    private int bind(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            sender.sendMessage(Component.text("Solo un jugador puede vincular una palanca.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Block block = player.getTargetBlockExact(REACH);
        if (block == null || block.getType() != Material.LEVER) {
            sender.sendMessage(Component.text(
                    "Mira a una palanca a " + REACH + " bloques o menos.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        LeverBinding binding = LeverBinding.of(block);
        try {
            store.save(binding);
        } catch (IOException e) {
            logger.log(Level.WARNING, "No se pudo guardar bindings.yml", e);
            sender.sendMessage(Component.text(
                    "No se pudo guardar bindings.yml: " + e.getMessage(), NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        // Desde el bind manda la palanca, aunque baje una dificultad puesta a mano.
        if (block.getBlockData() instanceof Powerable powerable) {
            difficultySwitch.apply(powerable.isPowered());
        }
        sender.sendMessage(Component.text("Palanca vinculada en " + binding.describe(), NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int unbind(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        // No toca la dificultad: desconectar no es apagar.
        try {
            store.clear();
        } catch (IOException e) {
            logger.log(Level.WARNING, "No se pudo guardar bindings.yml", e);
            sender.sendMessage(Component.text(
                    "No se pudo guardar bindings.yml: " + e.getMessage(), NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }
        sender.sendMessage(Component.text("Palanca desvinculada.", NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int info(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        LeverBinding binding = store.current();
        if (binding == null) {
            sender.sendMessage(Component.text("No hay ninguna palanca vinculada.", NamedTextColor.YELLOW));
            return Command.SINGLE_SUCCESS;
        }
        sender.sendMessage(Component.text(
                "Palanca vinculada en " + binding.describe() + ": " + status(binding), NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private static String status(LeverBinding binding) {
        World world = Bukkit.getWorld(binding.world());
        if (world == null) {
            return "el mundo no está cargado";
        }
        // Pedir el tipo de un bloque en un chunk descargado lo carga de forma síncrona.
        if (!world.isChunkLoaded(binding.x() >> 4, binding.z() >> 4)) {
            return "el chunk no está cargado";
        }
        return world.getBlockAt(binding.x(), binding.y(), binding.z()).getType() == Material.LEVER
                ? "la palanca sigue ahí"
                : "ya no hay una palanca ahí";
    }
}

package com.dario.controlpanel.listener;

import com.dario.controlpanel.difficulty.DifficultySwitch;
import com.dario.controlpanel.lever.BindingStore;
import com.dario.controlpanel.lever.LeverBinding;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;

import java.util.logging.Logger;

/**
 * Traduce los cambios de la palanca vinculada a encendido/apagado de la dificultad.
 */
public final class LeverListener implements Listener {
// Más grave (pitch < 1) suena más tétrico. MASTER: no depende del volumen de "criaturas".
    private static final Sound HARD_SOUND =
        Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.MASTER, 1f, 0.7f);
    private final BindingStore store;
    private final DifficultySwitch difficultySwitch;
    private final Logger logger;

    public LeverListener(BindingStore store, DifficultySwitch difficultySwitch, Logger logger) {
        this.store = store;
        this.difficultySwitch = difficultySwitch;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRedstoneChange(BlockRedstoneEvent event) {
        // Filtros de lo más barato a lo más caro.
        LeverBinding binding = store.current();
        if (binding == null) {
            return;
        }
        Block block = event.getBlock();
        if (!binding.matches(block) || block.getType() != Material.LEVER) {
            return;
        }

        // Si otro plugin vetó el movimiento, la corriente no cambia y no hay que tocar nada.
        boolean wasOn = event.getOldCurrent() > 0;
        boolean isOn = event.getNewCurrent() > 0;
        if (wasOn == isOn) {
            return;
        }

        if (difficultySwitch.apply(isOn)) {
            Difficulty target = difficultySwitch.target(isOn);
            logger.info("Palanca " + (isOn ? "encendida" : "apagada") + ": dificultad " + target.name());
            if (target == Difficulty.HARD) {
                Bukkit.getServer().playSound(HARD_SOUND);
            }
        }
    }
}

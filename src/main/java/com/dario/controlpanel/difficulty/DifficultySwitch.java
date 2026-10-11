package com.dario.controlpanel.difficulty;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;

/**
 * Aplica la dificultad de encendido o apagado a todos los mundos.
 */
public final class DifficultySwitch {

    private final Difficulty on;
    private final Difficulty off;

    public DifficultySwitch(Difficulty on, Difficulty off) {
        this.on = on;
        this.off = off;
    }

    public Difficulty target(boolean powered) {
        return powered ? on : off;
    }

    /**
     * Cambia solo los mundos que no están ya en el objetivo, así que llamarlo
     * dos veces seguidas no dispara eventos de más.
     *
     * @return true si algún mundo cambió de dificultad
     */
    public boolean apply(boolean powered) {
        Difficulty target = target(powered);
        boolean changed = false;
        for (World world : Bukkit.getWorlds()) {
            // En hardcore la dificultad siempre queda en HARD: intentar cambiarla
            // haría que apply() reportara un cambio que nunca ocurre.
            if (world.isHardcore() || world.getDifficulty() == target) {
                continue;
            }
            world.setDifficulty(target);
            changed = true;
        }
        return changed;
    }
}

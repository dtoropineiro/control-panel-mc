package com.dario.controlpanel.lever;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

/**
 * Posición de la palanca vinculada: identidad del mundo más tres enteros,
 * sin referencia a un World cargado.
 */
public record LeverBinding(NamespacedKey world, int x, int y, int z) {

    public static LeverBinding of(Block block) {
        return new LeverBinding(block.getWorld().getKey(), block.getX(), block.getY(), block.getZ());
    }

    public boolean matches(Block block) {
        // Los enteros primero: son lo más barato y casi siempre descartan.
        return block.getX() == x
                && block.getY() == y
                && block.getZ() == z
                && block.getWorld().getKey().equals(world);
    }

    public String describe() {
        return x + " " + y + " " + z + " (" + world.asString() + ")";
    }
}

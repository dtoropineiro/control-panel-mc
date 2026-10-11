package com.dario.controlpanel.lever;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Guarda la palanca vinculada en bindings.yml. Es estado que escribe el plugin,
 * por eso va aparte de config.yml.
 */
public final class BindingStore {

    private static final String PATH = "difficulty-lever";

    private final File file;
    private final Logger logger;
    // Única copia del estado: el YAML se reconstruye desde aquí en cada escritura.
    private @Nullable LeverBinding current;

    public BindingStore(File dataFolder, Logger logger) {
        this.file = new File(dataFolder, "bindings.yml");
        this.logger = logger;
    }

    /** Lee el archivo; si no existe o está incompleto, queda sin binding. */
    public void load() {
        current = null;

        ConfigurationSection section = YamlConfiguration.loadConfiguration(file).getConfigurationSection(PATH);
        if (section == null) {
            return;
        }
        String worldText = section.getString("world");
        NamespacedKey world = worldText == null ? null : NamespacedKey.fromString(worldText);
        if (world == null) {
            logger.warning("bindings.yml: mundo inválido '" + worldText + "', se ignora la palanca vinculada");
            return;
        }
        // getInt devuelve 0 si la clave falta o no es un entero: sin esto, una "y" perdida sería y=0.
        if (!section.isInt("x") || !section.isInt("y") || !section.isInt("z")) {
            logger.warning("bindings.yml: coordenadas inválidas, se ignora la palanca vinculada");
            return;
        }
        current = new LeverBinding(world, section.getInt("x"), section.getInt("y"), section.getInt("z"));
    }

    // Primero el disco, después la memoria: si la escritura falla, current queda como estaba.
    public void save(LeverBinding binding) throws IOException {
        write(binding);
        current = binding;
    }

    public void clear() throws IOException {
        write(null);
        current = null;
    }

    public @Nullable LeverBinding current() {
        return current;
    }

    private void write(@Nullable LeverBinding binding) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        if (binding != null) {
            ConfigurationSection section = yaml.createSection(PATH);
            section.set("world", binding.world().asString());
            section.set("x", binding.x());
            section.set("y", binding.y());
            section.set("z", binding.z());
        }
        yaml.save(file);
    }
}

package com.dario.controlpanel;

import com.dario.controlpanel.command.LeverCommand;
import com.dario.controlpanel.difficulty.DifficultySwitch;
import com.dario.controlpanel.lever.BindingStore;
import com.dario.controlpanel.listener.LeverListener;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Difficulty;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

/**
 * Clase principal de control-panel.
 */
public final class ControlPanel extends JavaPlugin {

    @Override
    public void onEnable() {
        // Copia config.yml solo si no existe; se valida una vez aquí.
        // Las claves no pueden ser on/off: YAML 1.1 las lee como true/false.
        saveDefaultConfig();
        Difficulty on = readDifficulty("difficulty.powered", Difficulty.HARD);
        Difficulty off = readDifficulty("difficulty.unpowered", Difficulty.NORMAL);
        DifficultySwitch difficultySwitch = new DifficultySwitch(on, off);

        BindingStore store = new BindingStore(getDataFolder(), getLogger());
        store.load();

        // Listeners de eventos
        getServer().getPluginManager().registerEvents(new LeverListener(store, difficultySwitch, getLogger()), this);

        // Comandos
        LeverCommand leverCommand = new LeverCommand(store, difficultySwitch, getLogger());
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> event.registrar().register(leverCommand.create()));

        getLogger().info("¡Plugin de Redstone activado correctamente!");
    }

    @Override
    public void onDisable() {
        // Libera aquí los recursos: tareas programadas, conexiones, etc.
    }

    private Difficulty readDifficulty(String path, Difficulty fallback) {
        String value = getConfig().getString(path, fallback.name());
        try {
            return Difficulty.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            getLogger().warning("config.yml: '" + value + "' no es una dificultad válida en " + path
                    + ", se usa " + fallback);
            return fallback;
        }
    }
}

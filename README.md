# control-panel

Plugin de [Paper](https://papermc.io) para Minecraft 26.2 (Java 25) que convierte una palanca en el
interruptor de la dificultad: encendida, todos los mundos pasan a **HARD**; apagada, a **NORMAL**.
Las dos dificultades se pueden cambiar en [`config.yml`](#configuración).

## Uso en el juego

### Vincular una palanca

Solo la palanca que vincules cambia la dificultad; las demás funcionan como siempre.

1. Coloca una palanca o elige una que ya exista.
2. Acércate a 5 bloques o menos y apunta con la mira a la palanca, no al bloque donde está pegada.
3. Ejecuta `/leverctl bind`.

El chat confirma la posición:

```text
Palanca vinculada en 120 64 -35 (minecraft:overworld)
```

En ese momento la dificultad se ajusta al estado actual de la palanca. Si en vez de eso ves
`Mira a una palanca a 5 bloques o menos.`, acércate más o apunta justo a la palanca.

Solo puede haber una palanca vinculada: si vinculas otra, reemplaza a la anterior. El vínculo se guarda y
sigue ahí después de reiniciar el servidor.

Necesitas ser op o tener el permiso `leverctl.admin`. Para darte op, escribe en la consola del servidor
`op <tu_nombre>`.

### Usar la palanca

Al mover la palanca vinculada, todos los mundos cargados (normalmente Overworld, Nether y End) cambian
de dificultad:

| Palanca | Dificultad por defecto | Se cambia en `config.yml` con |
|---|---|---|
| Encendida | `HARD` | `difficulty.powered` |
| Apagada | `NORMAL` | `difficulty.unpowered` |

Si la dificultad cambia, queda registrado en la consola del servidor:

```text
[control-panel] Palanca encendida: dificultad HARD
```

El permiso solo hace falta para los comandos: cualquier jugador que pueda usar la palanca cambia la dificultad.

Para comprobarlo, `/difficulty` sin argumentos muestra la dificultad actual. Para ver la de otra dimensión
sin viajar: `/execute in minecraft:the_nether run difficulty`.

### Comandos

| Comando | Qué hace |
|---|---|
| `/leverctl bind` | Vincula la palanca que estás mirando y ajusta la dificultad a su estado. Solo lo puede usar un jugador. |
| `/leverctl unbind` | Desvincula la palanca. La dificultad se queda como está. |
| `/leverctl info` | Muestra dónde está la palanca vinculada y si sigue ahí. |

Los tres piden el permiso `leverctl.admin`, que por defecto tienen los op; sin él, el comando ni siquiera
aparece. `unbind` e `info` también funcionan desde la consola. Si no hay nadie cerca de la palanca, `info`
responde `el chunk no está cargado`, porque no carga esa zona del mapa solo para comprobarla.

### A tener en cuenta

- La dificultad solo cambia al mover la palanca o al vincularla. Si alguien la cambia con `/difficulty`,
  se queda así hasta el próximo movimiento. Para sincronizarla otra vez, mira la palanca y repite `/leverctl bind`.
- Si rompes la palanca, la dificultad no cambia y el vínculo sigue guardado en esa posición: una palanca
  nueva en el mismo bloque funciona igual.
- En un mundo hardcore la dificultad siempre es HARD, así que el plugin no lo toca.
- Si usas `PEACEFUL`, al pasar a esa dificultad desaparecen los mobs hostiles.

## Configuración

El plugin guarda sus archivos en la carpeta `plugins/control-panel/` del servidor.

`config.yml` se crea la primera vez que arranca el plugin:

```yaml
difficulty:
  powered: HARD      # palanca encendida
  unpowered: NORMAL  # palanca apagada
```

Valores posibles: `PEACEFUL`, `EASY`, `NORMAL` y `HARD`. El archivo se lee al arrancar, así que reinicia el
servidor después de editarlo. Si un valor está mal escrito, la consola muestra un aviso y se usa el de este
ejemplo.

`bindings.yml` guarda la palanca vinculada. Lo escribe `/leverctl bind`; no hace falta editarlo.

## Desarrollo

### Abrir en IntelliJ IDEA

**File → Open…** y selecciona esta carpeta. IntelliJ detecta el proyecto Gradle y descarga las dependencias.

### Compilar

```bash
./gradlew build
```

En Windows: `.\gradlew build`. El plugin queda en `build/libs/`.

### Desplegar en el servidor de WSL

```bash
./gradlew deployLocal
```

Compila el plugin y copia el jar a la carpeta `plugins` del servidor de Paper en WSL
(`\\wsl.localhost\Ubuntu\home\dario\mc\server\plugins` desde Windows, `/home/dario/mc/server/plugins`
desde Linux). Antes borra los jars de versiones anteriores para que el servidor no cargue dos copias.
Reinicia el servidor para que cargue el jar nuevo.

Para usar otra carpeta: `./gradlew deployLocal -PpluginsDir=/ruta/a/plugins`.

### Probar con un servidor temporal

```bash
./gradlew runServer
```

Descarga Paper 26.2 y lo arranca en la carpeta `run/` con el plugin ya cargado.
La primera vez se detiene para que aceptes el [EULA de Minecraft](https://aka.ms/MinecraftEULA):
cambia `eula=false` por `eula=true` en `run/eula.txt` y vuelve a ejecutarlo.

Desde IntelliJ: pestaña **Gradle → Tasks → run paper → runServer** (con *Debug* puedes usar breakpoints).

### Estructura

| Archivo | Para qué |
|---|---|
| `build.gradle.kts` | Versión de Minecraft (`mcVersion`), versión de Java, dependencias y tarea `deployLocal` |
| `src/main/resources/plugin.yml` | Descriptor del plugin y permiso `leverctl.admin` |
| `src/main/resources/config.yml` | Configuración por defecto que se copia al servidor |
| `src/main/java/.../ControlPanel.java` | Clase principal: lee la configuración y registra el listener y el comando |
| `src/main/java/.../difficulty/` | `DifficultySwitch`: aplica la dificultad a todos los mundos |
| `src/main/java/.../lever/` | `LeverBinding` (posición de la palanca) y `BindingStore` (lee y escribe `bindings.yml`) |
| `src/main/java/.../listener/` | `LeverListener`: reacciona cuando se mueve la palanca vinculada |
| `src/main/java/.../command/` | `LeverCommand`: el comando `/leverctl` |

`JoinListener` y `ControlPanelCommand` (`/controlpanel`) son los ejemplos de la plantilla y no están registrados.

### Cambiar de versión de Minecraft

Cambia `mcVersion` en `build.gradle.kts` (y `JavaLanguageVersion` si la nueva versión pide otro Java).
El `api-version` de `plugin.yml` se calcula solo. Ojo: desde 26.1 la dependencia tiene el formato
`<versión>.build.+` (último build); hasta 1.21.11 era `<versión>-R0.1-SNAPSHOT`.

package net.countercraft.movecraft.util.teleport;

import net.countercraft.movecraft.Movecraft;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Code taken with permission from MicleBrick
 * https://www.spigotmc.org/threads/teleport-player-smoothly.317416/
 */
public class TeleportUtils {
    protected static @Nullable Object getHandle(Entity entity) {
        try {
            Method entity_getHandle = entity.getClass().getMethod("getHandle");
            return entity_getHandle.invoke(entity);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static @NotNull Field getField(@NotNull Class<?> clazz, String name) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    protected static @NotNull String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }


    private enum Mode {
        UNINITIALIZED,
        FALLBACK,
        SPIGOT_MAPPED,
        V1_17,
        V1_18,
        V1_19
    }

    private static Mode mode = Mode.UNINITIALIZED;

    private static void initialize() {
        int version = Integer.parseInt(getVersion().split("_")[1]);
        if (version < 17 && SpigotMappedTeleport.initialize()) {
            mode = Mode.SPIGOT_MAPPED;
        }
        else if (version <= 17 && V1_17Teleport.initialize()) {
            mode = Mode.V1_17;
        }
        else if (version <= 18 && V1_18Teleport.initialize()) {
            mode = Mode.V1_18;
        }
        else if (version <= 19 && V1_19Teleport.initialize()) {
            mode = Mode.V1_19;
        }
        else {
            Bukkit.getLogger().warning("Failed to access internal teleportation handle, switching to fallback");
            mode = Mode.FALLBACK;
        }
    }

    public static void teleport(Player player, @NotNull Location location, float yawChange, float pitchChange) {
        if (mode == Mode.UNINITIALIZED)
            initialize();

        switch (mode) {
            case SPIGOT_MAPPED:
                SpigotMappedTeleport.teleport(player, location, yawChange, pitchChange);
                break;
            case V1_17:
                V1_17Teleport.teleport(player, location, yawChange, pitchChange);
                break;
            case V1_18:
                V1_18Teleport.teleport(player, location, yawChange, pitchChange);
                break;
            case V1_19:
                V1_19Teleport.teleport(player, location, yawChange, pitchChange);
                break;
            case FALLBACK:
                Movecraft.getInstance().getWorldHandler().addPlayerLocation(player,
                        location.getX() - player.getLocation().getX(),
                        location.getY() - player.getLocation().getY(),
                        location.getZ() - player.getLocation().getZ(),
                        yawChange, pitchChange
                );
                break;
            default:
                throw new IllegalStateException("Unknown mode: " + mode);
        }
    }
}

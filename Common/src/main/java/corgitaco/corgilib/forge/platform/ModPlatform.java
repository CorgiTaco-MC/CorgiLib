package corgitaco.corgilib.forge.platform;

import corgitaco.corgilib.CorgiLib;

import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;

public interface ModPlatform {
    ModPlatform PLATFORM = load(ModPlatform.class);

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();


    Collection<String> getModIDS();

    Path configDir();

    default Path modConfigDir() {
        return configDir().resolve(CorgiLib.MOD_ID);
    }

    static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        CorgiLib.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}

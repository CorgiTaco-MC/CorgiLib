package corgitaco.corgilib.fabric;

import com.electronwill.nightconfig.core.file.FileWatcher;
import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.fabric.network.FabricNetworkHandler;
import corgitaco.corgilib.server.commands.CorgiLibCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.util.Objects;

public class CorgiLibFabric implements ModInitializer {

    private static String firstInitialized = null;

    @Override
    public void onInitialize() {
        initializeCorgiLib("Corgi Lib Fabric Mod Initializer");
    }

    public static void initializeCorgiLib(String initializedFrom) {
        Objects.requireNonNull(initializedFrom, "BWG must be told where it was initialized from.");
        if (firstInitialized != null) {
            CorgiLib.LOGGER.debug("Attempted to Initialize Oh The Biomes We've Gone (BWG) from \"{}\" but BWG already was initialized from \"{}\", this should not be a problem.", initializedFrom, firstInitialized);
            return;
        }
        firstInitialized = initializedFrom;
        CorgiLib.init();
        FabricNetworkHandler.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CorgiLibCommands.registerCommands(dispatcher, registryAccess));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> FileWatcher.defaultInstance().stop());
    }
}

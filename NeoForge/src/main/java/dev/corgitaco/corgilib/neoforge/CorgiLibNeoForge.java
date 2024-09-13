package dev.corgitaco.corgilib.neoforge;

import corgitaco.corgilib.CorgiLib;
import dev.corgitaco.corgilib.neoforge.platform.NeoForgePlatform;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

/**
 * Main class for the mod on the NeoForge platform.
 */
@Mod(CorgiLib.MOD_ID)
public class CorgiLibNeoForge {
    public CorgiLibNeoForge(IEventBus modEventBus) {
        CorgiLib.init();
        NeoForgePlatform.CACHED.values().forEach(deferredRegister -> deferredRegister.register(modEventBus));
        modEventBus.addListener(DataPackRegistryEvent.NewRegistry.class, newRegistry -> NeoForgePlatform.DATAPACK_REGISTRIES.forEach(newRegistryConsumer -> newRegistryConsumer.accept(newRegistry)));
        modEventBus.addListener(NewRegistryEvent.class, newRegistry -> NeoForgePlatform.NEW_REGISTRIES.forEach(newRegistryConsumer -> newRegistryConsumer.accept(newRegistry)));
    }
}

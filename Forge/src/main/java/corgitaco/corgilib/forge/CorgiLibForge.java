package corgitaco.corgilib.forge;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.forge.network.ForgeNetworkHandler;
import corgitaco.corgilib.forge.platform.ForgePlatform;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;

@Mod(CorgiLib.MOD_ID)
public class CorgiLibForge {

    public CorgiLibForge(final FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        CorgiLib.init();
        ForgePlatform.CACHED.values().forEach(deferredRegister -> deferredRegister.register(modEventBus));
        modEventBus.<DataPackRegistryEvent.NewRegistry>addListener(newRegistry -> ForgePlatform.DATAPACK_REGISTRIES.forEach(newRegistryConsumer -> newRegistryConsumer.accept(newRegistry)));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ForgeNetworkHandler.init();
    }
}
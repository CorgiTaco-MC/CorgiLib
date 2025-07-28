package corgitaco.corgilib.forge;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.forge.network.ForgeNetworkHandler;
import corgitaco.corgilib.forge.platform.ForgePlatform;
import corgitaco.corgilib.server.commands.CorgiLibCommands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;

@Mod(CorgiLib.MOD_ID)
public class CorgiLibForge {

    public CorgiLibForge(final FMLJavaModLoadingContext context) {
        BusGroup modEventBus = context.getModBusGroup();
        FMLCommonSetupEvent.getBus(modEventBus).addListener(fmlCommonSetupEvent -> ForgeNetworkHandler.init());
        CorgiLib.init();
        ForgePlatform.CACHED.values().forEach(deferredRegister -> deferredRegister.register(modEventBus));
        DataPackRegistryEvent.NewRegistry.getBus(modEventBus).addListener(newRegistry -> ForgePlatform.DATAPACK_REGISTRIES.forEach(newRegistryConsumer -> newRegistryConsumer.accept(newRegistry)));
        RegisterCommandsEvent.BUS.addListener(commandBus -> CorgiLibCommands.registerCommands(commandBus.getDispatcher(), commandBus.getBuildContext()));
    }
}
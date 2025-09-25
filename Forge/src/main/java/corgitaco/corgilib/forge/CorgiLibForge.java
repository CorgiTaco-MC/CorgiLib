package corgitaco.corgilib.forge;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.forge.network.ForgeNetworkHandler;
import corgitaco.corgilib.forge.platform.ForgePlatform;
import corgitaco.corgilib.server.commands.CorgiLibCommands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CorgiLib.MOD_ID)
public class CorgiLibForge {

    public CorgiLibForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        CorgiLib.init();
        ForgePlatform.CACHED.values().forEach(deferredRegister -> deferredRegister.register(modEventBus));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ForgeNetworkHandler.init();
    }

    private void registerCommands(final RegisterCommandsEvent event) {
        CorgiLibCommands.registerCommands(event.getDispatcher(), event.getBuildContext());
    }
}
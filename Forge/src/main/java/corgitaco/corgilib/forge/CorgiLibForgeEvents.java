package corgitaco.corgilib.forge;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.server.commands.CorgiLibCommands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CorgiLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CorgiLibForgeEvents {


    @SubscribeEvent
    public static void corgilib$registerCommands(RegisterCommandsEvent registerCommandsEvent) {
        CorgiLibCommands.registerCommands(registerCommandsEvent.getDispatcher(), registerCommandsEvent.getBuildContext());
    }
}

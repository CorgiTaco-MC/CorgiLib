package corgitaco.corgilib.forge.client;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.client.AnnouncementInfoClientTicker;
import corgitaco.corgilib.client.commands.CorgiLibClientCommands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CorgiLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CorgiLibForgeClientEvents {

/*
    @SubscribeEvent
    public static void screenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (AnnouncementInfo.getInstance() != null && AnnouncementConfig.INSTANCE.get().announcementDelivery() == AnnouncementConfig.AnnouncementDelivery.WIDGET) {
            screen.addRenderableWidget(new AnnouncementWidget(screen.width, screen.height, 25, 25, Component.literal("")));
        }
    }

 */

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent.Pre event) {
        AnnouncementInfoClientTicker.checkedAnnouncementTicker(event.player());
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        CorgiLibClientCommands.registerClientCommands(event.getDispatcher());
    }
}

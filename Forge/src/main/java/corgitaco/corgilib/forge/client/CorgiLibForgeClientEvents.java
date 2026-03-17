package corgitaco.corgilib.forge.client;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.client.AnnouncementInfoClientTicker;
import corgitaco.corgilib.client.commands.CorgiLibClientCommands;
import corgitaco.corgilib.client.screen.widget.EmergencyAnnouncementScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CorgiLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CorgiLibForgeClientEvents {


    @SubscribeEvent
    public static void screenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        EmergencyAnnouncementScreen.createAlertWidget(screen, screen.width, screen.height);
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        AnnouncementInfoClientTicker.checkedAnnouncementTicker(event.player);
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        CorgiLibClientCommands.registerClientCommands(event.getDispatcher());
    }
}

package dev.corgitaco.corgilib.neoforge.client;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.client.AnnouncementInfoClientTicker;
import corgitaco.corgilib.client.commands.CorgiLibClientCommands;
import corgitaco.corgilib.client.screen.widget.EmergencyAnnouncementScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = CorgiLib.MOD_ID, value = Dist.CLIENT)
public final class CorgiLibNeoForgeClientEvents {
    @SubscribeEvent
    public static void screenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        EmergencyAnnouncementScreen.createAlertWidget(screen, screen.width, screen.height);
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Pre event) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            AnnouncementInfoClientTicker.checkedAnnouncementTicker(player);
        }
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        CorgiLibClientCommands.registerClientCommands(event.getDispatcher());
    }
}

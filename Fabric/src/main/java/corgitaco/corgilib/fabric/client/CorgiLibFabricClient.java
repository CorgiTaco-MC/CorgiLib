package corgitaco.corgilib.fabric.client;

import corgitaco.corgilib.client.AnnouncementInfoClientTicker;
import corgitaco.corgilib.client.commands.CorgiLibClientCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;

public class CorgiLibFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ClientCommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext) -> CorgiLibClientCommands.registerClientCommands(commandDispatcher));

        ClientTickEvents.START_CLIENT_TICK.register(minecraft -> {
            LocalPlayer player = minecraft.player;
            if (player != null) {
                AnnouncementInfoClientTicker.checkedAnnouncementTicker(player);
            }
        });
/*
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (AnnouncementInfo.getInstance() != null && AnnouncementConfig.INSTANCE.get().announcementDelivery() == AnnouncementConfig.AnnouncementDelivery.WIDGET) {
                screen.addRenderableWidget(new AnnouncementWidget(scaledWidth, scaledHeight, 25, 25, Component.literal("")));
            }
        });

 */
    }
}

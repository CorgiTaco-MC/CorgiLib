package dev.corgitaco.corgilib.neoforge.client;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.network.Packet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(modid = CorgiLib.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientNetworkHandler {
    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        Packet.PACKETS.forEach(handler -> register(handler, event));
    }

    private static <T extends Packet> void register(Packet.Handler<T> handler, RegisterClientPayloadHandlersEvent event) {
        if (handler.direction() != Packet.PacketDirection.CLIENT_TO_SERVER) {
            event.register(handler.type(), (packet, context) -> handler.handle().handle(packet, context.player().level(), context.player()));
        }
    }
}

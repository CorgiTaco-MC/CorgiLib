package dev.corgitaco.corgilib.neoforge.network;

import corgitaco.corgilib.network.Packet;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NeoForgeNetworkHandler {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        Packet.PACKETS.forEach(handler -> register(handler, registrar));
    }

    private static <T extends Packet> void register(Packet.Handler<T> handler, PayloadRegistrar registrar) {
        if (handler.direction() == Packet.PacketDirection.SERVER_TO_CLIENT) {
            registrar.playToClient(handler.type(), handler.serializer(), (arg, iPayloadContext) -> arg.handle(iPayloadContext.player().level(), iPayloadContext.player()));
        }

        if (handler.direction() == Packet.PacketDirection.CLIENT_TO_SERVER) {
            registrar.playToServer(handler.type(), handler.serializer(), (arg, iPayloadContext) -> arg.handle(iPayloadContext.player().level(), iPayloadContext.player()));
        }

        if (handler.direction() == Packet.PacketDirection.BI_DIRECTIONAL) {
            registrar.playBidirectional(handler.type(), handler.serializer(), (arg, iPayloadContext) -> arg.handle(iPayloadContext.player().level(), iPayloadContext.player()));
        }
    }
}
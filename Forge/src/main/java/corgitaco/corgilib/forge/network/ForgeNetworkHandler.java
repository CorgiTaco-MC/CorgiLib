package corgitaco.corgilib.forge.network;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.network.Packet;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.payload.PayloadConnection;
import net.minecraftforge.network.payload.PayloadFlow;
import net.minecraftforge.network.payload.PayloadProtocol;

import java.util.List;

public class ForgeNetworkHandler {
    public static final Channel<CustomPacketPayload> SIMPLE_CHANNEL = Util.make(() -> {
        PayloadConnection<CustomPacketPayload> connection = ChannelBuilder.named(CorgiLib.createLocation("network")).payloadChannel();

        PayloadProtocol<RegistryFriendlyByteBuf, CustomPacketPayload> play = connection.play();

        Packet.PACKETS.forEach(packet -> register(packet, play));
        return play.bidirectional().build();
    });

    public static void init() {}


    private static <T extends Packet> void register(Packet.Handler<T> handler, PayloadProtocol<RegistryFriendlyByteBuf, CustomPacketPayload> protocol) {
        PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> flow = switch (handler.direction()) {
            case SERVER_TO_CLIENT -> protocol.clientbound();
            case CLIENT_TO_SERVER -> protocol.serverbound();
            case BI_DIRECTIONAL -> protocol.bidirectional();
        };
        flow.addMain(handler.type(), handler.serializer(), (t, context) -> {
            if (context.isClientSide()) {
                Client.clientHandle(t, handler.handle());
            } else {
                ServerPlayer sender = context.getSender();
                if (sender != null) {
                    handler.handle().handle(t, sender.level(), sender);
                }
            }
        });
    }

    public static <T extends Packet> void sendToPlayer(ServerPlayer playerEntity, T packet) {
        SIMPLE_CHANNEL.send(packet, playerEntity.connection.getConnection());
    }

    public static <T extends Packet> void sendToAllPlayers(List<ServerPlayer> playerEntities, T packet) {
        for (ServerPlayer playerEntity : playerEntities) {
            SIMPLE_CHANNEL.send(packet, playerEntity.connection.getConnection());
        }
    }

    public static <T extends Packet> void sendToServer(T packet) {
        Client.sendToServer(packet);
    }

    private static class Client {
        private static <T extends Packet> void sendToServer(T packet) {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) {
                throw new IllegalStateException("Cannot send a CorgiLib packet without a server connection");
            }
            SIMPLE_CHANNEL.send(packet, connection.getConnection());
        }

        private static <T extends Packet> void clientHandle(T packet, Packet.Handle<T> handle) {
            handle.handle(packet, Minecraft.getInstance().level, Minecraft.getInstance().player);
        }
    }
}
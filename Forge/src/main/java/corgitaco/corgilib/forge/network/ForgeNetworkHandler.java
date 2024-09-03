package corgitaco.corgilib.forge.network;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.network.Packet;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
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

        for (Packet.Handler<?> packet : Packet.PACKETS) {

            if (packet.direction() == Packet.PacketDirection.SERVER_TO_CLIENT) {
                play = play.clientbound();
                registerS2C(packet, (PayloadFlow) play);
            }

            if (packet.direction() == Packet.PacketDirection.CLIENT_TO_SERVER) {
                play = play.serverbound();
                registerC2S(packet, (PayloadFlow) play);
            }

            if (packet.direction() == Packet.PacketDirection.BI_DIRECTIONAL) {
                play = play.bidirectional();
                registerBiDirectional(packet, (PayloadFlow) play);
            }
        }

        return ((PayloadFlow) play).build();
    });

    public static void init() {}


    private static <T extends Packet> void registerS2C(Packet.Handler<T> handler, PayloadFlow<RegistryFriendlyByteBuf, T> flow) {
        flow.addMain(handler.type(), handler.serializer(), (t, context) -> {
            context.enqueueWork(() -> handler.handle().handle(t, context.getSender().level(), context.getSender()));
            context.setPacketHandled(true);
        });
    }

    private static <T extends Packet> void registerC2S(Packet.Handler<T> handler, PayloadFlow<RegistryFriendlyByteBuf, T> flow) {
        flow.addMain(handler.type(), handler.serializer(), (t, context) -> {
            context.enqueueWork(() -> Client.clientHandle(t, handler.handle()));
            context.setPacketHandled(true);
        });
    }

    private static <T extends Packet> void registerBiDirectional(Packet.Handler<T> handler, PayloadFlow<RegistryFriendlyByteBuf, T> flow) {
        flow.addMain(handler.type(), handler.serializer(), (t, context) -> {
            if (FMLLoader.getDist() == Dist.CLIENT) {
                if (context.isClientSide()) {
                    Client.clientHandle(t, handler.handle());
                }
            } else {
                handler.handle().handle(t, context.getSender().level(), context.getSender());
            }
            context.setPacketHandled(true);
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
        SIMPLE_CHANNEL.send(packet, Minecraft.getInstance().getConnection().getConnection());
    }

    private static class Client {
        private static <T extends Packet> void clientHandle(T packet, Packet.Handle<T> handle) {
            handle.handle(packet, Minecraft.getInstance().level, Minecraft.getInstance().player);
        }
    }
}
package corgitaco.corgilib.fabric.network;

import corgitaco.corgilib.network.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class FabricNetworkHandler {

    public static void init() {
        Packet.PACKETS.forEach(FabricNetworkHandler::register);
    }

    private static <T extends Packet> void register(Packet.Handler<T> handler) {
        if (handler.direction() == Packet.PacketDirection.SERVER_TO_CLIENT) {
            PayloadTypeRegistry.clientboundPlay().register(handler.type(), handler.serializer());
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientProxy.registerClientReceiver(handler);
            }
        }

        if (handler.direction() == Packet.PacketDirection.CLIENT_TO_SERVER) {
            PayloadTypeRegistry.serverboundPlay().register(handler.type(), handler.serializer());
            ServerProxy.registerServerReceiver(handler);
        }

        if (handler.direction() == Packet.PacketDirection.BI_DIRECTIONAL) {
            PayloadTypeRegistry.serverboundPlay().register(handler.type(), handler.serializer());
            PayloadTypeRegistry.clientboundPlay().register(handler.type(), handler.serializer());
            ServerProxy.registerServerReceiver(handler);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientProxy.registerClientReceiver(handler);
            }
        }

    }


    public static <MSG extends Packet> void sendToPlayer(ServerPlayer player, MSG packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static <MSG extends Packet> void sendToAllPlayers(List<ServerPlayer> players, MSG packet) {
        players.forEach(player -> sendToPlayer(player, packet));
    }

    public static <MSG extends Packet> void sendToServer(MSG packet) {
        ClientProxy.sendToServer(packet);
    }

    public record ClientProxy() {

        private static <T extends Packet> void sendToServer(T packet) {
            ClientPlayNetworking.send(packet);
        }

        public static <T extends Packet> void registerClientReceiver(Packet.Handler<T> handler) {
            ClientPlayNetworking.registerGlobalReceiver(handler.type(), (t, context) -> handler.handle().handle(t, context.client().level, context.player()));
        }
    }

    public static class ServerProxy {
        private static <T extends Packet> void registerServerReceiver(Packet.Handler<T> handler) {
            ServerPlayNetworking.registerGlobalReceiver(handler.type(), (t, context) -> handler.handle().handle(t, context.player().level(), context.player()));
        }
    }
}
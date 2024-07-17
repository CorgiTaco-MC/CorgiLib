package corgitaco.corgilib.fabric.network;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.network.Packet;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricNetworkHandler {

    private static final String PACKET_LOCATION = CorgiLib.MOD_ID;
    private static final Function<String, ResourceLocation> PACKET_ID = id -> {
        ResourceLocation value = new ResourceLocation(id);

        return value.getNamespace().equals("minecraft") ? CorgiLib.createLocation(id) : value;
    };

    private static final Map<Class<? extends Packet>, BiConsumer<?, FriendlyByteBuf>> ENCODERS = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Packet>, ResourceLocation> PACKET_IDS = new ConcurrentHashMap<>();

    public static void init() {
        Packet.PACKETS.forEach(FabricNetworkHandler::register);
    }

    private static <T extends Packet> void register(String path, Packet.Handler<T> handler) {
        registerMessage(path, handler.clazz(), handler.write(), handler.read(), handler.handle());
    }

    private static <T extends Packet> void registerMessage(String id, Class<T> clazz,
                                                           BiConsumer<T, FriendlyByteBuf> encode,
                                                           Function<FriendlyByteBuf, T> decode,
                                                           Packet.Handle<T> handler) {
        ENCODERS.put(clazz, encode);
        PACKET_IDS.put(clazz, PACKET_ID.apply(id));


        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.registerClientReceiver(id, decode, handler);
        }
        ServerProxy.registerServerReceiver(id, decode, handler);

    }


    public static <MSG extends Packet> void sendToPlayer(ServerPlayer player, MSG packet) {
        ResourceLocation packetId = PACKET_IDS.get(packet.getClass());
        @SuppressWarnings("unchecked")
        BiConsumer<MSG, FriendlyByteBuf> encoder = (BiConsumer<MSG, FriendlyByteBuf>) ENCODERS.get(packet.getClass());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        encoder.accept(packet, buf);
        ServerPlayNetworking.send(player, packetId, buf);
    }

    public static <MSG extends Packet> void sendToAllPlayers(List<ServerPlayer> players, MSG packet) {
        players.forEach(player -> sendToPlayer(player, packet));
    }

    public static <MSG extends Packet> void sendToServer(MSG packet) {
        ResourceLocation packetId = PACKET_IDS.get(packet.getClass());
        @SuppressWarnings("unchecked")
        BiConsumer<MSG, FriendlyByteBuf> encoder = (BiConsumer<MSG, FriendlyByteBuf>) ENCODERS.get(packet.getClass());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        encoder.accept(packet, buf);
        ClientPlayNetworking.send(packetId, buf);
    }

    public record ClientProxy() {

        public static <T extends Packet> void registerClientReceiver(String id, Function<FriendlyByteBuf, T> decode,
                                                                     Packet.Handle<T> handler) {
            ClientPlayNetworking.registerGlobalReceiver(PACKET_ID.apply(id), (client, listener, buf, responseSender) -> {
                buf.retain();
                client.execute(() -> {
                    T packet = decode.apply(buf);
                    ClientLevel level = client.level;
                    if (level != null) {
                        try {
                            handler.handle(packet, level, Minecraft.getInstance().player);
                        } catch (Throwable throwable) {
                            CorgiLib.LOGGER.error("Packet failed: ", throwable);
                            throw throwable;
                        }
                    }
                    buf.release();
                });
            });
        }
    }

    public static class ServerProxy {
        private static <T extends Packet> void registerServerReceiver(String id, Function<FriendlyByteBuf, T> decode, Packet.Handle<T> handler) {
            ServerPlayNetworking.registerGlobalReceiver(PACKET_ID.apply(id), (server, player, handler1, buf, responseSender) -> {
                buf.retain();
                server.execute(() -> {
                    T packet = decode.apply(buf);
                    Level level = player.level();
                    try {
                        handler.handle(packet, level, player);
                    } catch (Throwable throwable) {
                        CorgiLib.LOGGER.error("Packet failed: ", throwable);
                        throw throwable;
                    }
                    buf.release();
                });
            });
        }
    }
}
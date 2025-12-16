package corgitaco.corgilib.network;

import corgitaco.corgilib.CorgiLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;


import java.util.ArrayList;
import java.util.List;

public interface Packet extends CustomPacketPayload {
    List<Handler<?>> PACKETS = Util.make(new ArrayList<>(), list -> {
        CorgiLib.LOGGER.info("Initializing network...");
        list.add(new Handler<>(EntityIsInsideStructureTrackerUpdatePacket.TYPE, PacketDirection.SERVER_TO_CLIENT, EntityIsInsideStructureTrackerUpdatePacket.CODEC, EntityIsInsideStructureTrackerUpdatePacket::handle));
        list.add(new Handler<>(UpdateStructureBoxPacketC2S.TYPE, PacketDirection.CLIENT_TO_SERVER, UpdateStructureBoxPacketC2S.CODEC, UpdateStructureBoxPacketC2S::handle));
        CorgiLib.LOGGER.info("Initialized network!");
    });


    void handle(@Nullable Level level, @Nullable Player player);


    record Handler<T extends Packet>(Type<T> type, PacketDirection direction,
                                     StreamCodec<RegistryFriendlyByteBuf, T> serializer, Handle<T> handle) {
    }

    enum PacketDirection {
        SERVER_TO_CLIENT,
        CLIENT_TO_SERVER,
        BI_DIRECTIONAL
    }

    @FunctionalInterface
    interface Handle<T extends Packet> {
        void handle(T packet, Level level, Player player);
    }
}

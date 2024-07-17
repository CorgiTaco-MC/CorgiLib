package corgitaco.corgilib.network;

import corgitaco.corgilib.CorgiLib;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Packet {
    Map<String, Handler<?>> PACKETS = Util.make(new HashMap<>(), map -> {
        CorgiLib.LOGGER.info("Initializing network...");
        map.put(CorgiLib.createLocation("is_entity_inside_structure").toString(), new Handler<>(EntityIsInsideStructureTrackerUpdatePacket.class, PacketDirection.SERVER_TO_CLIENT, EntityIsInsideStructureTrackerUpdatePacket::write, EntityIsInsideStructureTrackerUpdatePacket::readFromPacket, EntityIsInsideStructureTrackerUpdatePacket::handle));
        map.put(CorgiLib.createLocation("update_structure").toString(), new Handler<>(UpdateStructureBoxPacketC2S.class, PacketDirection.CLIENT_TO_SERVER, UpdateStructureBoxPacketC2S::write, UpdateStructureBoxPacketC2S::readFromPacket, UpdateStructureBoxPacketC2S::handle));
        CorgiLib.LOGGER.info("Initialized network!");
    });


    void write(FriendlyByteBuf buf);

    void handle(@Nullable Level level, @Nullable Player player);

    record Handler<T extends Packet>(Class<T> clazz, PacketDirection direction, BiConsumer<T, FriendlyByteBuf> write,
                                     Function<FriendlyByteBuf, T> read,
                                     Handle<T> handle) {
    }

    enum PacketDirection {
        SERVER_TO_CLIENT,
        CLIENT_TO_SERVER
    }

    @FunctionalInterface
    interface Handle<T extends Packet> {
        void handle(T packet, Level level, Player player);
    }
}

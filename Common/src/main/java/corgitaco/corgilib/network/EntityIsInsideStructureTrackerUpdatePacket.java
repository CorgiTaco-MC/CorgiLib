package corgitaco.corgilib.network;


import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.entity.IsInsideStructureTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record EntityIsInsideStructureTrackerUpdatePacket(
        int id,
        IsInsideStructureTracker.IsInside isInside
) implements Packet {

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityIsInsideStructureTrackerUpdatePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EntityIsInsideStructureTrackerUpdatePacket::id,
            ByteBufCodecs.fromCodec(IsInsideStructureTracker.IsInside.CODEC), EntityIsInsideStructureTrackerUpdatePacket::isInside,
            EntityIsInsideStructureTrackerUpdatePacket::new
    );

    public static final CustomPacketPayload.Type<EntityIsInsideStructureTrackerUpdatePacket> TYPE = new CustomPacketPayload.Type<>(CorgiLib.createLocation("is_entity_inside_structure"));

    @Override
    public void handle(@Nullable Level level, @Nullable Player player) {
        if (level != null) {
            final Entity entity = level.getEntity(this.id);
            if (entity != null) {
                IsInsideStructureTracker.IsInside tracker = ((IsInsideStructureTracker.Access) entity).getIsInsideStructureTracker().getTracker();
                tracker.setInsideStructure(this.isInside.isInsideStructure());
                tracker.setInsideStructurePiece(this.isInside.isInsideStructurePiece());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
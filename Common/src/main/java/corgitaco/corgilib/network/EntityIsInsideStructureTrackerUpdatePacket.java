package corgitaco.corgilib.network;


import corgitaco.corgilib.entity.IsInsideStructureTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class EntityIsInsideStructureTrackerUpdatePacket implements Packet {

    private final int id;
    private final IsInsideStructureTracker.IsInside isInside;

    public EntityIsInsideStructureTrackerUpdatePacket(int id, IsInsideStructureTracker.IsInside isInside) {
        this.id = id;
        this.isInside = isInside;
    }


    public static EntityIsInsideStructureTrackerUpdatePacket readFromPacket(FriendlyByteBuf buf) {
        return new EntityIsInsideStructureTrackerUpdatePacket(buf.readVarInt(), buf.readJsonWithCodec(IsInsideStructureTracker.IsInside.CODEC));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.id);
        buf.writeJsonWithCodec(IsInsideStructureTracker.IsInside.CODEC, this.isInside);
    }

    @Override
    public void handle(@Nullable Level level, @Nullable Player player) {

        ClientLevel world = Minecraft.getInstance().level;
        if (world != null) {
            final Entity entity = world.getEntity(this.id);
            if (entity != null) {
                IsInsideStructureTracker.IsInside tracker = ((IsInsideStructureTracker.Access) entity).getIsInsideStructureTracker().getTracker();
                tracker.setInsideStructure(this.isInside.isInsideStructure());
                tracker.setInsideStructurePiece(this.isInside.isInsideStructurePiece());
            }
        }
    }
}
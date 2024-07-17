package corgitaco.corgilib.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.network.EntityIsInsideStructureTrackerUpdatePacket;
import corgitaco.corgilib.platform.PlatformNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class IsInsideStructureTracker {

    private IsInside tracker = new IsInside(false, false);

    public void setInside(Level world, Entity entity, IsInside isInside) {
        this.tracker = isInside;
        if (!world.isClientSide) {
            PlatformNetwork.NETWORK.sendToAllClients(((ServerLevel) world).players(), new EntityIsInsideStructureTrackerUpdatePacket(entity.getId(), isInside));
        }
    }

    public IsInside getTracker() {
        return tracker;
    }

    public static class IsInside {

        public static final Codec<IsInside> CODEC = RecordCodecBuilder.create(builder -> builder.group(Codec.BOOL.fieldOf("insideStructure").forGetter(isInside -> isInside.insideStructure),
                Codec.BOOL.fieldOf("insideStructurePiece").forGetter(isInside -> isInside.insideStructure)
        ).apply(builder, IsInside::new));

        private boolean insideStructure;
        private boolean insideStructurePiece;

        public IsInside(boolean insideStructure, boolean insideStructurePiece) {
            this.insideStructure = insideStructure;
            this.insideStructurePiece = insideStructurePiece;
        }

        public boolean isInsideStructure() {
            return insideStructure;
        }

        public boolean isInsideStructurePiece() {
            return insideStructurePiece;
        }

        public IsInside setInsideStructure(boolean insideStructure) {
            this.insideStructure = insideStructure;
            return this;
        }

        public IsInside setInsideStructurePiece(boolean insideStructurePiece) {
            this.insideStructurePiece = insideStructurePiece;
            return this;
        }
    }

    public interface Access {
        IsInsideStructureTracker getIsInsideStructureTracker();
    }
}

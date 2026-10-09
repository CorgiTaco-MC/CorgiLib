package corgitaco.corgilib.network;

import corgitaco.corgilib.CorgiLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record UpdateStructureBoxPacketC2S(
        BlockPos pos,
        BlockPos structureOffset,
        BoundingBox box
) implements Packet {

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateStructureBoxPacketC2S> CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BlockPos.CODEC), UpdateStructureBoxPacketC2S::pos,
            ByteBufCodecs.fromCodec(BlockPos.CODEC), UpdateStructureBoxPacketC2S::structureOffset,
            ByteBufCodecs.fromCodec(BoundingBox.CODEC), UpdateStructureBoxPacketC2S::box,
            UpdateStructureBoxPacketC2S::new
    );

    public static final CustomPacketPayload.Type<UpdateStructureBoxPacketC2S> TYPE = new CustomPacketPayload.Type<>(CorgiLib.createLocation("update_structure"));


    @Override
    public void handle(@Nullable Level level, @Nullable Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)
                || serverPlayer.level() != serverLevel || !serverPlayer.canUseGameMasterBlocks()
                || !serverLevel.isLoaded(this.pos) || !validOffset(this.structureOffset)) {
            return;
        }

        double maxDistance = Math.sqrt(3.0) * (StructureBlockEntity.MAX_OFFSET_PER_AXIS + StructureBlockEntity.MAX_SIZE_PER_AXIS)
                + serverPlayer.blockInteractionRange();
        if (serverPlayer.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.pos)) > maxDistance * maxDistance) {
            return;
        }

        long sizeX = (long) this.box.maxX() - this.box.minX() + 1;
        long sizeY = (long) this.box.maxY() - this.box.minY() + 1;
        long sizeZ = (long) this.box.maxZ() - this.box.minZ() + 1;
        if (!validSize(sizeX) || !validSize(sizeY) || !validSize(sizeZ)) {
            return;
        }

        BlockEntity blockEntity = serverLevel.getBlockEntity(this.pos);
        if (blockEntity instanceof StructureBlockEntity structureBlockEntity && structureBlockEntity.getMode() == StructureMode.SAVE) {
            structureBlockEntity.setStructurePos(this.structureOffset);
            structureBlockEntity.setStructureSize(new Vec3i((int) sizeX, (int) sizeY, (int) sizeZ));
            structureBlockEntity.setChanged();
            BlockState blockState = serverLevel.getBlockState(this.pos);
            serverLevel.sendBlockUpdated(this.pos, blockState, blockState, 3);
        }
    }

    private static boolean validOffset(BlockPos offset) {
        int limit = StructureBlockEntity.MAX_OFFSET_PER_AXIS;
        return offset.getX() >= -limit && offset.getX() <= limit
                && offset.getY() >= -limit && offset.getY() <= limit
                && offset.getZ() >= -limit && offset.getZ() <= limit;
    }

    private static boolean validSize(long size) {
        return size >= 1 && size <= StructureBlockEntity.MAX_SIZE_PER_AXIS;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
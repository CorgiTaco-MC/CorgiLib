package corgitaco.corgilib.network;

import corgitaco.corgilib.CorgiLib;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

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
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(this.pos);

            if (blockEntity instanceof StructureBlockEntity structureBlockEntity) {
                structureBlockEntity.setStructurePos(new BlockPos(structureOffset.getX(), structureOffset.getY(), structureOffset.getZ()));
                structureBlockEntity.setStructureSize(box.getLength());
                structureBlockEntity.setChanged();
                BlockState blockState = level.getBlockState(pos);
                level.sendBlockUpdated(pos, blockState, blockState, 3);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }
}
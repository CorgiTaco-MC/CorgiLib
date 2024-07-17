package corgitaco.corgilib.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public record UpdateStructureBoxPacketC2S(BlockPos pos, BlockPos structureOffset, BoundingBox box) implements Packet {

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(structureOffset.getX());
        buf.writeInt(structureOffset.getY());
        buf.writeInt(structureOffset.getZ());

        buf.writeInt(box.minX());
        buf.writeInt(box.minY());
        buf.writeInt(box.minZ());
        buf.writeInt(box.maxX());
        buf.writeInt(box.maxY());
        buf.writeInt(box.maxZ());
    }

    public static UpdateStructureBoxPacketC2S readFromPacket(FriendlyByteBuf buf) {
        return new UpdateStructureBoxPacketC2S(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()), new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()), new BoundingBox(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt()));
    }

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
}
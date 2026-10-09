package corgitaco.corgilib.mixin.chunk;

import corgitaco.corgilib.world.level.RandomTickScheduler;
import corgitaco.corgilib.world.level.ScheduledRandomTickData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SerializableChunkData.class)
public class MixinSerializableChunkData implements RandomTickScheduler {
    @Unique
    private final List<BlockPos> corgilib$scheduledRandomTicks = new ArrayList<>();

    @Override
    public void scheduleRandomTick(BlockPos pos) {
        corgilib$scheduledRandomTicks.add(pos.immutable());
    }

    @Override
    public List<BlockPos> getScheduledRandomTicks() {
        return corgilib$scheduledRandomTicks;
    }

    @Inject(method = "copyOf", at = @At("RETURN"))
    private static void snapshotTicks(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<SerializableChunkData> cir) {
        ((RandomTickScheduler) (Object) cir.getReturnValue()).getScheduledRandomTicks()
                .addAll(List.copyOf(((RandomTickScheduler) chunk).getScheduledRandomTicks()));
    }

    @Inject(method = "parse", at = @At("RETURN"))
    private static void parseTicks(LevelHeightAccessor height, PalettedContainerFactory factory, CompoundTag tag,
                                   CallbackInfoReturnable<SerializableChunkData> cir) {
        if (cir.getReturnValue() != null) {
            ((RandomTickScheduler) (Object) cir.getReturnValue()).getScheduledRandomTicks().addAll(ScheduledRandomTickData.read(tag));
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void writeTicks(CallbackInfoReturnable<CompoundTag> cir) {
        ScheduledRandomTickData.write(cir.getReturnValue(), corgilib$scheduledRandomTicks);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void restoreTicks(ServerLevel level, PoiManager poiManager, RegionStorageInfo region, ChunkPos pos,
                              CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk loaded = cir.getReturnValue();
        ChunkAccess target = loaded instanceof ImposterProtoChunk imposter ? imposter.getWrapped() : loaded;
        ((RandomTickScheduler) target).getScheduledRandomTicks().addAll(corgilib$scheduledRandomTicks);
    }
}

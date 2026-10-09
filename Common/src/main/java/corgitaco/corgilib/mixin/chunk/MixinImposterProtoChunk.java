package corgitaco.corgilib.mixin.chunk;

import corgitaco.corgilib.world.level.RandomTickScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ImposterProtoChunk.class)
public abstract class MixinImposterProtoChunk implements RandomTickScheduler {
    @Shadow @Final
    private LevelChunk wrapped;

    @Override
    public void scheduleRandomTick(BlockPos pos) {
        ((RandomTickScheduler) wrapped).scheduleRandomTick(pos);
    }

    @Override
    public List<BlockPos> getScheduledRandomTicks() {
        return ((RandomTickScheduler) wrapped).getScheduledRandomTicks();
    }
}

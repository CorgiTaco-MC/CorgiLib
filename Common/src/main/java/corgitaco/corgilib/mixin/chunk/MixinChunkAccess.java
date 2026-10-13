package corgitaco.corgilib.mixin.chunk;


import corgitaco.corgilib.world.level.RandomTickScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChunkAccess.class)
public class MixinChunkAccess implements RandomTickScheduler {

    @Unique
    private final List<BlockPos> corgilib$scheduledRandomTicks = new ArrayList<>();


    @Override
    public void corgilib$scheduleRandomTick(BlockPos pos) {
        corgilib$scheduledRandomTicks.add(pos.immutable());
    }

    @Override
    public List<BlockPos> corgilib$getScheduledRandomTicks() {
        return corgilib$scheduledRandomTicks;
    }
}

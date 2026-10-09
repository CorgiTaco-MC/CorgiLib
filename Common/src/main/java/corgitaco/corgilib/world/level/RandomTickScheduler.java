package corgitaco.corgilib.world.level;


import net.minecraft.core.BlockPos;

import java.util.List;

public interface RandomTickScheduler {

    void corgilib$scheduleRandomTick(BlockPos pos);


    List<BlockPos> corgilib$getScheduledRandomTicks();
}

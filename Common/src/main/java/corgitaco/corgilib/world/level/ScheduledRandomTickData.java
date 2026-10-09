package corgitaco.corgilib.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public final class ScheduledRandomTickData {
    private ScheduledRandomTickData() {
    }

    public static List<BlockPos> read(CompoundTag chunkTag) {
        List<BlockPos> positions = new ArrayList<>();
        for (Tag entry : chunkTag.getCompoundOrEmpty("corgilib").getListOrEmpty("scheduled_random_ticks")) {
            if (entry instanceof IntArrayTag array) {
                int[] coordinates = array.getAsIntArray();
                if (coordinates.length == 3) {
                    positions.add(new BlockPos(coordinates[0], coordinates[1], coordinates[2]));
                }
            }
        }
        return positions;
    }

    public static void write(CompoundTag chunkTag, List<BlockPos> positions) {
        if (positions.isEmpty()) {
            chunkTag.getCompoundOrEmpty("corgilib").remove("scheduled_random_ticks");
            return;
        }
        CompoundTag data = chunkTag.getCompoundOrEmpty("corgilib");
        ListTag ticks = new ListTag();
        for (BlockPos position : positions) {
            ticks.add(new IntArrayTag(new int[]{position.getX(), position.getY(), position.getZ()}));
        }
        data.put("scheduled_random_ticks", ticks);
        chunkTag.put("corgilib", data);
    }
}

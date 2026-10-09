package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class PrecipitationAtCondition implements Condition {

    public static Codec<PrecipitationAtCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(BlockPos.CODEC.optionalFieldOf("offset", BlockPos.ZERO).forGetter(precipitationAtCondition -> precipitationAtCondition.offset), Codec.BOOL.optionalFieldOf("snow", false).forGetter(precipitationAtCondition -> precipitationAtCondition.snow)).apply(builder, PrecipitationAtCondition::new));

    private final BlockPos offset;
    private final boolean snow;

    public PrecipitationAtCondition(BlockPos offset, boolean snow) {
        this.offset = offset;
        this.snow = snow;
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        BlockPos offset = conditionContext.entity().blockPosition().offset(this.offset);
        Level world = conditionContext.world();
        return world.precipitationAt(offset) == (snow ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN);
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}

package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.Collection;
//TODO: Fix lunar phase condition to actually check lunar phase, it seems to be missing in current Mappings
public class LunarPhaseCondition implements Condition {

    public static final Codec<LunarPhaseCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(Codec.INT.listOf().fieldOf("is_valid_moon_phase").forGetter(lunarPhaseCondition -> new ArrayList<>(lunarPhaseCondition.validMoonPhases))).apply(builder, LunarPhaseCondition::new));

    private final IntSet validMoonPhases = new IntArraySet();

    public LunarPhaseCondition(Collection<Integer> validMoonPhases) {
        this.validMoonPhases.addAll(validMoonPhases);
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        return this.validMoonPhases.contains(conditionContext.world());
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}

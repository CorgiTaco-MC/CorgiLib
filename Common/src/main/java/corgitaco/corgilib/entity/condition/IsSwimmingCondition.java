package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class IsSwimmingCondition implements Condition {

    public static final Codec<IsSwimmingCondition> CODEC = MapCodec.unitCodec(IsSwimmingCondition::new);

    @Override
    public boolean passes(ConditionContext conditionContext) {
        return conditionContext.entity().isSwimming();
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}
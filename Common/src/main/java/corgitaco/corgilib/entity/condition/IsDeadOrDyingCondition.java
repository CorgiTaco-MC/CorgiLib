package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class IsDeadOrDyingCondition implements Condition {
    public static final IsDeadOrDyingCondition INSTANCE = new IsDeadOrDyingCondition();
    public static final Codec<IsDeadOrDyingCondition> CODEC = MapCodec.unitCodec(() -> INSTANCE);

    @Override
    public boolean passes(ConditionContext conditionContext) {
        return conditionContext.isDeadOrDying();
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}

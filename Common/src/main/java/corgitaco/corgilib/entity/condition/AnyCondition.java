package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class AnyCondition implements Condition {

    public static final AnyCondition INSTANCE = new AnyCondition();

    public static final Codec<AnyCondition> CODEC = MapCodec.unitCodec(() -> INSTANCE);


    public AnyCondition() {
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        return true;
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}
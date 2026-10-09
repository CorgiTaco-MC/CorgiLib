package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.serialization.codec.CodecUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

public class HasEffectCondition implements Condition {

    public static final Codec<HasEffectCondition> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    CodecUtil.EFFECT_CODEC.listOf().validate(effects -> effects.isEmpty() ? DataResult.error(() -> "Effects condition requires at least 1 effect to check against.") : DataResult.success(effects)).fieldOf("effects").forGetter(hasEffectCondition -> hasEffectCondition.effects),
                    Codec.BOOL.fieldOf("has_any").forGetter(hasEffectCondition -> hasEffectCondition.hasAny)
            ).apply(builder, HasEffectCondition::new)
    );

    private final List<MobEffect> effects;
    private final boolean hasAny;

    public HasEffectCondition(List<MobEffect> effects, boolean hasAny) {
        this.effects = List.copyOf(effects);
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("Effects condition requires at least 1 effect to check against.");
        }
        this.hasAny = hasAny;
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        for (MobEffect effect : this.effects) {
            boolean hasEffect = conditionContext.entity().hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
            if (hasAny && hasEffect) {
                return true;
            }
            if (!hasAny && !hasEffect) {
                return false;
            }
        }
        return !hasAny;
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}
package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.comparator.DoubleComparator;
import corgitaco.corgilib.serialization.codec.CodecUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.Map;
import java.util.Set;

public class AttributeCondition implements Condition {

    public static final Codec<AttributeCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(Codec.unboundedMap(CodecUtil.ATTRIBUTE_CODEC, DoubleComparator.CODEC).fieldOf("attribute_is").forGetter(attributeCondition -> attributeCondition.attributeComparator)
    ).apply(builder, AttributeCondition::new));
    private final Map<Attribute, DoubleComparator> attributeComparator;
    private final Set<Map.Entry<Attribute, DoubleComparator>> entries;

    public AttributeCondition(Map<Attribute, DoubleComparator> attributeComparator) {
        this.attributeComparator = attributeComparator;
        this.entries = attributeComparator.entrySet();
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        if (attributeComparator.isEmpty()) {
            return false;
        }
        for (Map.Entry<Attribute, DoubleComparator> entry : entries) {
            AttributeInstance attribute = conditionContext.entity().getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getKey()));
            if (attribute == null) {
                return false;
            }

            if (!entry.getValue().check(attribute.getValue())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}

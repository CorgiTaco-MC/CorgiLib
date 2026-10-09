package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record EntityTypeTagFilter(TagKey<EntityType<?>> entityTypeTag) implements Condition {

    public static final Codec<EntityTypeTagFilter> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    TagKey.hashedCodec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(EntityTypeTagFilter::entityTypeTag)
            ).apply(builder, EntityTypeTagFilter::new)
    );

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        return conditionContext.entity().getType().builtInRegistryHolder().is(this.entityTypeTag);
    }
}

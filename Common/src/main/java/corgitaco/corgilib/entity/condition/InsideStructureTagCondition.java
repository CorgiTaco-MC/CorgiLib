package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.entity.IsInsideStructureTracker;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.List;
import java.util.function.Function;

public class InsideStructureTagCondition implements Condition {

    public static final Codec<InsideStructureTagCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(TagKey.codec(Registries.STRUCTURE).listOf().validate(tags -> tags.isEmpty() ? DataResult.error(() -> "No structures were specified.") : DataResult.success(tags)).fieldOf("structure_tag_is").forGetter(insideStructureTagCondition -> insideStructureTagCondition.structureTags),
            Codec.BOOL.optionalFieldOf("in_piece", false).forGetter(insideStructureTagCondition -> insideStructureTagCondition.intersectsPiece)
    ).apply(builder, InsideStructureTagCondition::new));

    private final List<TagKey<Structure>> structureTags;
    private final boolean intersectsPiece;

    public InsideStructureTagCondition(List<TagKey<Structure>> structureTags, boolean mustIntersectPiece) {
        if (structureTags.isEmpty()) {
            throw new IllegalArgumentException("No structures were specified.");
        }
        this.structureTags = List.copyOf(structureTags);
        this.intersectsPiece = mustIntersectPiece;
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        Level world = conditionContext.world();
        LivingEntity entity = conditionContext.entity();
        IsInsideStructureTracker tracker = ((IsInsideStructureTracker.Access) entity).getIsInsideStructureTracker();
        if (world.isClientSide()) {
            return matches(tracker.getTracker());
        }

        Registry<Structure> structures = world.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        ServerLevel serverLevel = (ServerLevel) world;
        BlockPos entityPosition = entity.blockPosition();
        IsInsideStructureTracker.IsInside inside = findInside(
                entityPosition,
                tag -> structures.getTagOrEmpty(tag),
                structure -> serverLevel.structureManager().startsForStructure(SectionPos.of(entityPosition), structure.value())
        );
        IsInsideStructureTracker.IsInside previous = tracker.getTracker();
        if (inside.isInsideStructure() || inside.isInsideStructurePiece()
                || previous.isInsideStructure() || previous.isInsideStructurePiece()) {
            tracker.setInside(world, entity, inside);
        }
        return matches(inside);
    }

    IsInsideStructureTracker.IsInside findInside(BlockPos position,
                                                  Function<TagKey<Structure>, Iterable<Holder<Structure>>> structuresForTag,
                                                  Function<Holder<Structure>, List<StructureStart>> startsForStructure) {
        boolean insideStructure = false;
        for (TagKey<Structure> tag : structureTags) {
            for (Holder<Structure> structure : structuresForTag.apply(tag)) {
                for (StructureStart start : startsForStructure.apply(structure)) {
                    if (!start.isValid() || !start.getBoundingBox().isInside(position)) {
                        continue;
                    }
                    insideStructure = true;
                    if (start.getPieces().stream().anyMatch(piece -> piece.getBoundingBox().isInside(position))) {
                        return new IsInsideStructureTracker.IsInside(true, true);
                    }
                }
            }
        }
        return new IsInsideStructureTracker.IsInside(insideStructure, false);
    }

    boolean matches(IsInsideStructureTracker.IsInside inside) {
        return intersectsPiece ? inside.isInsideStructurePiece() : inside.isInsideStructure();
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}

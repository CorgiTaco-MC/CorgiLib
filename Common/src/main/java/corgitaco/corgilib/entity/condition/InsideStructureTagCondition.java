package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.entity.IsInsideStructureTracker;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.List;
import java.util.Optional;

public class InsideStructureTagCondition implements Condition {

    public static final Codec<InsideStructureTagCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(TagKey.codec(Registries.STRUCTURE).listOf().fieldOf("structure_tag_is").forGetter(insideStructureTagCondition -> insideStructureTagCondition.structureTags),
            Codec.BOOL.optionalFieldOf("in_piece", false).forGetter(insideStructureTagCondition -> insideStructureTagCondition.intersectsPiece)
    ).apply(builder, InsideStructureTagCondition::new));

    private final List<TagKey<Structure>> structureTags;
    private final boolean intersectsPiece;

    public InsideStructureTagCondition(List<TagKey<Structure>> structureTags, boolean mustIntersectPiece) {
        if (structureTags.isEmpty()) {
            throw new IllegalArgumentException("No structures were specified.");
        }
        this.structureTags = structureTags;
        this.intersectsPiece = mustIntersectPiece;
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        Level world = conditionContext.world();
        LivingEntity entity = conditionContext.entity();
        if (world.isClientSide) {
            return clientPasses((IsInsideStructureTracker.Access) entity);
        } else {
            Registry<Structure> configuredStructureFeatures = world.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            for (TagKey<Structure> structureTag : structureTags) {
                HolderSet.Named<Structure> tag = configuredStructureFeatures.getOrThrow(structureTag);

                List<Holder<Structure>> structures = tag.stream().toList();

                for (Holder<Structure> structure : structures) {
                    BlockPos entityPosition = entity.blockPosition();
                    Optional<? extends StructureStart> possibleStructureStart = ((ServerLevel) world).structureManager().startsForStructure(SectionPos.of(entityPosition), structure.value()).stream().findFirst();

                    if (possibleStructureStart.isEmpty()) {
                        return false;
                    }

                    StructureStart structureStart = possibleStructureStart.get();

                    if (this.intersectsPiece) {
                        for (StructurePiece piece : structureStart.getPieces()) {
                            if (piece.getBoundingBox().isInside(entityPosition)) {
                                ((IsInsideStructureTracker.Access) entity).getIsInsideStructureTracker().setInside(world, entity, new IsInsideStructureTracker.IsInside(true, true));
                                return true;
                            } else {
                                ((IsInsideStructureTracker.Access) entity).getIsInsideStructureTracker().setInside(world, entity, new IsInsideStructureTracker.IsInside(structureStart.getBoundingBox().isInside(entityPosition), false));
                            }
                        }
                    } else {
                        if (structureStart.getBoundingBox().isInside(entityPosition)) {
                            ((IsInsideStructureTracker.Access) entity).getIsInsideStructureTracker().setInside(world, entity, new IsInsideStructureTracker.IsInside(true, false));
                            return true;
                        } else {
                            ((IsInsideStructureTracker.Access) entity).getIsInsideStructureTracker().setInside(world, entity, new IsInsideStructureTracker.IsInside(false, true));
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean clientPasses(IsInsideStructureTracker.Access entity) {
        IsInsideStructureTracker.IsInside tracker = entity.getIsInsideStructureTracker().getTracker();
        return (tracker.isInsideStructurePiece() && this.intersectsPiece) || tracker.isInsideStructure();
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}

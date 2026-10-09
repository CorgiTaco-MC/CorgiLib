package corgitaco.corgilib.mixin;

import corgitaco.corgilib.entity.IsInsideStructureTracker;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class MixinEntity implements IsInsideStructureTracker.Access {

    @Unique
    private final IsInsideStructureTracker corgilib$isInsideStructureTracker = new IsInsideStructureTracker();

    @Override
    public IsInsideStructureTracker getIsInsideStructureTracker() {
        return corgilib$isInsideStructureTracker;
    }
}

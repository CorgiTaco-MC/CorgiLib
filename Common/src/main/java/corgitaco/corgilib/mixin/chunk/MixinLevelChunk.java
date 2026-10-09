package corgitaco.corgilib.mixin.chunk;

import corgitaco.corgilib.world.level.RandomTickScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk implements RandomTickScheduler {

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V", at = @At("RETURN"))
    private void addScheduledRandomTicks(ServerLevel serverLevel, ProtoChunk chunk, LevelChunk.PostLoadProcessor $$2, CallbackInfo ci) {
        this.corgilib$getScheduledRandomTicks().addAll(((RandomTickScheduler) chunk).corgilib$getScheduledRandomTicks());
    }
}

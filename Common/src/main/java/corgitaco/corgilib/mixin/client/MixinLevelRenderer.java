package corgitaco.corgilib.mixin.client;

import corgitaco.corgilib.client.StructureBoxEditor;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "finalizeGizmoCollection", at = @At("HEAD"))
    private void extractStructureBox(CallbackInfo ci) {
        StructureBoxEditor.extractGizmos((LevelRenderer) (Object) this);
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void clearStructureBox(CallbackInfo ci) {
        StructureBoxEditor.clear();
    }
}

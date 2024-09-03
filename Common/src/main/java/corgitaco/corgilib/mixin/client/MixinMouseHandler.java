package corgitaco.corgilib.mixin.client;

import corgitaco.corgilib.client.StructureBoxEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {


    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;"), cancellable = true)
    private void editor(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        double scrollY = (this.minecraft.options.discreteMouseScroll().get() ? Math.signum(yOffset) : yOffset) * this.minecraft.options.mouseWheelSensitivity().get();
        if (StructureBoxEditor.onScroll(scrollY)) {
            ci.cancel();
        }
    }
}
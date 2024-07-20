package corgitaco.corgilib.mixin.client;

import com.mojang.blaze3d.platform.Window;
import corgitaco.corgilib.client.imgui.ImGuiImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Final
    private Window window;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initImGui(GameConfig gameConfig, CallbackInfo ci) {
        if (!Minecraft.ON_OSX)
            ImGuiImpl.create(window.getWindow());
    }
}

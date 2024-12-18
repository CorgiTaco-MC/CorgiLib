package corgitaco.corgilib.fabric.mixin;

import com.electronwill.nightconfig.core.file.FileWatcher;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {

    @Inject(method = "stopServer", at = @At("RETURN"))
    private void killNightConfig(CallbackInfo ci) {
        FileWatcher.defaultInstance().stop();
    }
}

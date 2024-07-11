package corgitaco.corgilib.forge.platform;

import com.google.auto.service.AutoService;
import corgitaco.corgilib.CorgiLib;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.DeferredRegister;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

@AutoService(ModPlatform.class)
public class ForgePlatform implements ModPlatform {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Collection<String> getModIDS() {
        return ModList.get().getMods().stream().map(IModInfo::getModId).toList();
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static final Map<ResourceKey<?>, DeferredRegister> CACHED = new Reference2ObjectOpenHashMap<>();

    @Override
    public <T> Supplier<T> register(Registry<T> registry, String location, Supplier<T> value) {
        return CACHED.computeIfAbsent(registry.key(), key -> DeferredRegister.create(registry.key().location(), CorgiLib.MOD_ID)).register(location, value);
    }
}

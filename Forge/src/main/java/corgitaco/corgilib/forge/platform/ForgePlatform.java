package corgitaco.corgilib.forge.platform;

import com.google.auto.service.AutoService;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.platform.ModPlatform;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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

    @Override
    public <T> Supplier<Registry<T>> createSimpleBuiltin(ResourceKey<Registry<T>> registryKey) {
        if (BuiltInRegistries.REGISTRY instanceof MappedRegistry<? extends Registry<?>> mappedRegistry) { // We have to unlock the registry first
            mappedRegistry.unfreeze();
        }

        Registry<T> registry = BuiltInRegistries.registerSimple(registryKey, Lifecycle.stable(), builder -> (T) new Object());

        if (BuiltInRegistries.REGISTRY instanceof MappedRegistry<? extends Registry<?>> mappedRegistry) { // Relock the registry
            mappedRegistry.freeze();
        }
        return () -> registry;
    }

    public static final List<Consumer<DataPackRegistryEvent.NewRegistry>> DATAPACK_REGISTRIES = new ArrayList<>();

    @Override
    public <T> void registerDatapackRegistry(ResourceKey<Registry<T>> key, Supplier<Codec<T>> codec) {
        DATAPACK_REGISTRIES.add(newRegistry -> newRegistry.dataPackRegistry(key, codec.get()));
    }
}

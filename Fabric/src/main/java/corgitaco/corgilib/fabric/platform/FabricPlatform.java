package corgitaco.corgilib.fabric.platform;

import com.google.auto.service.AutoService;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.platform.ModPlatform;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@AutoService(ModPlatform.class)
public class FabricPlatform implements ModPlatform {
    private static final Supplier<List<String>> MOD_IDS = Suppliers.memoize(() -> FabricLoader.getInstance().getAllMods().stream().map(modContainer -> modContainer.getMetadata().getId()).toList());


    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }


    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Collection<String> getModIDS() {
        return MOD_IDS.get();
    }

    @Override
    public <T> Supplier<T> register(Registry<T> registry, String name, Supplier<T> value) {
        T value1 = Registry.register(registry, CorgiLib.createLocation(name), value.get());
        return () -> value1;
    }

    @Override
    public <T> Supplier<Registry<T>> createSimpleBuiltin(ResourceKey<Registry<T>> registryKey) {
        MappedRegistry<T> registry = FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
        Supplier<Registry<T>> supplier = () -> registry;
        return supplier;
    }

    @Override
    public <T> void registerDatapackRegistry(ResourceKey<Registry<T>> key, Supplier<Codec<T>> codec) {
        DynamicRegistries.registerSynced(key, codec.get());
    }
}

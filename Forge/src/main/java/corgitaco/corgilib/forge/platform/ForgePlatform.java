package corgitaco.corgilib.forge.platform;

import com.google.auto.service.AutoService;
import com.mojang.serialization.Codec;
import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.platform.ModPlatform;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import org.jspecify.annotations.Nullable;

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
        return ModList.isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Collection<String> getModIDS() {
        return ModList.getMods().stream().map(IModInfo::getModId).toList();
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static final Map<ResourceKey<?>, DeferredRegister<?>> CACHED = new Reference2ObjectOpenHashMap<>();
    private static @Nullable BusGroup registrationBus;

    public static synchronized void bindRegistries(BusGroup bus) {
        if (registrationBus != null) {
            if (registrationBus != bus) {
                throw new IllegalStateException("CorgiLib registries are already bound to another event bus");
            }
            return;
        }
        registrationBus = bus;
        CACHED.values().forEach(register -> register.register(bus));
    }

    @SuppressWarnings("unchecked")
    private static synchronized <T> DeferredRegister<T> deferredRegister(ResourceKey<? extends Registry<T>> key) {
        return (DeferredRegister<T>) CACHED.computeIfAbsent(key, ignored -> {
            DeferredRegister<T> register = DeferredRegister.create(key, CorgiLib.MOD_ID);
            if (registrationBus != null) {
                register.register(registrationBus);
            }
            return register;
        });
    }

    @Override
    public <T> Supplier<T> register(Registry<T> registry, String location, Supplier<T> value) {
        return deferredRegister(registry.key()).register(location, value);
    }

    @Override
    public <T> Supplier<T> register(ResourceKey<Registry<T>> registryKey, String location, Supplier<T> value) {
        return deferredRegister(registryKey).register(location, value);
    }

    @Override
    public <T> Supplier<Registry<T>> createSimpleBuiltin(ResourceKey<Registry<T>> registryKey) {
        deferredRegister(registryKey).makeRegistry(() -> RegistryBuilder.<T>of().hasTags());
        return () -> builtinRegistry(registryKey);
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> builtinRegistry(ResourceKey<Registry<T>> registryKey) {
        Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        if (registry == null) {
            throw new IllegalStateException("Registry " + registryKey.identifier() + " has not been registered yet");
        }
        return (Registry<T>) registry;
    }

    public static final List<Consumer<DataPackRegistryEvent.NewRegistry>> DATAPACK_REGISTRIES = new ArrayList<>();

    @Override
    public <T> void registerDatapackRegistry(ResourceKey<Registry<T>> key, Supplier<Codec<T>> codec) {
        DATAPACK_REGISTRIES.add(newRegistry -> {
            Codec<T> resolvedCodec = codec.get();
            newRegistry.dataPackRegistry(key, resolvedCodec, resolvedCodec);
        });
    }
}

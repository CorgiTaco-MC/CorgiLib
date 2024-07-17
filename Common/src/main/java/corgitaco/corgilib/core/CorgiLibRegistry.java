package corgitaco.corgilib.core;

import com.mojang.serialization.Codec;
import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.entity.condition.Condition;
import corgitaco.corgilib.entity.npc.VillagerTradeRegistry;
import corgitaco.corgilib.platform.ModPlatform;
import corgitaco.corgilib.math.blendingfunction.BlendingFunction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.function.Supplier;

public class CorgiLibRegistry {

    public static final ResourceKey<Registry<Codec<? extends BlendingFunction>>> BLENDING_FUNCTION_RESOURCE_KEY = ResourceKey.createRegistryKey(CorgiLib.createLocation("blending_function"));

    public static final Supplier<Registry<Codec<? extends BlendingFunction>>> BLENDING_FUNCTION = ModPlatform.PLATFORM.createSimpleBuiltin(BLENDING_FUNCTION_RESOURCE_KEY);

    public static final ResourceKey<Registry<Codec<? extends VillagerTrades.ItemListing>>> VILLAGER_TRADES_ITEM_LISTING_RESOURCE_KEY = ResourceKey.createRegistryKey(CorgiLib.createLocation("villager_trades_item_listing"));

    public static final Supplier<Registry<Codec<? extends VillagerTrades.ItemListing>>> VILLAGER_TRADES_ITEM_LISTING = ModPlatform.PLATFORM.createSimpleBuiltin(VILLAGER_TRADES_ITEM_LISTING_RESOURCE_KEY);

    public static final ResourceKey<Registry<Codec<? extends Condition>>> CONDITION_KEY = ResourceKey.createRegistryKey(CorgiLib.createLocation("condition"));

    public static final Supplier<Registry<Codec<? extends Condition>>> CONDITION = ModPlatform.PLATFORM.createSimpleBuiltin(CONDITION_KEY);

    public static void init() {
        BlendingFunction.register();
        VillagerTradeRegistry.register();
        Condition.register();
    }
}

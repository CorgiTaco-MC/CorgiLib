package corgitaco.corgilib.entity.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.core.CorgiLibRegistry;
import corgitaco.corgilib.platform.ModPlatform;
import corgitaco.corgilib.serialization.codec.CodecUtil;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VillagerTradeRegistry {

    public static final Codec<Item> ITEM_CODEC = CodecUtil.createLoggedExceptionRegistryCodec(BuiltInRegistries.ITEM);
    public static final Codec<MobEffect> MOB_EFFECT_CODEC = CodecUtil.createLoggedExceptionRegistryCodec(BuiltInRegistries.MOB_EFFECT);

    private final static Codec<VillagerTrades.EmeraldForItems> EMERALD_FOR_ITEMS_CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ITEM_CODEC.fieldOf("item").forGetter(listing -> listing.item),
                    Codec.INT.fieldOf("cost").forGetter(listing -> listing.cost),
                    Codec.INT.fieldOf("max_uses").forGetter(listing -> listing.maxUses),
                    Codec.INT.fieldOf("villager_xp").forGetter(listing -> listing.villagerXp)
            ).apply(builder, VillagerTrades.EmeraldForItems::new)
    );

    private final static Codec<VillagerTrades.ItemsForEmeralds> ITEMS_FOR_EMERALDS_CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ITEM_CODEC.fieldOf("item").forGetter(listing -> listing.itemStack.getItem()),
                    Codec.INT.fieldOf("emerald_cost").forGetter(listing -> listing.emeraldCost),
                    Codec.INT.fieldOf("number_of_items").forGetter(listing -> listing.numberOfItems),
                    Codec.INT.fieldOf("max_uses").forGetter(listing -> listing.maxUses),
                    Codec.INT.fieldOf("villager_xp").forGetter(listing -> listing.villagerXp)
            ).apply(builder, VillagerTrades.ItemsForEmeralds::new)
    );

    private final static Codec<VillagerTrades.ItemsAndEmeraldsToItems> ITEMS_AND_EMERALDS_TO_ITEMS_CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ITEM_CODEC.fieldOf("from_item").forGetter(listing -> listing.fromItem.getItem()),
                    Codec.INT.fieldOf("from_count").forGetter(listing -> listing.fromCount),
                    Codec.INT.fieldOf("emerald_cost").forGetter(listing -> listing.emeraldCost),
                    ITEM_CODEC.fieldOf("to_item").forGetter(listing -> listing.toItem.getItem()),
                    Codec.INT.fieldOf("to_count").forGetter(listing -> listing.toCount),
                    Codec.INT.fieldOf("max_uses").forGetter(listing -> listing.maxUses),
                    Codec.INT.fieldOf("villager_xp").forGetter(listing -> listing.villagerXp)
            ).apply(builder, VillagerTrades.ItemsAndEmeraldsToItems::new)
    );

    private final static Codec<VillagerTrades.SuspiciousStewForEmerald> SUSPICIOUS_STEW_FOR_EMERALD_CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    MOB_EFFECT_CODEC.fieldOf("mob_effect").forGetter(listing -> listing.effect),
                    Codec.INT.fieldOf("duration").forGetter(listing -> listing.duration),
                    Codec.INT.fieldOf("xp").forGetter(listing -> listing.xp)
            ).apply(builder, VillagerTrades.SuspiciousStewForEmerald::new)
    );

    public static final Codec<MapDecoration.Type> MAP_DECORATION_TYPE_CODEC = Codec.STRING.comapFlatMap(type -> {
        try {
            return DataResult.success(MapDecoration.Type.valueOf(type.toUpperCase()));
        } catch (Exception e) {
            return DataResult.error(() -> "Invalid Map Decoration Type! You put \"%s\". Valid values: %s".formatted(type, Arrays.toString(MapDecoration.Type.values())));
        }
    }, MapDecoration.Type::name);

    private final static Codec<VillagerTrades.TreasureMapForEmeralds> TREASURE_MAP_FOR_EMERALDS_CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.INT.fieldOf("emerald_cost").forGetter(listing -> listing.emeraldCost),
                    TagKey.hashedCodec(Registries.STRUCTURE).fieldOf("destination").forGetter(listing -> listing.destination),
                    Codec.STRING.fieldOf("display_name").forGetter(listing -> listing.displayName),
                    MAP_DECORATION_TYPE_CODEC.fieldOf("destination_type").forGetter(listing -> listing.destinationType),
                    Codec.INT.fieldOf("max_uses").forGetter(listing -> listing.maxUses),
                    Codec.INT.fieldOf("villager_xp").forGetter(listing -> listing.villagerXp)
            ).apply(builder, VillagerTrades.TreasureMapForEmeralds::new)
    );

    private static final Map<Class<? extends VillagerTrades.ItemListing>, Codec<? extends VillagerTrades.ItemListing>> ITEM_LISTING_CLASS_BY_CODEC = Util.make(new HashMap<>(), map -> {
        map.put(VillagerTrades.EmeraldForItems.class, EMERALD_FOR_ITEMS_CODEC);
        map.put(VillagerTrades.ItemsForEmeralds.class, ITEMS_FOR_EMERALDS_CODEC);
        map.put(VillagerTrades.SuspiciousStewForEmerald.class, SUSPICIOUS_STEW_FOR_EMERALD_CODEC);
        map.put(VillagerTrades.ItemsAndEmeraldsToItems.class, ITEMS_AND_EMERALDS_TO_ITEMS_CODEC);
        map.put(VillagerTrades.TreasureMapForEmeralds.class, TREASURE_MAP_FOR_EMERALDS_CODEC);
    });

    public static final Codec<VillagerTrades.ItemListing> ITEM_LISTING_CODEC = ExtraCodecs.lazyInitializedCodec(() -> CorgiLibRegistry.VILLAGER_TRADES_ITEM_LISTING.get().byNameCodec()
            .dispatchStable(itemListing -> ITEM_LISTING_CLASS_BY_CODEC.get(itemListing.getClass()), codec -> codec.fieldOf("config").codec()));

    public static void register() {
        ModPlatform.PLATFORM.register(CorgiLibRegistry.VILLAGER_TRADES_ITEM_LISTING.get(), "emerald_for_items", () -> EMERALD_FOR_ITEMS_CODEC);
        ModPlatform.PLATFORM.register(CorgiLibRegistry.VILLAGER_TRADES_ITEM_LISTING.get(), "items_for_emeralds", () -> ITEMS_FOR_EMERALDS_CODEC);
        ModPlatform.PLATFORM.register(CorgiLibRegistry.VILLAGER_TRADES_ITEM_LISTING.get(), "suspicious_stew_for_emerald", () -> SUSPICIOUS_STEW_FOR_EMERALD_CODEC);
        ModPlatform.PLATFORM.register(CorgiLibRegistry.VILLAGER_TRADES_ITEM_LISTING.get(), "items_and_emeralds_to_items", () -> ITEMS_AND_EMERALDS_TO_ITEMS_CODEC);
        ModPlatform.PLATFORM.register(CorgiLibRegistry.VILLAGER_TRADES_ITEM_LISTING.get(), "treasure_map_for_emeralds", () -> TREASURE_MAP_FOR_EMERALDS_CODEC);
    }
}

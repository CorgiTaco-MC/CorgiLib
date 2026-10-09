package corgitaco.corgilib.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;

public class CodecUtil {

    public static final Codec<Block> BLOCK_CODEC = createLoggedExceptionRegistryCodec(BuiltInRegistries.BLOCK);

    public static final Codec<MobEffect> MOB_EFFECT = createLoggedExceptionRegistryCodec(BuiltInRegistries.MOB_EFFECT);
    public static final Codec<EntityType<?>> ENTITY_TYPE = createLoggedExceptionRegistryCodec(BuiltInRegistries.ENTITY_TYPE);

    public static final Codec<EntityType<?>> ENTITY_TYPE_CODEC = createLoggedExceptionRegistryCodec(BuiltInRegistries.ENTITY_TYPE);
    public static final Codec<Attribute> ATTRIBUTE_CODEC = createLoggedExceptionRegistryCodec(BuiltInRegistries.ATTRIBUTE);
    public static final Codec<Item> ITEM_CODEC = createLoggedExceptionRegistryCodec(BuiltInRegistries.ITEM);
    public static final Codec<MobEffect> EFFECT_CODEC = createLoggedExceptionRegistryCodec(BuiltInRegistries.MOB_EFFECT);

    public static final Codec<ResourceKey<Biome>> BIOME_CODEC = Identifier.CODEC.comapFlatMap(Identifier -> DataResult.success(ResourceKey.create(Registries.BIOME, Identifier)), ResourceKey::identifier);

    public static final Codec<EquipmentSlot> EQUIPMENT_SLOT_CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(EquipmentSlot.byName(s.toLowerCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> String.format("\"%s\" is not a valid equipmentSlotType. Valid equipmentSlotTypes: %s", s, Arrays.toString(Arrays.stream(EquipmentSlot.values()).map(EquipmentSlot::getName).toArray())));
        }

    }, EquipmentSlot::getName);
    public static final Codec<Difficulty> DIFFICULTY_CODEC = Codec.STRING.comapFlatMap(s -> {
        final Difficulty difficulty = Difficulty.byName(s.toLowerCase(Locale.ROOT));
        if (difficulty == null) {
            return DataResult.error(() -> String.format("\"%s\" is not a valid difficulty. Valid difficulties: %s", s, Arrays.toString(Arrays.stream(Difficulty.values()).map(Difficulty::getSerializedName).toArray())));
        }
        return DataResult.success(difficulty);

    }, Difficulty::getSerializedName);

    public static final Codec<ClickEvent.Action> CLICK_EVENT_ACTION_CODEC = Codec.STRING.comapFlatMap(name -> {
        try {
            return DataResult.success(ClickEvent.Action.valueOf(name.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Unknown click event action: " + name);
        }
    }, ClickEvent.Action::name);

    public static final Codec<ClickEvent> CLICK_EVENT_CODEC = ClickEvent.Action.UNSAFE_CODEC
            .<ClickEvent>dispatch("action", ClickEvent::action, ClickEvent.Action::valueCodec).withAlternative(
            RecordCodecBuilder.<LegacyClickEvent>create(builder -> builder.group(
                    CLICK_EVENT_ACTION_CODEC.fieldOf("action").forGetter(LegacyClickEvent::action),
                    Codec.STRING.fieldOf("value").forGetter(LegacyClickEvent::value)
            ).apply(builder, LegacyClickEvent::new)).comapFlatMap(LegacyClickEvent::toClickEvent, LegacyClickEvent::fromClickEvent)
    );

    private record LegacyClickEvent(ClickEvent.Action action, String value) {
        DataResult<ClickEvent> toClickEvent() {
            try {
                return switch (action) {
                    case OPEN_URL -> DataResult.success(new ClickEvent.OpenUrl(URI.create(value)));
                    case OPEN_FILE -> DataResult.success(new ClickEvent.OpenFile(value));
                    case RUN_COMMAND -> DataResult.success(new ClickEvent.RunCommand(value));
                    case SUGGEST_COMMAND -> DataResult.success(new ClickEvent.SuggestCommand(value));
                    case CHANGE_PAGE -> DataResult.success(new ClickEvent.ChangePage(Integer.parseInt(value)));
                    case COPY_TO_CLIPBOARD -> DataResult.success(new ClickEvent.CopyToClipboard(value));
                    default -> DataResult.error(() -> "Click event action requires the modern event format: " + action);
                };
            } catch (IllegalArgumentException exception) {
                return DataResult.error(() -> "Invalid value for click event " + action + ": " + exception.getMessage());
            }
        }

        static LegacyClickEvent fromClickEvent(ClickEvent event) {
            return switch (event) {
                case ClickEvent.OpenUrl open -> new LegacyClickEvent(event.action(), open.uri().toString());
                case ClickEvent.OpenFile open -> new LegacyClickEvent(event.action(), open.path());
                case ClickEvent.RunCommand command -> new LegacyClickEvent(event.action(), command.command());
                case ClickEvent.SuggestCommand command -> new LegacyClickEvent(event.action(), command.command());
                case ClickEvent.ChangePage page -> new LegacyClickEvent(event.action(), Integer.toString(page.page()));
                case ClickEvent.CopyToClipboard copy -> new LegacyClickEvent(event.action(), copy.value());
                default -> throw new IllegalArgumentException("Click event requires the modern event format: " + event.action());
            };
        }
    }

    public static Function<String, DataResult<Integer>> validateColorHex() {
        return input -> {

            if (input.isEmpty()) {
                return DataResult.success(ChatFormatting.WHITE.getColor());
            }

            try {
                return DataResult.success((int) Long.parseLong(input.replace("#", "").replace("0x", ""), 16));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return DataResult.error(e::getMessage);
            }

        };
    }


    public static Codec<Integer> COLOR_FROM_HEX = Codec.STRING.comapFlatMap(validateColorHex(), Integer::toHexString);

    public record LazyCodec<TYPE>(Supplier<Codec<TYPE>> delegate) implements Codec<TYPE> {

        @Override
        public <T> DataResult<T> encode(TYPE input, DynamicOps<T> ops, T prefix) {
            return this.delegate().get().encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<TYPE, T>> decode(DynamicOps<T> ops, T input) {
            return this.delegate().get().decode(ops, input);
        }
    }


    public static final Codec<Integer> INTEGER_KEY_CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(Integer.valueOf(s));
        } catch (NumberFormatException exception) {
            return DataResult.error(() -> "Not an integer key: " + s);
        }
    }, Object::toString);

    public static Codec<Integer> intKeyRangeCodec(int min, int max) {
        Function<Integer, DataResult<Integer>> check = Codec.checkRange(min, max);
        return INTEGER_KEY_CODEC.flatXmap(check, check);
    }

    public static <T> Codec<T> createLoggedExceptionRegistryCodec(Registry<T> registry) {
        return Identifier.CODEC.comapFlatMap(location -> {
            final Optional<T> result = registry.getOptional(location);

            if (result.isEmpty()) {
                StringBuilder registryElements = new StringBuilder();
                for (int i = 0; i < registry.entrySet().size(); i++) {
                    final T object = registry.byId(i);
                    registryElements.append(i).append(". \"").append(registry.getKey(object).toString()).append("\"\n");
                }

                return DataResult.error(() -> String.format("\"%s\" is not a valid id in registry: %s.\nCurrent Registry Values:\n\n%s\n", location.toString(), registry, registryElements));
            }
            return DataResult.success(result.get());
        }, registry::getKey);
    }


    public static <T> Codec<WrapForSerialization<T>> wrapCodecForCollectionSerializing(Codec<T> codec) {
        return RecordCodecBuilder.create(builder ->
                builder.group(
                        codec.fieldOf("value").forGetter(tWrapForSerialization -> tWrapForSerialization.value)
                ).apply(builder, WrapForSerialization::new));
    }

    public static <T> WrapForSerialization<T> wrap(T toWrap) {
        return new WrapForSerialization<>(toWrap);
    }

    public record WrapForSerialization<T>(T value) {
    }
}

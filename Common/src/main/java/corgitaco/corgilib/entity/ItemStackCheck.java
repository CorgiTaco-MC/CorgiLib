package corgitaco.corgilib.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.comparator.DoubleComparator;
import corgitaco.corgilib.serialization.codec.CodecUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Map;
import java.util.Optional;

public class ItemStackCheck {

    public static final Codec<ItemStackCheck> CODEC = RecordCodecBuilder.<Settings>create(builder -> builder.group(
                    CodecUtil.ITEM_CODEC.fieldOf("item").forGetter(Settings::item),
                    DoubleComparator.CODEC.optionalFieldOf("durability_is").forGetter(Settings::durability),
                    DoubleComparator.CODEC.optionalFieldOf("stack_size_is").forGetter(Settings::stackSize),
                    Codec.unboundedMap(Enchantment.CODEC, DoubleComparator.CODEC).optionalFieldOf("enchantment_check").forGetter(Settings::enchantments))
            .apply(builder, Settings::new)).comapFlatMap(settings -> {
                if (settings.durability().isEmpty() && settings.stackSize().isEmpty() && settings.enchantments().isEmpty()) {
                    return DataResult.error(() -> "We need at least one check in an Item Stack Check!");
                }
                return DataResult.success(new ItemStackCheck(settings.item(), settings.durability(), settings.stackSize(), settings.enchantments()));
            }, check -> new Settings(check.item, check.durabilityComparator, check.stackSizeComparator, check.enchantmentLevelComparator));

    private record Settings(Item item, Optional<DoubleComparator> durability, Optional<DoubleComparator> stackSize,
                            Optional<Map<Holder<Enchantment>, DoubleComparator>> enchantments) {
    }

    private final Item item;
    private final Optional<DoubleComparator> durabilityComparator;
    private final Optional<DoubleComparator> stackSizeComparator;
    private final Optional<Map<Holder<Enchantment>, DoubleComparator>> enchantmentLevelComparator;

    public ItemStackCheck(Item item, Optional<DoubleComparator> durabilityComparator, Optional<DoubleComparator> stackSizeComparator, Optional<Map<Holder<Enchantment>, DoubleComparator>> enchantmentLevelComparator) {
        this.item = item;
        if (durabilityComparator.isEmpty() && stackSizeComparator.isEmpty() && enchantmentLevelComparator.isEmpty()) {
            throw new IllegalArgumentException("We need at least one check in an Item Stack Check!");
        }
        this.durabilityComparator = durabilityComparator;
        this.stackSizeComparator = stackSizeComparator;
        this.enchantmentLevelComparator = enchantmentLevelComparator.map(Map::copyOf);
    }

    public boolean test(ItemStack itemStack) {
        if (item == itemStack.getItem()) {
            if (durabilityComparator.isPresent() && !durabilityComparator.get().check(itemStack.getDamageValue())) {
                return false;
            }
            if (stackSizeComparator.isPresent() && !stackSizeComparator.get().check(itemStack.getCount())) {
                return false;
            }

            if (enchantmentLevelComparator.isPresent()) {
                ItemEnchantments enchantments = itemStack.getEnchantments();
                for (Map.Entry<Holder<Enchantment>, DoubleComparator> check : enchantmentLevelComparator.get().entrySet()) {
                    if (!check.getValue().check(enchantments.getLevel(check.getKey()))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public Item getItem() {
        return item;
    }
}
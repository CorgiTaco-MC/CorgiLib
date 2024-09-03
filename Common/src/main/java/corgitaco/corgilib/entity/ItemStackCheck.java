package corgitaco.corgilib.entity;

import com.mojang.serialization.Codec;
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
import java.util.Set;

public class ItemStackCheck {

    public static final Codec<ItemStackCheck> CODEC = RecordCodecBuilder.create(builder -> builder.group(CodecUtil.ITEM_CODEC.fieldOf("item").forGetter(itemStackCheck -> itemStackCheck.item), DoubleComparator.CODEC.optionalFieldOf("durability_is").forGetter(itemStackCheck -> itemStackCheck.durabilityComparator),
                    DoubleComparator.CODEC.optionalFieldOf("stack_size_is").forGetter(itemStackCheck -> itemStackCheck.stackSizeComparator),
                    Codec.unboundedMap(Enchantment.CODEC, DoubleComparator.CODEC).optionalFieldOf("enchantment_check").forGetter(itemStackCheck -> itemStackCheck.enchantmentLevelComparator))
            .apply(builder, ItemStackCheck::new));

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
        this.enchantmentLevelComparator = enchantmentLevelComparator;
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
                Map<Holder<Enchantment>, DoubleComparator> enchantmentComparator = enchantmentLevelComparator.get();


                ItemEnchantments enchantments = itemStack.getEnchantments();
                Set<Holder<Enchantment>> holders = enchantments.keySet();
                for (Holder<Enchantment> enchantmentHolder : holders) {
                    if (enchantmentComparator.containsKey(enchantmentHolder)) {
                        final DoubleComparator doubleComparator = enchantmentComparator.get(enchantmentHolder);
                        if (!doubleComparator.check(enchantments.getLevel(enchantmentHolder))) {
                            return false;
                        }
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
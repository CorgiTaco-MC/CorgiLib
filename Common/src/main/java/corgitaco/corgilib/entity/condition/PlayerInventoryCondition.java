package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.entity.ItemStackCheck;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class PlayerInventoryCondition implements Condition {

    public static final Codec<PlayerInventoryCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemStackCheck.CODEC.listOf().validate(PlayerInventoryCondition::validateChecks).fieldOf("has")
                    .forGetter(condition -> condition.stackChecks)
    ).apply(builder, PlayerInventoryCondition::new));

    private static DataResult<List<ItemStackCheck>> validateChecks(List<ItemStackCheck> checks) {
        if (checks.isEmpty()) {
            return DataResult.error(() -> "No item stack checks were specified.");
        }
        Set<Item> items = new HashSet<>();
        for (ItemStackCheck check : checks) {
            if (!items.add(check.getItem())) {
                return DataResult.error(() -> "Found another check for an already existing item.");
            }
        }
        return DataResult.success(checks);
    }

    private final List<ItemStackCheck> stackChecks;
    private final Map<Item, ItemStackCheck> itemItemStackCheckMap;

    public PlayerInventoryCondition(List<ItemStackCheck> stackChecks) {
        if (stackChecks.isEmpty()) {
            throw new IllegalArgumentException("No item stack checks were specified.");
        }
        this.stackChecks = List.copyOf(stackChecks);
        this.itemItemStackCheckMap = new Object2ObjectOpenHashMap<>();
        for (ItemStackCheck stackCheck : stackChecks) {
            final Item item = stackCheck.getItem();
            if (itemItemStackCheckMap.containsKey(item)) {
                throw new UnsupportedOperationException("Found another check for an already existing item.");
            } else {
                itemItemStackCheckMap.put(item.asItem(), stackCheck);
            }
        }
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        if (conditionContext.entity() instanceof Player player) {
            return testInventory(player.getInventory().getNonEquipmentItems());
        }
        return false;
    }

    public boolean testInventory(Iterable<ItemStack> inventory) {
        Set<Item> matchedItems = new HashSet<>();
        for (ItemStack item : inventory) {
            if (item.isEmpty()) {
                continue;
            }
            ItemStackCheck check = itemItemStackCheckMap.get(item.getItem());
            if (check != null && check.test(item)) {
                matchedItems.add(item.getItem());
                if (matchedItems.size() == itemItemStackCheckMap.size()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}

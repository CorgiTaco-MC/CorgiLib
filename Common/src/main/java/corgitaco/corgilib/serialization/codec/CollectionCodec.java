package corgitaco.corgilib.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;

import java.util.Collection;
import java.util.function.Supplier;

public record CollectionCodec<TYPE, COLLECTION extends Collection<TYPE>>(Codec<TYPE> element,
                                                                         Supplier<COLLECTION> collectionConstruction) implements Codec<COLLECTION> {

    @Override
    public <T> DataResult<T> encode(COLLECTION input, DynamicOps<T> ops, T prefix) {
        final ListBuilder<T> builder = ops.listBuilder();

        for (TYPE type : input) {
            builder.add(this.element.encodeStart(ops, type));
        }

        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<COLLECTION, T>> decode(DynamicOps<T> ops, T input) {
        return element.listOf().decode(ops, input).map(decoded -> {
            COLLECTION result = collectionConstruction.get();
            result.addAll(decoded.getFirst());
            return Pair.of(result, decoded.getSecond());
        });
    }


}

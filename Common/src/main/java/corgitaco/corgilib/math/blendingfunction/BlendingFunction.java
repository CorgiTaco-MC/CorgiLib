package corgitaco.corgilib.math.blendingfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.core.CorgiLibRegistry;
import corgitaco.corgilib.platform.ModPlatform;

public interface BlendingFunction {
    Codec<BlendingFunction> CODEC = Codec.lazyInitialized(() -> CorgiLibRegistry.BLENDING_FUNCTION.get().byNameCodec().dispatchStable(BlendingFunction::codec, MapCodec::assumeMapUnsafe));

    Codec<? extends BlendingFunction> codec();

    double apply(double factor);

    default double apply(double factor, double min, double max) {
        double range = max - min;
        return min + (range * apply(factor));
    }

    static void register() {
        register("ease_in_out_circ", EaseInOutCirc.CODEC);
        register("ease_out_bounce", EaseOutBounce.CODEC);
        register("ease_out_cubic", EaseOutCubic.CODEC);
        register("ease_out_elastic", EaseOutElastic.CODEC);
        register("ease_in_circ", EaseInCirc.CODEC);
        register("ease_out_quint", EaseOutQuint.CODEC);
    }

    private static void register(String name, Codec<? extends BlendingFunction> function) {
        ModPlatform.PLATFORM.register(CorgiLibRegistry.BLENDING_FUNCTION.get(), name, () -> function);
    }

    record EaseInOutCirc() implements BlendingFunction {
        public static final EaseInOutCirc INSTANCE = new EaseInOutCirc();
        public static final Codec<EaseInOutCirc> CODEC = Codec.unit(() -> INSTANCE);

        @Override
        public double apply(double factor) {
            return BlendingFunctions.easeInOutCirc(factor);
        }

        @Override
        public Codec<? extends BlendingFunction> codec() {
            return CODEC;
        }
    }

    record EaseOutCubic() implements BlendingFunction {
        public static final EaseOutCubic INSTANCE = new EaseOutCubic();
        public static final Codec<EaseOutCubic> CODEC = Codec.unit(() -> INSTANCE);

        @Override
        public double apply(double factor) {
            return BlendingFunctions.easeOutCubic(factor);
        }

        @Override
        public Codec<? extends BlendingFunction> codec() {
            return CODEC;
        }

    }

    record EaseOutBounce() implements BlendingFunction {
        public static final EaseOutBounce INSTANCE = new EaseOutBounce();
        public static final Codec<EaseOutBounce> CODEC = Codec.unit(() -> INSTANCE);

        @Override
        public double apply(double factor) {
            return BlendingFunctions.easeOutBounce(factor);
        }

        @Override
        public Codec<? extends BlendingFunction> codec() {
            return CODEC;
        }
    }

    record EaseOutElastic(double intensity) implements BlendingFunction {
        public static final EaseOutElastic INSTANCE = new EaseOutElastic(10);
        public static final Codec<EaseOutElastic> CODEC = RecordCodecBuilder.create(builder ->
                builder.group(
                        Codec.DOUBLE.fieldOf("intensity").forGetter(EaseOutElastic::intensity)
                ).apply(builder, EaseOutElastic::new)
        );

        @Override
        public double apply(double factor) {
            return BlendingFunctions.easeOutElastic(factor, this.intensity);
        }

        @Override
        public Codec<? extends BlendingFunction> codec() {
            return CODEC;
        }
    }

    record EaseInCirc(double exponent) implements BlendingFunction {
        public static final EaseInCirc INSTANCE = new EaseInCirc(2.0);
        public static final Codec<EaseInCirc> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.DOUBLE.fieldOf("exponent").forGetter(EaseInCirc::exponent)
                ).apply(instance, EaseInCirc::new)
        );

        @Override
        public double apply(double factor) {
            return BlendingFunctions.easeInCirc(factor, this.exponent);
        }

        @Override
        public Codec<? extends BlendingFunction> codec() {
            return CODEC;
        }
    }

    record EaseOutQuint() implements BlendingFunction {
        public static final EaseOutQuint INSTANCE = new EaseOutQuint();
        public static final Codec<EaseOutQuint> CODEC = Codec.unit(() -> INSTANCE);

        @Override
        public double apply(double factor) {
            return BlendingFunctions.easeOutQuint(factor);
        }

        @Override
        public Codec<? extends BlendingFunction> codec() {
            return CODEC;
        }
    }
}

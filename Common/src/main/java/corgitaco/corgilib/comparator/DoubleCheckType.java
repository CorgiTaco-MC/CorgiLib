package corgitaco.corgilib.comparator;

import java.util.function.BiPredicate;

@SuppressWarnings("UnnecessaryUnboxing")
public enum DoubleCheckType {
    GREATER_THAN(((number, number2) -> number.doubleValue() > number2.doubleValue())),
    GREATER_THAN_OR_EQUAL(((number, number2) -> number.doubleValue() >= number2.doubleValue())),
    LESSER_THAN(((number, number2) -> number.doubleValue() < number2.doubleValue())),
    LESSER_THAN_OR_EQUAL(((number, number2) -> number.doubleValue() <= number2.doubleValue())),
    EQUAL(((number, number2) -> number.doubleValue() == number2.doubleValue()));

    private final BiPredicate<Double, Double> numberBiPredicate;

    DoubleCheckType(BiPredicate<Double, Double> numberBiPredicate) {
        this.numberBiPredicate = numberBiPredicate;
    }

    public boolean test(Double first, Double two) {
        return numberBiPredicate.test(first, two);
    }
}

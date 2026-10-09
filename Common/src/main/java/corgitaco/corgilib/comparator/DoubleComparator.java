package corgitaco.corgilib.comparator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class DoubleComparator {
    public static final Codec<DoubleComparator> CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(new DoubleComparator(s));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }, doubleComparator -> doubleComparator.original);

    private final double checkAgainst;
    private final DoubleCheckType check;
    private final String original;

    public DoubleComparator(String s) {
        this.original = s;
        if (s.startsWith("<=")) {
            s = s.replaceAll("<=", "");
            check = DoubleCheckType.LESSER_THAN_OR_EQUAL;
        } else if (s.startsWith(">=")) {
            s = s.replaceAll(">=", "");
            check = DoubleCheckType.GREATER_THAN_OR_EQUAL;
        } else if (s.startsWith(">")) {
            s = s.replaceAll(">", "");
            check = DoubleCheckType.GREATER_THAN;
        } else if (s.startsWith("<")) {
            s = s.replaceAll("<", "");
            check = DoubleCheckType.LESSER_THAN;
        } else if (s.startsWith("=") || s.startsWith("==")) {
            s = s.replaceAll("=", "");
            check = DoubleCheckType.EQUAL;
        } else {
            throw new IllegalArgumentException("Illegal Comparison specified");
        }
        this.checkAgainst = Double.parseDouble(s);
    }

    public boolean check(double number) {
        return check.test(number, checkAgainst);
    }
}

package corgitaco.corgilib.entity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.math.LongPair;

import java.util.List;

public class TimeOfDayCondition implements Condition {

    public static final Codec<TimeOfDayCondition> CODEC = RecordCodecBuilder.<Settings>create(builder -> builder.group(LongPair.createLongPairCodec("min_time", "max_time").listOf().fieldOf("times_of_day").forGetter(Settings::timesOfDay),
            Codec.LONG.validate(length -> length > 0 ? DataResult.success(length) : DataResult.error(() -> "Day length must be positive")).optionalFieldOf("day_length", 24000L).forGetter(Settings::dayLength)
    ).apply(builder, Settings::new)).comapFlatMap(settings -> {
        try {
            return DataResult.success(new TimeOfDayCondition(settings.timesOfDay(), settings.dayLength()));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }, condition -> new Settings(condition.timesOfDay, condition.dayLength));

    private record Settings(List<LongPair> timesOfDay, long dayLength) {
    }

    private final List<LongPair> timesOfDay;
    private final long dayLength;

    public TimeOfDayCondition(List<LongPair> timesOfDay, long dayLength) {
        this.dayLength = dayLength;
        if (timesOfDay.isEmpty()) {
            throw new IllegalArgumentException("No times of day were specified.");
        }
        if (dayLength <= 0) {
            throw new IllegalArgumentException("Day length must be positive.");
        }
        this.timesOfDay = List.copyOf(timesOfDay);
        for (LongPair longPair : this.timesOfDay) {
            if (longPair.getVal1() > this.dayLength || longPair.getVal1() < 0 || longPair.getVal2() > this.dayLength || longPair.getVal2() < 0 || longPair.getVal1() > longPair.getVal2()) {
                throw new IllegalArgumentException("minTime & maxTime must be between 0 & " + dayLength + " and minTime cannot be greater than maxTime! Pair: \"" + longPair + "\" failed.");
            }
        }
    }

    @Override
    public boolean passes(ConditionContext conditionContext) {
        for (LongPair longPair : this.timesOfDay) {
            if (longPair.isInBetween(Math.floorMod(conditionContext.world().getOverworldClockTime(), this.dayLength))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Codec<? extends Condition> codec() {
        return CODEC;
    }
}

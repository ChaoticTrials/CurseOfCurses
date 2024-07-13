package de.melanx.curseofcurses;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ConfigHandler {

    public static final ForgeConfigSpec COMMONG_CONFIG;
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    static {
        init(COMMON_BUILDER);
        COMMONG_CONFIG = COMMON_BUILDER.build();
    }

    public static ForgeConfigSpec.DoubleValue curseChance;
    public static ForgeConfigSpec.BooleanValue cursePerItem;
    public static ForgeConfigSpec.BooleanValue enchantedCurses;
    public static ForgeConfigSpec.BooleanValue curseForSleep;
    public static ForgeConfigSpec.DoubleValue curseForSleepChance;
    public static ForgeConfigSpec.IntValue sleepsInARow;
    public static ForgeConfigSpec.BooleanValue resetRowOnDeath;
    public static ForgeConfigSpec.IntValue curseAmount;
    public static ForgeConfigSpec.IntValue curseTimeStart;
    public static ForgeConfigSpec.IntValue curseTimeEnd;
    public static ForgeConfigSpec.IntValue dangeTimesPerNight;
    public static ForgeConfigSpec.EnumValue<CooldownSetting> cooldownSetting;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistCurses;

    public static void init(ForgeConfigSpec.Builder builder) {
        curseChance = builder.comment("The chance for applying an curse enchantment to an item at midnight. [Default: 0.01 = 1%]")
                .defineInRange("chance.percentage", 0.01, 0, 1);
        cursePerItem = builder.comment("If set to true, each item will be checked to get cursed. Else it will stop after the first cursed item. [Default: false]")
                .define("chance.perItem", false);
        enchantedCurses = builder.comment("If set to true, curses will applied to non-enchanted items, too [Default: false]")
                .define("enchantedItems", false);
        curseAmount = builder.comment("The amount of curses being applied at the \"curse time\" [Default: 1]")
                .defineInRange("curseAmount", 1, 0, Integer.MAX_VALUE);
        curseTimeStart = builder.comment("The earliest time when curses can be applied [Default: 18000]")
                .defineInRange("curseTime.start", 18000, 0, 24000);
        curseTimeEnd = builder.comment("The latest time when curses can be applied. Should be HIGHER then start time [Default: 21000]")
                .defineInRange("curseTime.end", 21000, 0, 24000);
        curseForSleep = builder.comment("If set to true, players get curses if they skip the night [Default: true]")
                .define("cursedSleep.enabled", true);
        curseForSleepChance = builder.comment("Chance to apply a curse when sleeping. [default: 1 = 100%]")
                .defineInRange("cursedSleep.chance", 1D, 0, 1);
        sleepsInARow = builder.comment("Number of sleeps in a row to apply a curse after the given sleeping time. Use 1 to disable. [default: 7]")
                .defineInRange("cursedSleep.row.count", 8, 1, Integer.MAX_VALUE);
        resetRowOnDeath = builder.comment("Should sleep row being reset on death? [default: false]")
                .define("cursedSleep.row.reset", false);
        dangeTimesPerNight = builder.comment("The amount of times within the curse time which can be curse the items")
                .defineInRange("curseTimeAmount", 3, 0, 24000);
        cooldownSetting = builder.comment("In which type of nights should curses be applied")
                .defineEnum("cooldownSetting", CooldownSetting.EVERY_NIGHT);
        blacklistCurses = builder.comment("Curses in this list will not be applied. You can use * as a wildcard.")
                .defineList("blacklistedCurses", Collections.emptyList(), obj -> obj instanceof String);
    }

    public enum CooldownSetting {
        EVERY_NIGHT(i -> true),
        FULL_MOON(i -> i == 0),
        NEW_MOON(i -> i == 4);

        private final Predicate<Integer> predicate;

        CooldownSetting(Predicate<Integer> predicate) {
            this.predicate = predicate;
        }

        public boolean test(int value) {
            return this.predicate.test(value % 8);
        }
    }
}

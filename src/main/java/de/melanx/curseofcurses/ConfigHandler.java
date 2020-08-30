package de.melanx.curseofcurses;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ConfigHandler {

    public static final ForgeConfigSpec SERVER_CONFIG;
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

    static {
        init(SERVER_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static ForgeConfigSpec.DoubleValue curseChance;
    public static ForgeConfigSpec.BooleanValue cursePerItem;
    public static ForgeConfigSpec.BooleanValue enchantedCurses;
    public static ForgeConfigSpec.BooleanValue curseForSleep;
    public static ForgeConfigSpec.IntValue curseAmount;
    public static ForgeConfigSpec.IntValue curseTimeStart;
    public static ForgeConfigSpec.IntValue curseTimeEnd;
    public static ForgeConfigSpec.IntValue dangeTimesPerNight;
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
                .define("cursedSleep", true);
        dangeTimesPerNight = builder.comment("The amount of times within the curse time which can be curse the items")
                .defineInRange("curseTimeAmount", 3, 0, 24000);
        blacklistCurses = builder.comment("Curses in this list will not be applied. You can use * as a wildcard.")
                .defineList("blacklistedCurses", Collections.emptyList(), obj -> obj instanceof String);
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        CurseOfCurses.LOGGER.debug("Loading config file {}", path);

        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();

        configData.load();

        spec.setConfig(configData);
    }
}

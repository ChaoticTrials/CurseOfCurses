package de.melanx.curseofcurses;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class ConfigHandler {

    public static final ForgeConfigSpec SERVER_CONFIG;
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

    static {
        init(SERVER_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static ForgeConfigSpec.DoubleValue curseChance;

    public static void init(ForgeConfigSpec.Builder builder) {
        curseChance = builder.comment("The chance for applying an curse enchantment to an item at midnight. [Default: 0.01 = 1%]")
                .defineInRange("chance", 0.01, 0, 1);
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        CurseOfCurses.LOGGER.debug("Loading config file {}", path);

        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();

        configData.load();

        spec.setConfig(configData);
    }
}

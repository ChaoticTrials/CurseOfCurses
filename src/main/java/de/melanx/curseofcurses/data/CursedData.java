package de.melanx.curseofcurses.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.curseofcurses.ConfigHandler;
import de.melanx.curseofcurses.CurseOfCurses;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CursedData extends SavedData {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(CurseOfCurses.MODID, "curse_of_curses");
    private final List<Integer> possibleTimes;
    private final Random random = new Random();

    public static final Codec<CursedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.listOf().fieldOf("CurseTimes").forGetter(CursedData::getTimes)
            )
            .apply(instance, CursedData::new));

    public CursedData(List<Integer> times) {
        this.possibleTimes = new ArrayList<>(times);
    }

    public static SavedDataType<CursedData> type() {
        return new SavedDataType<>(ID, _ -> new CursedData(List.of()), _ -> CODEC);
    }

    public static CursedData get(ServerLevel level) {
        SavedDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(CursedData.type());
    }

    public List<Integer> getTimes() {
        return this.possibleTimes;
    }

    public void generateTimes() {
        this.possibleTimes.clear();
        for (int i = 0; i < ConfigHandler.dangeTimesPerNight.get(); i++) {
            this.possibleTimes.add(this.random.nextInt(ConfigHandler.curseTimeEnd.get() - ConfigHandler.curseTimeStart.get()) + ConfigHandler.curseTimeStart.get());
        }

        CurseOfCurses.LOGGER.debug("Changing dange times to {}", Arrays.toString(this.possibleTimes.toArray()));
        this.setDirty();
    }
}

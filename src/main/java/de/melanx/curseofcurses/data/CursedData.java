package de.melanx.curseofcurses.data;

import de.melanx.curseofcurses.ConfigHandler;
import de.melanx.curseofcurses.CurseOfCurses;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CursedData extends SavedData {
    private static final String NAME = "curse_of_curses";
    private final List<Integer> possibleTimes = new ArrayList<>();

    private final ServerLevel level;

    public CursedData(ServerLevel level) {
        this.level = level;
        this.generateTimes();
    }

    public static CursedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(nbt -> new CursedData(level).load(nbt), () -> new CursedData(level), NAME);
    }

    public CursedData load(@Nonnull CompoundTag nbt) {
        this.possibleTimes.clear();

        for (Tag tag : nbt.getList("CurseTimes", Constants.NBT.TAG_COMPOUND)) {
            int time = ((CompoundTag) tag).getInt("Time");
            this.possibleTimes.add(time);
        }

        return this;
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        ListTag list = new ListTag();
        for (int time : this.possibleTimes) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Time", time);
            list.add(tag);
        }

        compound.put("CurseTimes", list);
        return compound;
    }

    public List<Integer> getTimes() {
        return this.possibleTimes;
    }

    public void generateTimes() {
        this.possibleTimes.clear();
        for (int i = 0; i < ConfigHandler.dangeTimesPerNight.get(); i++) {
            this.possibleTimes.add(this.level.random.nextInt(ConfigHandler.curseTimeEnd.get() - ConfigHandler.curseTimeStart.get()) + ConfigHandler.curseTimeStart.get());
        }

        CurseOfCurses.LOGGER.debug("Changing dange times to " + Arrays.toString(this.possibleTimes.toArray()));
        this.setDirty();
    }
}

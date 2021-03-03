package de.melanx.curseofcurses.data;

import de.melanx.curseofcurses.ConfigHandler;
import de.melanx.curseofcurses.CurseOfCurses;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CursedData extends WorldSavedData {
    private static final String NAME = "curse_of_curses";
    private final List<Integer> possibleTimes = new ArrayList<>();

    private final ServerWorld world;

    public CursedData(ServerWorld world) {
        super(NAME);
        this.world = world;
        this.generateTimes();
    }

    public static CursedData get(ServerWorld world) {
        DimensionSavedDataManager storage = world.getServer().func_241755_D_().getSavedData();
        return storage.getOrCreate(() -> new CursedData(world), NAME);
    }

    @Override
    public void read(@Nonnull CompoundNBT nbt) {
        this.possibleTimes.clear();

        for (INBT tag : nbt.getList("CurseTimes", Constants.NBT.TAG_COMPOUND)) {
            int time = ((CompoundNBT) tag).getInt("Time");
            this.possibleTimes.add(time);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (int time : this.possibleTimes) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("Time", time);
            list.add(tag);
        }

        nbt.put("CurseTimes", list);
        return nbt;
    }

    public List<Integer> getTimes() {
        return this.possibleTimes;
    }

    public void generateTimes() {
        this.possibleTimes.clear();
        for (int i = 0; i < ConfigHandler.dangeTimesPerNight.get(); i++) {
            this.possibleTimes.add(this.world.rand.nextInt(ConfigHandler.curseTimeEnd.get() - ConfigHandler.curseTimeStart.get()) + ConfigHandler.curseTimeStart.get());
        }

        CurseOfCurses.LOGGER.debug("Changing dange times to " + Arrays.toString(this.possibleTimes.toArray()));
        this.markDirty();
    }
}

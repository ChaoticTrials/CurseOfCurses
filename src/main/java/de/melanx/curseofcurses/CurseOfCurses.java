package de.melanx.curseofcurses;

import de.melanx.curseofcurses.api.CurseUtil;
import de.melanx.curseofcurses.data.CursedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CurseOfCurses.MODID)
public class CurseOfCurses {

    public static final String MODID = "curseofcurses";
    public static final Logger LOGGER = LoggerFactory.getLogger(CurseOfCurses.class);

    public CurseOfCurses(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMONG_CONFIG);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (!level.isClientSide && ConfigHandler.cooldownSetting.get().test(level.getMoonPhase()) && CursedData.get((ServerLevel) level).getTimes().contains((int) level.getDayTime() % 24000)) {
            LOGGER.info("It's dange now.");
            CurseUtil.applyCursesRandomly(player, ConfigHandler.curseChance.get(), ConfigHandler.enchantedCurses.get(), !ConfigHandler.cursePerItem.get());
        }
    }

    @SubscribeEvent
    public void onWorldTick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level && level == level.getServer().overworld() && ConfigHandler.cooldownSetting.get().test(level.getMoonPhase() - 1) && level.getDayTime() % 24000 == 12000) {
            CursedData.get(level).generateTimes();
        }
    }

    @SubscribeEvent
    public void onSleep(PlayerWakeUpEvent event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide && ConfigHandler.curseForSleep.get()) {
            ServerPlayer player = (ServerPlayer) event.getEntity();

            int row = ConfigHandler.sleepsInARow.get();
            if (row == 1) {
                return;
            }

            CompoundTag nbt = player.getPersistentData();
            int i = 1;
            if (nbt.contains("SleepRow")) {
                i = nbt.getInt("SleepRow") + 1;
                nbt.putInt("SleepRow", i);
            } else {
                nbt.putInt("SleepRow", 1);
            }

            if (i >= ConfigHandler.sleepsInARow.get()) {
                nbt.putInt("SleepRow", 0);
                CurseUtil.applyCursesRandomly(player, ConfigHandler.curseForSleepChance.get(), ConfigHandler.enchantedCurses.get());
            }
        }
    }

    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone event) {
        Player newPlayer = event.getEntity();
        CompoundTag newData = newPlayer.getPersistentData();

        Player oldPlayer = event.getOriginal();
        CompoundTag oldData = oldPlayer.getPersistentData();

        if (!ConfigHandler.resetRowOnDeath.get()) {
            newData.putInt("SleepRow", oldData.getInt("SleepRow"));
        }
    }
}

package de.melanx.curseofcurses;

import de.melanx.curseofcurses.api.CurseUtil;
import de.melanx.curseofcurses.data.CursedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CurseOfCurses.MODID)
public class CurseOfCurses {

    public static final String MODID = "curseofcurses";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public CurseOfCurses() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMONG_CONFIG);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigChange);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        BlacklistHandler.initBlacklist();
        CurseUtil.reloadCurses();
    }

    private void onConfigChange(ModConfigEvent event) {
        if (event.getConfig().getModId().equals(MODID)) {
            BlacklistHandler.initBlacklist();
            CurseUtil.reloadCurses();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level level = player.getCommandSenderWorld();

        if (event.phase == TickEvent.Phase.START) {
            if (!level.isClientSide && ConfigHandler.cooldownSetting.get().test(level.getMoonPhase()) && CursedData.get((ServerLevel) level).getTimes().contains((int) level.getDayTime() % 24000)) {
                LOGGER.info("It's dange now.");
                CurseUtil.applyCursesRandomly(player, ConfigHandler.curseChance.get(), ConfigHandler.enchantedCurses.get(), !ConfigHandler.cursePerItem.get());
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START
                && event.level instanceof ServerLevel level
                && level == level.getServer().overworld()
                && ConfigHandler.cooldownSetting.get().test(level.getMoonPhase() - 1)
                && level.getDayTime() % 24000 == 12000) {
            CursedData.get(level).generateTimes();
        }
    }

    @SubscribeEvent
    public void onSleep(PlayerWakeUpEvent event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide && ConfigHandler.curseForSleep.get()) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            CurseUtil.applyCursesRandomly(player, ConfigHandler.curseForSleepChance.get(), ConfigHandler.enchantedCurses.get());

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

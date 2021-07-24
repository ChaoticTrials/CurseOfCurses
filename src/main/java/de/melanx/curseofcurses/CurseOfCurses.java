package de.melanx.curseofcurses;

import de.melanx.curseofcurses.api.CurseUtil;
import de.melanx.curseofcurses.data.CursedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CurseOfCurses.MODID)
public class CurseOfCurses {

    public static final String MODID = "curseofcurses";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public CurseOfCurses() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHandler.SERVER_CONFIG);
        ConfigHandler.loadConfig(ConfigHandler.SERVER_CONFIG, FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve(MODID + "-server.toml"));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigChange);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        BlacklistHandler.initBlacklist();
        CurseUtil.reloadCurses();
    }

    private void onConfigChange(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getModId().equals(MODID)) {
            BlacklistHandler.initBlacklist();
            CurseUtil.reloadCurses();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        World world = player.getEntityWorld();

        if (event.phase == TickEvent.Phase.START) {
            if (!world.isRemote && CursedData.get((ServerWorld) world).getTimes().contains((int) world.getDayTime() % 24000)) {
                LOGGER.info("It's dange now.");
                CurseUtil.applyCursesRandomly(player, ConfigHandler.curseChance.get(), ConfigHandler.enchantedCurses.get(), !ConfigHandler.cursePerItem.get());
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (event.world.getServer() != null && event.world == event.world.getServer().func_241755_D_() && event.world.getDayTime() % 24000 == 12000) {
                CursedData.get((ServerWorld) event.world).generateTimes();
            }
        }
    }

    @SubscribeEvent
    public void onSleep(PlayerWakeUpEvent event) {
        if (!event.getEntityLiving().getEntityWorld().isRemote && ConfigHandler.curseForSleep.get()) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            CurseUtil.applyCursesRandomly(player, ConfigHandler.curseForSleepChance.get(), ConfigHandler.enchantedCurses.get());

            int row = ConfigHandler.sleepsInARow.get();
            if (row == 1) {
                return;
            }

            CompoundNBT nbt = player.getPersistentData();
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
        PlayerEntity newPlayer = event.getPlayer();
        CompoundNBT newData = newPlayer.getPersistentData();

        PlayerEntity oldPlayer = event.getOriginal();
        CompoundNBT oldData = oldPlayer.getPersistentData();

        if (!ConfigHandler.resetRowOnDeath.get()) {
            newData.putInt("SleepRow", oldData.getInt("SleepRow"));
        }
    }
}

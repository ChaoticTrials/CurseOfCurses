package de.melanx.curseofcurses;

import de.melanx.curseofcurses.api.CurseUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod(CurseOfCurses.MODID)
public class CurseOfCurses {

    public static final String MODID = "curseofcurses";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    private final List<Integer> possibleTimes = new ArrayList<>();
    public CurseOfCurses instance;

    public CurseOfCurses() {
        instance = this;
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHandler.SERVER_CONFIG);
        ConfigHandler.loadConfig(ConfigHandler.SERVER_CONFIG, FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve(MODID + "-server.toml"));
        MinecraftForge.EVENT_BUS.register(CurseUtil.class);
        MinecraftForge.EVENT_BUS.addListener(this::onServerFinished);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onServerFinished(FMLServerStartedEvent event) {
        possibleTimes.clear();
        for (int i = 0; i < 3; i++) {
            possibleTimes.add(CurseUtil.random.nextInt(3000) + 18000);
        }
        LOGGER.info("Changing dange times to " + Arrays.toString(possibleTimes.toArray()));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        World world = player.getEntityWorld();

        if (event.phase == TickEvent.Phase.START) {
            if (!world.isRemote && possibleTimes.contains((int) world.getDayTime() % 24000)) {
                LOGGER.info("It's dange now.");
                CurseUtil.applyCursesRandomly(player, ConfigHandler.curseChance.get(), ConfigHandler.enchantedCurses.get());
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (!event.world.isRemote && event.world.getDayTime() % 24000 == 12000) {
                possibleTimes.clear();
                for (int i = 0; i < 3; i++) {
                    possibleTimes.add(CurseUtil.random.nextInt(3000) + 18000);
                }
                LOGGER.info("Changing dange times to " + Arrays.toString(possibleTimes.toArray()));
            }
        }
    }
}

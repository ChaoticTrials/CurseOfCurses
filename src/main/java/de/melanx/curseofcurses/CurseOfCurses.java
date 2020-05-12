package de.melanx.curseofcurses;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CurseOfCurses.MODID)
public class CurseOfCurses {

    public static final String MODID = "curseofcurses";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    public CurseOfCurses instance;

    public CurseOfCurses() {
        instance = this;

        MinecraftForge.EVENT_BUS.register(this);
    }
}

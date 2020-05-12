package de.melanx.curseofcurses;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
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
import java.util.List;

@Mod(CurseOfCurses.MODID)
public class CurseOfCurses {

    public static final String MODID = "curseofcurses";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    private final List<Enchantment> curses = new ArrayList<>();
    public CurseOfCurses instance;

    public CurseOfCurses() {
        instance = this;
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHandler.SERVER_CONFIG);
        ConfigHandler.loadConfig(ConfigHandler.SERVER_CONFIG, FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve(MODID + "-server.toml"));
        MinecraftForge.EVENT_BUS.addListener(this::onServerFinished);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static boolean canEnchant(Enchantment enchantment, ItemStack stack) {
        return !(enchantment == null || !enchantment.isCurse() || hasEnchantment(enchantment, stack) || !enchantment.canApply(stack));
    }

    private static boolean hasEnchantment(Enchantment enchantment, ItemStack stack) {
        ListNBT enchantments = stack.getEnchantmentTagList();
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundNBT nbt = enchantments.getCompound(i);
            String resourceLocation = nbt.getString("id");
            if (new ResourceLocation(resourceLocation).equals(enchantment.getRegistryName())) return true;
        }
        return false;
    }

    public void onServerFinished(FMLServerStartedEvent event) {
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment.isCurse()) {
                curses.add(enchantment);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        World world = player.getEntityWorld();

        if (event.phase == TickEvent.Phase.START)
            if (!world.isRemote && world.getDayTime() == 18000) {
                PlayerInventory inventory = player.inventory;
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem().isEnchantable(stack) && stack.isEnchanted() && ConfigHandler.curseChance.get() > world.getRandom().nextDouble()) {
                        Enchantment curse = Enchantments.AQUA_AFFINITY;
                        List<Enchantment> curses1 = new ArrayList<>(curses);
                        while (!canEnchant(curse, stack)) {
                            int index = world.getRandom().nextInt(curses1.size());
                            curse = curses1.get(index);
                            curses1.remove(index);
                            if (curses1.isEmpty()) {
                                curse = null;
                                break;
                            }
                        }
                        if (curse != null) {
                            stack.addEnchantment(curse, curse.getMaxLevel());
                            player.sendStatusMessage(new TranslationTextComponent("curseofcurses.message", stack.getDisplayName(), curse.getDisplayName(curse.getMaxLevel())), false);
                            player.playSound(SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.AMBIENT, 0.5F, 0.1F);
                        }
                    }
                }
            }
    }
}

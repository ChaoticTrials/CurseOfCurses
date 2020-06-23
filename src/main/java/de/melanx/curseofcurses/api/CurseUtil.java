package de.melanx.curseofcurses.api;

import de.melanx.curseofcurses.ConfigHandler;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CurseUtil {

    public static final Random random = new Random();
    private static final List<Enchantment> curses = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean canEnchant(Enchantment enchantment, ItemStack stack) {
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

    public static void applyCursesRandomly(PlayerEntity player, double chance) {
        applyCursesRandomly(player, chance, false);
    }

    public static void applyCursesRandomly(PlayerEntity player, double chance, boolean ignoreEnchantments) {
        PlayerInventory inventory = player.inventory;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem().isEnchantable(stack) && (stack.isEnchanted() || ignoreEnchantments) && chance > random.nextDouble()) {
                Enchantment curse = Enchantments.AQUA_AFFINITY;
                List<Enchantment> curses1 = new ArrayList<>(curses);
                while (!CurseUtil.canEnchant(curse, stack)) {
                    int index = random.nextInt(curses1.size());
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

    @SubscribeEvent
    public static void onServerFinished(FMLServerStartedEvent event) {
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment.isCurse()) {
                curses.add(enchantment);
            }
        }
        LOGGER.info(curses.size() + " curses loaded.");
    }

}

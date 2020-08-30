package de.melanx.curseofcurses.api;

import de.melanx.curseofcurses.BlacklistHandler;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
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
        applyCursesRandomly(player, chance, false, true);
    }

    public static void applyCursesRandomly(PlayerEntity player, double chance, boolean ignoreEnchantments) {
        applyCursesRandomly(player, chance, ignoreEnchantments, true);
    }

    public static void applyCursesRandomly(PlayerEntity player, double chance, boolean ignoreEnchantments, boolean oneItemOnly) {
        PlayerInventory inv = player.inventory;
        List<ItemStack> inventory = new ArrayList<>();
        inventory.addAll(inv.armorInventory);
        inventory.addAll(inv.mainInventory);
        inventory.addAll(inv.offHandInventory);
        Collections.shuffle(inventory);
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && stack.getItem().isEnchantable(stack) && (stack.isEnchanted() || ignoreEnchantments) && chance > random.nextDouble()) {
                Enchantment curse = Enchantments.AQUA_AFFINITY;
                for (int j = 0; j < ConfigHandler.curseAmount.get(); j++) {
                    List<Enchantment> curses1 = new ArrayList<>(curses);
                    while (!CurseUtil.canEnchant(curse, stack)) {
                        if (curses1.isEmpty()) {
                            curse = null;
                            break;
                        }
                        int index = random.nextInt(curses1.size());
                        curse = curses1.get(index);
                        curses1.remove(index);
                    }
                    if (curse != null) {
                        stack.addEnchantment(curse, curse.getMaxLevel());
                        player.sendStatusMessage(new TranslationTextComponent("curseofcurses.message", stack.getDisplayName(), curse.getDisplayName(curse.getMaxLevel())), false);
                        player.playSound(SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.AMBIENT, 0.5F, 0.1F);
                    }
                }
                if (curse != null && curse != Enchantments.AQUA_AFFINITY && oneItemOnly) {
                    break;
                }
            }
        }
    }

    public static void reloadCurses() {
        curses.clear();
        if (!BlacklistHandler.BLACKLISTED_CURSES.isEmpty()) LOGGER.info("Curses on blacklist: ");
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment.isCurse()) {
                //noinspection ConstantConditions
                if (!BlacklistHandler.BLACKLISTED_CURSES.contains(enchantment.getRegistryName().toString())) {
                    curses.add(enchantment);
                } else {
                    LOGGER.info(enchantment.getRegistryName().toString());
                }
            }
        }
        LOGGER.info(curses.size() + " curses loaded.");
    }
}

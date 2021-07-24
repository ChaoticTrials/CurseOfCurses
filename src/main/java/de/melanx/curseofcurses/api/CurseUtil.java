package de.melanx.curseofcurses.api;

import de.melanx.curseofcurses.BlacklistHandler;
import de.melanx.curseofcurses.ConfigHandler;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CurseUtil {

    public static final Random RANDOM = new Random();
    private static final List<Enchantment> CURSES = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean canEnchant(Enchantment enchantment, ItemStack stack) {
        return !(enchantment == null || !enchantment.isCurse() || hasEnchantment(enchantment, stack) || !enchantment.canEnchant(stack));
    }

    private static boolean hasEnchantment(Enchantment enchantment, ItemStack stack) {
        ListTag enchantments = stack.getEnchantmentTags();
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag nbt = enchantments.getCompound(i);
            String resourceLocation = nbt.getString("id");
            if (new ResourceLocation(resourceLocation).equals(enchantment.getRegistryName())) return true;
        }
        return false;
    }

    public static void applyCursesRandomly(Player player, double chance) {
        applyCursesRandomly(player, chance, false, true);
    }

    public static void applyCursesRandomly(Player player, double chance, boolean ignoreEnchantments) {
        applyCursesRandomly(player, chance, ignoreEnchantments, true);
    }

    public static void applyCursesRandomly(Player player, double chance, boolean ignoreEnchantments, boolean oneItemOnly) {
        Inventory inv = player.getInventory();
        List<ItemStack> inventory = new ArrayList<>();
        inventory.addAll(inv.armor);
        inventory.addAll(inv.items);
        inventory.addAll(inv.offhand);
        Collections.shuffle(inventory);
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && stack.getItem().isEnchantable(stack) && (!stack.isEnchanted() || ignoreEnchantments) && chance > Math.random()) {
                Enchantment curse = Enchantments.AQUA_AFFINITY;
                for (int j = 0; j < ConfigHandler.curseAmount.get(); j++) {
                    List<Enchantment> curses1 = new ArrayList<>(CURSES);
                    while (!CurseUtil.canEnchant(curse, stack)) {
                        if (curses1.isEmpty()) {
                            curse = null;
                            break;
                        }
                        int index = RANDOM.nextInt(curses1.size());
                        curse = curses1.get(index);
                        curses1.remove(index);
                    }
                    if (curse != null) {
                        stack.enchant(curse, curse.getMaxLevel());
                        player.displayClientMessage(new TranslatableComponent("curseofcurses.message", stack.getHoverName(), curse.getFullname(curse.getMaxLevel())), false);
                        player.playNotifySound(SoundEvents.WITHER_AMBIENT, SoundSource.AMBIENT, 0.5F, 0.1F);
                    }
                }
                if (curse != null && curse != Enchantments.AQUA_AFFINITY && oneItemOnly) {
                    break;
                }
            }
        }
    }

    public static void reloadCurses() {
        CURSES.clear();
        if (!BlacklistHandler.BLACKLISTED_CURSES.isEmpty()) LOGGER.info("Curses on blacklist: ");
        //noinspection deprecation
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment.isCurse()) {
                //noinspection ConstantConditions
                if (!BlacklistHandler.BLACKLISTED_CURSES.contains(enchantment.getRegistryName().toString())) {
                    CURSES.add(enchantment);
                } else {
                    LOGGER.info(enchantment.getRegistryName().toString());
                }
            }
        }
        LOGGER.info(CURSES.size() + " curses loaded.");
    }
}

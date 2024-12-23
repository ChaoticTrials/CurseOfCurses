package de.melanx.curseofcurses.api;

import de.melanx.curseofcurses.ConfigHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CurseUtil {

    public static final Random RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean canEnchant(Optional<Holder.Reference<Enchantment>> enchantment, ItemStack stack) {
        return !(enchantment.isEmpty() || !enchantment.get().isBound() || !enchantment.get().is(EnchantmentTags.CURSE) || hasEnchantment(enchantment.get(), stack) || enchantment.get().value().canEnchant(stack));
    }

    private static boolean hasEnchantment(Holder<Enchantment> enchantment, ItemStack stack) {
        return stack.getTagEnchantments().keySet().contains(enchantment);
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
        Registry<Enchantment> enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && stack.isEnchantable() && (!stack.isEnchanted() || ignoreEnchantments) && chance > Math.random()) {
                Optional<Holder.Reference<Enchantment>> curse = Optional.empty();
                for (int j = 0; j < ConfigHandler.curseAmount.get(); j++) {
                    List<ResourceLocation> curses = new ArrayList<>(enchantments.keySet());
                    while (!CurseUtil.canEnchant(curse, stack)) {
                        if (curses.isEmpty()) {
                            curse = Optional.empty();
                            break;
                        }

                        int index = RANDOM.nextInt(curses.size());
                        curse = enchantments.get(curses.get(index));
                        curses.remove(index);
                    }

                    curse.ifPresent(enchantment -> {
                        stack.enchant(enchantment, enchantment.value().getMaxLevel());
                        player.displayClientMessage(Component.translatable("curseofcurses.message", stack.getHoverName(), Enchantment.getFullname(enchantment, enchantment.value().getMaxLevel())), false);
                        player.playNotifySound(SoundEvents.WITHER_AMBIENT, SoundSource.AMBIENT, 0.5F, 0.1F);
                    });
                }

                if (curse.isPresent() && oneItemOnly) {
                    break;
                }
            }
        }
    }
}

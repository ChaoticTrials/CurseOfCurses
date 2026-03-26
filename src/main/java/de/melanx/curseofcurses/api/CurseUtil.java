package de.melanx.curseofcurses.api;

import de.melanx.curseofcurses.ConfigHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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

    public static boolean canEnchant(Optional<Holder<Enchantment>> enchantment, ItemStack stack) {
        return enchantment.isPresent() && enchantment.get().isBound() && enchantment.get().is(EnchantmentTags.CURSE) && !hasEnchantment(enchantment.get(), stack) && stack.supportsEnchantment(enchantment.get());
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
        List<ItemStack> inventory = new ArrayList<>(inv.getNonEquipmentItems());
        Inventory.EQUIPMENT_SLOT_MAPPING.keySet().forEach(slot -> inventory.add(inv.getItem(slot)));
        Collections.shuffle(inventory);

        Registry<Enchantment> enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> curses = new ArrayList<>();
        for (Holder<Enchantment> enchantmentHolder : enchantments.asHolderIdMap()) {
            if (enchantmentHolder.is(EnchantmentTags.CURSE)) {
                curses.add(enchantmentHolder);
            }
        }
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && stack.isEnchantable() && (!stack.isEnchanted() || ignoreEnchantments) && chance > Math.random()) {
                Optional<Holder<Enchantment>> curse = Optional.empty();
                for (int j = 0; j < ConfigHandler.curseAmount.get(); j++) {
                    List<Holder<Enchantment>> toCheck = new ArrayList<>(curses);
                    Collections.shuffle(toCheck);
                    while (!CurseUtil.canEnchant(curse, stack)) {
                        if (toCheck.isEmpty()) {
                            curse = Optional.empty();
                            break;
                        }

                        int index = RANDOM.nextInt(toCheck.size());
                        curse = Optional.of(toCheck.get(index));
                        toCheck.remove(index);
                    }

                    curse.ifPresent(enchantment -> {
                        stack.enchant(enchantment, enchantment.value().getMaxLevel());
                        player.sendSystemMessage(Component.translatable("curseofcurses.message", stack.getHoverName(), Enchantment.getFullname(enchantment, enchantment.value().getMaxLevel())));
                        player.playSound(SoundEvents.WITHER_AMBIENT, 0.5F, 0.1F);
                    });
                }

                if (curse.isPresent() && oneItemOnly) {
                    break;
                }
            }
        }
    }
}

package de.melanx.curseofcurses;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BlacklistHandler {

    public static List<String> BLACKLISTED_CURSES;

    public static void initBlacklist() {
        String[] forbiddenCurses = ConfigHandler.blacklistCurses.get().toArray(new String[0]);
        BLACKLISTED_CURSES = new ArrayList<>();

        for (String s : forbiddenCurses) {
            if (s.contains("*")) {
                Pattern regex = Pattern.compile("^" + s.replace("*", ".*") + "$");
                for (int k = 0; k < ForgeRegistries.ENCHANTMENTS.getKeys().size(); k++) {
                    Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue((ResourceLocation) ForgeRegistries.ENCHANTMENTS.getKeys().toArray()[k]);
                    //noinspection ConstantConditions
                    if (enchantment != null && enchantment.isCurse() && ForgeRegistries.ENCHANTMENTS.getKey(enchantment).toString().matches(regex.pattern())) {
                        //noinspection ConstantConditions
                        BLACKLISTED_CURSES.add(ForgeRegistries.ENCHANTMENTS.getKey(enchantment).toString());
                    }
                }
            } else {
                BLACKLISTED_CURSES.add(s);
            }
        }
    }

}

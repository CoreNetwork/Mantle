package us.corenetwork.mantle.util;

import net.minecraft.server.v1_8_R1.Item;
import net.minecraft.server.v1_8_R1.MobEffect;
import net.minecraft.server.v1_8_R1.MobEffectList;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

/**
 * Created by Matej on 2.12.2014.
 */
public class MinecraftNames
{
    public static Integer getEnchantmentId(String name)
    {
        net.minecraft.server.v1_8_R1.Enchantment nmsEnchantment = net.minecraft.server.v1_8_R1.Enchantment.getByName(name);
        if (nmsEnchantment == null)
            return null;

        return nmsEnchantment.id;
    }

    public static Integer getMaterialId(String name)
    {
        Item item = Item.d(name);
        if (item == null)
            return null;

        return Item.getId(item);
    }

    public static Integer getPotionEffectId(String name)
    {
        MobEffectList effect = net.minecraft.server.v1_8_R1.MobEffectList.b(name);
        if (effect == null)
            return null;

        return effect.getId();
    }
}

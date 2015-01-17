package us.corenetwork.mantle.perks;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.nanobot.NanobotUtil;

public class PerksUtil
{
    public static final String GOLD_START = new String(new char[] {ChatColor.COLOR_CHAR, ChatColor.GOLD.getChar()});

    /**
     * Check if specified Bukkit ItemStack is perk item for donators or not
     */
    public static boolean isPerkItem(ItemStack bukkitStack)
    {
        return isPerkItem(NanobotUtil.getInternalNMSStack(bukkitStack));
    }

    /**
     * Check if specified NMS ItemStack is perk item for donators or not
     */
    public static boolean isPerkItem(net.minecraft.server.v1_8_R1.ItemStack nmsStack)
    {
        String name = NanobotUtil.getStackName(nmsStack);
        if (name != null && name.startsWith(GOLD_START))
            return true;

        return false;
    }
}
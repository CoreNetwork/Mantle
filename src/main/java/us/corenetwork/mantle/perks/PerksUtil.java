package us.corenetwork.mantle.perks;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagList;
import net.minecraft.server.v1_8_R1.TileEntityBanner;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.block.CraftBlock;
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

    public static boolean isPerkBlock(Block block)
    {
        if (block.getType() == Material.SIGN)
        {

        }
        else if (block.getType() == Material.BANNER)
        {
            CraftBlock craftBlock = (CraftBlock) block;
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
            TileEntityBanner bannerTileEntity = (TileEntityBanner) nmsWorld.getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));

            NBTTagList patterns = bannerTileEntity.patterns;
            return BannerRecipeProxy.doesPatternListContainPerkPatterns(patterns);

        }

        return false;
    }
}

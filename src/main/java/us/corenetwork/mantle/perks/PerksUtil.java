package us.corenetwork.mantle.perks;

import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Items;
import net.minecraft.server.v1_8_R2.NBTTagByte;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagList;
import net.minecraft.server.v1_8_R2.NBTTagString;
import net.minecraft.server.v1_8_R2.TileEntityBanner;
import net.minecraft.server.v1_8_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.block.CraftBlock;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.nanobot.NanobotUtil;
import us.corenetwork.mantle.util.SignUtil;

public class PerksUtil
{
    public static final String GOLD_START = new String(new char[] {ChatColor.COLOR_CHAR, ChatColor.GOLD.getChar()});

    /**
     * Check if specified Bukkit ItemStack has golden name
     */
    public static boolean hasGoldenName(ItemStack bukkitStack)
    {
        return hasGoldenName(NanobotUtil.getInternalNMSStack(bukkitStack));
    }

    /**
     * Check if specified NMS ItemStack has golden name
     */
    public static boolean hasGoldenName(net.minecraft.server.v1_8_R2.ItemStack nmsStack)
    {
        String name = NanobotUtil.getStackName(nmsStack);
        if (name != null && name.startsWith(GOLD_START))
            return true;

        return false;
    }

    /**
     * Check if block is meant to only be placed by subscribers on subscriber's claims.
     */
    public static boolean isPerkBlock(Block block)
    {
        if (block.getType() == Material.SIGN)
        {
            Sign sign = (Sign) block.getState();

            return SignUtil.doesSignHaveColors(sign);
        }
        else if (block.getType() == Material.BANNER)
        {
            CraftBlock craftBlock = (CraftBlock) block;
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
            TileEntityBanner bannerTileEntity = (TileEntityBanner) nmsWorld.getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));

            NBTTagList patterns = bannerTileEntity.patterns;
            return doesPatternListContainPerkPatterns(patterns);
        }

        return false;
    }

    /**
     * Checks if banner type is supposed to only be usable by perk users
     * @param itemTag NBT tag of the banner item
     */
    public static boolean isSupposedToBePerkBannerItem(NBTTagCompound itemTag)
    {
        if (itemTag == null)
            return false;

        NBTTagCompound blockEntityTag = (NBTTagCompound) itemTag.get("BlockEntityTag");

        return isSupposedToBePerkBanner(blockEntityTag);
    }

    /**
     * Checks if banner type is supposed to only be usable on perk users
     * @param blockEntityTag NBT tag of the banner block
     */
    public static boolean isSupposedToBePerkBanner(NBTTagCompound blockEntityTag)
    {
        if (blockEntityTag == null)
            return false;

        NBTTagList patterns = (NBTTagList) blockEntityTag.get("Patterns");
        if (patterns == null)
            return false;

        return doesPatternListContainPerkPatterns(patterns);
    }

    /**
     * Check if list of patterns contains patterns reserved for subscribers
     * @param patterns NBTTagList containing string patterns
     */
    public static boolean doesPatternListContainPerkPatterns(NBTTagList patterns)
    {
        for (int i = 0; i < patterns.size(); i++)
        {
            NBTTagCompound pattern = patterns.get(i);
            String patternType = pattern.getString("Pattern");

            //cbo = Curly border, bri = Bricks
            if ("cbo".equals(patternType) || "bri".equals(patternType))
                return true;
        }

        return false;
    }

    /**
     * Add gold name and lore to banner
     */
    public static void addLoreToBanner(net.minecraft.server.v1_8_R2.ItemStack banner)
    {
        String customName = GOLD_START.concat(Items.BANNER.a(banner));

        NBTTagCompound itemTag = banner.getTag();

        NBTTagCompound displayTag = itemTag.getCompound("display");
        displayTag.setString("Name", customName);

        NBTTagList lore = new NBTTagList();
        for (String loreLine : PerksSettings.BANNER_LORE.stringList())
        {
            lore.add(new NBTTagString(Util.applyColors(loreLine)));
        }

        displayTag.set("Lore", lore);
        itemTag.set("display", displayTag);
    }

    /**
     * Checks if armor stand is supposed to only be usable by perk users
     * @param itemTag NBT tag of the armor stand item
     */
    public static boolean isSupposedToBePerkArmorStandItem(NBTTagCompound itemTag)
    {
        if (itemTag == null)
            return false;

        NBTTagCompound blockEntityTag = (NBTTagCompound) itemTag.get("EntityTag");
        if (blockEntityTag == null)
            return false;

        return isSupposedToBePerkArmorStand(blockEntityTag);
    }


    /**
     * Checks if armor stand is supposed to only be usable on perk users
     * @param entityTag NBT tag of the armor stand entity
     */
    public static boolean isSupposedToBePerkArmorStand(NBTTagCompound entityTag)
    {
        NBTTagByte showArms = (NBTTagByte) entityTag.get("ShowArms");
        if (showArms == null)
            return false;

        return showArms.f() == 1;
    }

    /**
     * Checks if armor stand is supposed to only be usable on perk users
     * @param armorStand Bukkit ArmorStand entity
     */
    public static boolean isSupposedToBePerkArmorStand(ArmorStand armorStand)
    {
        return armorStand.hasArms();
    }

    /**
     * Checks if skull is supposed to be used only by perk users
     * @param itemTag NBT tag of the skull
     */
    public static boolean iSupposedToBePerkSkullItem(NBTTagCompound itemTag)
    {
        if (itemTag == null)
            return false;

        return itemTag.hasKey("SkullOwner");
    }
}

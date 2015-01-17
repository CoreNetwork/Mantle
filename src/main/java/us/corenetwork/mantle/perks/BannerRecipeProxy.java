package us.corenetwork.mantle.perks;

import java.util.Iterator;
import java.util.List;
import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.CraftingManager;
import net.minecraft.server.v1_8_R1.EnumBannerPatternType;
import net.minecraft.server.v1_8_R1.IRecipe;
import net.minecraft.server.v1_8_R1.InventoryCrafting;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.Items;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagList;
import net.minecraft.server.v1_8_R1.NBTTagString;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.inventory.Recipe;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.util.ReflectionUtils;

public class BannerRecipeProxy implements IRecipe
{
    private IRecipe originalRecipe;

    public BannerRecipeProxy(IRecipe originalRecipe)
    {
        this.originalRecipe = originalRecipe;
    }


    @Override
    public boolean a(InventoryCrafting inventoryCrafting, World world)
    {
        return originalRecipe.a(inventoryCrafting, world);
    }

    /**
     * Craft
     */
    @Override
    public ItemStack a(InventoryCrafting inventoryCrafting)
    {
        ItemStack resultBanner = originalRecipe.a(inventoryCrafting);

        //Only perform actions if banner is not already marked as perk item and if banner is special type
        if (!PerksUtil.isPerkItem(resultBanner) && isSupposedToBePerkBanner(resultBanner.getTag()))
        {
            turnIntoPerkBanner(resultBanner);
        }

        return resultBanner;
    }

    @Override
    public int a()
    {
        return originalRecipe.a();
    }

    @Override
    public ItemStack b()
    {
        return originalRecipe.b();
    }

    @Override
    public ItemStack[] b(InventoryCrafting inventoryCrafting)
    {
        return originalRecipe.b(inventoryCrafting);
    }

    @Override
    public Recipe toBukkitRecipe()
    {
        return originalRecipe.toBukkitRecipe();
    }

    @Override
    public List<ItemStack> getIngredients()
    {
        return originalRecipe.getIngredients();
    }

    /**
     * Checks if banner type is supposed to only be usable by perk users
     * @param itemTag NBT tag of the banner item
     */
    public static boolean isSupposedToBePerkBannerItem(NBTTagCompound itemTag)
    {
        NBTTagCompound blockEntityTag = (NBTTagCompound) itemTag.get("BlockEntityTag");
        if (blockEntityTag == null)
            return false;

        return isSupposedToBePerkBanner(blockEntityTag);
    }

    /**
     * Checks if banner type is supposed to only be usable on perk users
     * @param blockEntityTag NBT tag of the banner block
     */
    public static boolean isSupposedToBePerkBanner(NBTTagCompound blockEntityTag)
    {
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

    public static void turnIntoPerkBanner(ItemStack banner)
    {
        String customName = PerksUtil.GOLD_START.concat(Items.BANNER.a(banner));

        NBTTagCompound itemTag = banner.getTag();

        NBTTagCompound displayTag = itemTag.getCompound("display");
        displayTag.setString("Name", customName);

        NBTTagList lore = displayTag.getList("Lore", 8); //8 = String type
        for (String loreLine : PerksSettings.BANNER_LORE.stringList())
        {
            lore.add(new NBTTagString(Util.applyColors(loreLine)));
        }

        displayTag.set("Lore", lore);
        itemTag.set("display", displayTag);
    }

    public static void inject()
    {
        List<IRecipe> recipes = CraftingManager.getInstance().recipes;

        IRecipe original = null;
        Iterator<IRecipe> iterator = recipes.iterator();
        while (iterator.hasNext())
        {
            IRecipe recipe = iterator.next();

            if (recipe.getClass().getName().equals("net.minecraft.server.v1_8_R1.RecipesBannerInnerClass2"))
            {
                original = recipe;
                iterator.remove();
                break;
            }
        }

        if (original == null)
        {
            MLog.severe("Failed to inject custom banner recipes!");
            return;
        }

        recipes.add(new BannerRecipeProxy(original));

        //Change recipes that contain bricks and vines
        ReflectionUtils.set(EnumBannerPatternType.CURLY_BORDER, "Q", new ItemStack(Blocks.TALLGRASS, 1, 1));
        ReflectionUtils.set(EnumBannerPatternType.BRICKS, "Q", new ItemStack(Blocks.STONEBRICK));
    }
}

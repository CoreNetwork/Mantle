package us.corenetwork.mantle.perks;

import java.util.Iterator;
import java.util.List;
import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.CraftingManager;
import net.minecraft.server.v1_8_R1.EnumBannerPatternType;
import net.minecraft.server.v1_8_R1.IRecipe;
import net.minecraft.server.v1_8_R1.InventoryCrafting;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.inventory.Recipe;
import us.corenetwork.mantle.MLog;
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
        if (!PerksUtil.hasGoldenName(resultBanner) && PerksUtil.isSupposedToBePerkBannerItem(resultBanner.getTag()))
        {
            PerksUtil.addLoreToBanner(resultBanner);
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

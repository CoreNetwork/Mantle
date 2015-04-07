package us.corenetwork.mantle.perks;

import java.util.Iterator;
import java.util.List;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.CraftingManager;
import net.minecraft.server.v1_8_R2.IRecipe;
import net.minecraft.server.v1_8_R2.InventoryCrafting;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.Items;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagList;
import net.minecraft.server.v1_8_R2.NBTTagString;
import net.minecraft.server.v1_8_R2.TileEntityBanner;
import net.minecraft.server.v1_8_R2.World;
import org.bukkit.inventory.Recipe;
import us.core_network.cornel.common.Messages;
import us.core_network.cornel.custom.PerksUtil;
import us.core_network.cornel.java.ReflectionUtils;
import us.corenetwork.mantle.MLog;

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
            addLoreToBanner(resultBanner);
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

            if (recipe.getClass().getName().equals("net.minecraft.server.v1_8_R2.RecipesBanner$DuplicateRecipe"))
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
        ReflectionUtils.set(TileEntityBanner.EnumBannerPatternType.CURLY_BORDER, "Q", new ItemStack(Blocks.TALLGRASS, 1, 1));
        ReflectionUtils.set(TileEntityBanner.EnumBannerPatternType.BRICKS, "Q", new ItemStack(Blocks.STONEBRICK));
    }

    /**
     * Add gold name and lore to banner
     */
    public static void addLoreToBanner(net.minecraft.server.v1_8_R2.ItemStack banner)
    {
        String customName = PerksUtil.GOLD_START.concat(Items.BANNER.a(banner));

        NBTTagCompound itemTag = banner.getTag();

        NBTTagCompound displayTag = itemTag.getCompound("display");
        displayTag.setString("Name", customName);

        NBTTagList lore = new NBTTagList();
        for (String loreLine : PerksSettings.BANNER_LORE.stringList())
        {
            lore.add(new NBTTagString(Messages.applyFormattingCodes(loreLine)));
        }

        displayTag.set("Lore", lore);
        itemTag.set("display", displayTag);
    }
}

package us.corenetwork.mantle.perks;

import io.netty.util.internal.RightPaddedReference;
import net.minecraft.server.v1_8_R1.IRecipe;
import net.minecraft.server.v1_8_R1.InventoryCrafting;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.Items;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagFloat;
import net.minecraft.server.v1_8_R1.NBTTagList;
import net.minecraft.server.v1_8_R1.ShapelessRecipes;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import us.corenetwork.mantle.nanobot.NanobotUtil;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;
import us.corenetwork.mantle.util.InventoryUtil;

public class CustomArmorStandRecipe extends ShapelessRecipes implements IRecipe
{
    public CustomArmorStandRecipe()
    {
        //Recipe is dynamic, so lets just pass some bogus info to bukkit class which is meant for static recipes
        super(new ItemStack(Items.ARMOR_STAND, 0, 0), java.util.Arrays.asList(new ItemStack(Items.STICK, 2)));
    }

    /**
     * @return Does recipe matches current crafting area
     */
    @Override
    public boolean a(InventoryCrafting inventoryCrafting, World world)
    {
        ItemStack armorStand = inventoryCrafting.getItem(4);

        //Recipe is not valid if there is no armor stand in the middle
        if (armorStand == null || armorStand.getItem() != Items.ARMOR_STAND)
            return false;

        if (PerksUtil.isPerkItem(armorStand))
            return false; //Special armor stands cannot be re-crafted.

        int numberOfSticksOnLeft = 0;
        if (InventoryUtil.isItemTypeOnSlot(inventoryCrafting, 0, Items.STICK))
            numberOfSticksOnLeft++;
        if (InventoryUtil.isItemTypeOnSlot(inventoryCrafting, 3, Items.STICK))
            numberOfSticksOnLeft++;
        if (InventoryUtil.isItemTypeOnSlot(inventoryCrafting, 6, Items.STICK))
            numberOfSticksOnLeft++;

        if (numberOfSticksOnLeft != 1 && numberOfSticksOnLeft != 2) //Number of sticks must be exactly 1 or 2 on either side
            return false;

        int numberOfSticksOnRight = 0;
        if (InventoryUtil.isItemTypeOnSlot(inventoryCrafting, 2, Items.STICK))
            numberOfSticksOnRight++;
        if (InventoryUtil.isItemTypeOnSlot(inventoryCrafting, 5, Items.STICK))
            numberOfSticksOnRight++;
        if (InventoryUtil.isItemTypeOnSlot(inventoryCrafting, 8, Items.STICK))
            numberOfSticksOnRight++;

        if (numberOfSticksOnRight != 1 && numberOfSticksOnRight != 2) //Number of sticks must be exactly 1 or 2 on either side
            return false;

        return true;
    }

    /**
     * Craft
     */
    @Override
    public ItemStack a(InventoryCrafting inventoryCrafting)
    {
        ItemStack armorStand = new ItemStack(Items.ARMOR_STAND, 1);
        NBTTagCompound tag = LoadCommand.load(PerksSettings.SPECIAL_ARMOR_STAND_NANOBOT_FILE.string());

        float leftHandOrientation = getOrientationFromStickPosition(inventoryCrafting, 0, 3, 6);
        float righHandOrientation = getOrientationFromStickPosition(inventoryCrafting, 2, 5, 8);

        NBTTagCompound entityTag = new NBTTagCompound();
        NBTTagCompound poseTag = new NBTTagCompound();

        NBTTagList leftHand = new NBTTagList();
        leftHand.add(new NBTTagFloat(leftHandOrientation)); //X rotation
        leftHand.add(new NBTTagFloat(0)); //Y rotation
        leftHand.add(new NBTTagFloat(0)); //Z rotation

        NBTTagList rightHand = new NBTTagList();
        rightHand.add(new NBTTagFloat(leftHandOrientation)); //X rotation
        rightHand.add(new NBTTagFloat(0)); //Y rotation
        rightHand.add(new NBTTagFloat(0)); //Z rotation


        poseTag.set("LeftArm", leftHand);
        poseTag.set("RightArm", rightHand);
        entityTag.set("Pose", poseTag);
        entityTag.setBoolean("ShowArms", true);
        tag.set("EntityTag", entityTag);

        armorStand.setTag(tag);

        return armorStand;
    }

    /**
     * @return Amount of items that can be crafted from this recipe
     */
    @Override
    public int a()
    {
        return 10;
    }

    /**
     * @return output stack. Used only for statistics apparently.
     */
    @Override
    public ItemStack b()
    {
        return new ItemStack(Items.ARMOR_STAND);
    }

    /**
     * @return List of items that have to be returned to the player after crafting (as opposed to being consumed)
     */
    @Override
    public ItemStack[] b(InventoryCrafting inventoryCrafting)
    {
        return new ItemStack[inventoryCrafting.getSize()]; //Return array with nulls (consume all the things)
    }

    private static float getOrientationFromStickPosition(InventoryCrafting craftingArea, int upStickSlot, int midStickSlot, int downStickSlot)
    {
        if (InventoryUtil.isItemTypeOnSlot(craftingArea, upStickSlot, Items.STICK))
        {
            if (InventoryUtil.isItemTypeOnSlot(craftingArea, midStickSlot, Items.STICK))
            {
                return 180f;
            }
            else
            {
                return 135f;
            }
        }
        else if (InventoryUtil.isItemTypeOnSlot(craftingArea, downStickSlot, Items.STICK))
        {
            if (InventoryUtil.isItemTypeOnSlot(craftingArea, midStickSlot, Items.STICK))
            {
                return 0f;
            }
            else
            {
                return 35f;
            }
        }
        else
        {
            return 90f;
        }
    }
}

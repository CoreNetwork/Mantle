package us.corenetwork.mantle.perks;

import net.minecraft.server.v1_8_R2.IRecipe;
import net.minecraft.server.v1_8_R2.InventoryCrafting;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.Items;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagFloat;
import net.minecraft.server.v1_8_R2.NBTTagList;
import net.minecraft.server.v1_8_R2.ShapelessRecipes;
import net.minecraft.server.v1_8_R2.World;
import org.bukkit.configuration.InvalidConfigurationException;
import us.core_network.cornel.custom.PerksUtil;
import us.core_network.cornel.items.NbtUtils;
import us.core_network.cornel.items.NbtYaml;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.YamlUtils;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;
import us.core_network.cornel.items.InventoryUtil;

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

        if (PerksUtil.hasGoldenName(armorStand))
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
        NBTTagCompound tag = null;
        try
        {
            tag = NbtYaml.loadFromFile(PerksSettings.SPECIAL_ARMOR_STAND_NANOBOT_FILE.string());
        }
        catch (Exception e)
        {
            MLog.severe("Failed to load special armorstand nanobot file!");
            e.printStackTrace();
            return null;
        }

        ArmOrientation leftHandOrientation = getOrientationFromStickPosition(inventoryCrafting, 0, 3, 6);
        ArmOrientation rightHandOrientation = getOrientationFromStickPosition(inventoryCrafting, 2, 5, 8);

        NBTTagCompound entityTag = new NBTTagCompound();
        NBTTagCompound poseTag = new NBTTagCompound();


        //Enter hand orientation into entity reverse since player sees mirror image when placing armor stand
        NBTTagList leftHand = new NBTTagList();
        leftHand.add(new NBTTagFloat(-rightHandOrientation.getVanillaOrientation())); //X rotation
        leftHand.add(new NBTTagFloat(0)); //Y rotation
        leftHand.add(new NBTTagFloat(0)); //Z rotation

        NBTTagList rightHand = new NBTTagList();
        rightHand.add(new NBTTagFloat(-leftHandOrientation.getVanillaOrientation())); //X rotation
        rightHand.add(new NBTTagFloat(0)); //Y rotation
        rightHand.add(new NBTTagFloat(0)); //Z rotation


        poseTag.set("LeftArm", leftHand);
        poseTag.set("RightArm", rightHand);
        entityTag.set("Pose", poseTag);
        entityTag.setBoolean("ShowArms", true);
        tag.set("EntityTag", entityTag);

        NbtUtils.replaceStringInNBT(tag, "<LeftArmPose>", leftHandOrientation.getDescription());
        NbtUtils.replaceStringInNBT(tag, "<RightArmPose>", rightHandOrientation.getDescription());

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

    private static ArmOrientation getOrientationFromStickPosition(InventoryCrafting craftingArea, int upStickSlot, int midStickSlot, int downStickSlot)
    {
        if (InventoryUtil.isItemTypeOnSlot(craftingArea, upStickSlot, Items.STICK))
        {
            if (InventoryUtil.isItemTypeOnSlot(craftingArea, midStickSlot, Items.STICK))
            {
                return ArmOrientation.UP;
            }
            else
            {
                return ArmOrientation.RAISED;
            }
        }
        else if (InventoryUtil.isItemTypeOnSlot(craftingArea, downStickSlot, Items.STICK))
        {
            if (InventoryUtil.isItemTypeOnSlot(craftingArea, midStickSlot, Items.STICK))
            {
                return ArmOrientation.DOWN;
            }
            else
            {
                return ArmOrientation.LOWERED;
            }
        }
        else
        {
            return ArmOrientation.SIDE;
        }
    }

    private static enum ArmOrientation
    {
        UP(180, "up"),
        RAISED(135, "raised"),
        SIDE(90, "side"),
        LOWERED(45, "lowered"),
        DOWN(0, "down");


        float vanillaOrientation;
        String description;

        ArmOrientation(float vanillaOrientation, String description)
        {
            this.vanillaOrientation = vanillaOrientation;
            this.description = description;
        }

        public float getVanillaOrientation()
        {
            return vanillaOrientation;
        }

        public String getDescription()
        {
            return description;
        }
    }
}

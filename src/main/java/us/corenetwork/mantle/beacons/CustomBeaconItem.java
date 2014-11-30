package us.corenetwork.mantle.beacons;

import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EnumDirection;
import net.minecraft.server.v1_8_R1.ItemBlock;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.World;

/**
 * Created by Matej on 30.11.2014.
 */
public class CustomBeaconItem extends ItemBlock
{
    public CustomBeaconItem(Block block)
    {
        super(block);
    }

    @Override
    public boolean interactWith(ItemStack itemStack, EntityHuman entityHuman, World world, BlockPosition blockPosition, EnumDirection enumDirection, float v, float v1, float v2)
    {
        boolean result = super.interactWith(itemStack, entityHuman, world, blockPosition, enumDirection, v, v1, v2);
        System.out.println("interact " + result);
        return result;

    }
}

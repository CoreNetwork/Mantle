package us.corenetwork.mantle.beacons;

import net.minecraft.server.v1_7_R4.BlockContainer;
import net.minecraft.server.v1_7_R4.CreativeModeTab;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.Material;
import net.minecraft.server.v1_7_R4.TileEntity;
import net.minecraft.server.v1_7_R4.World;

/**
 * Clone of original Minecraft's BlockBeacon.java with TileEntity changes and interact event changed.
 */
public class CustomBlockBeacon extends BlockContainer
{
    public CustomBlockBeacon()
    {
        super(Material.SHATTERABLE);
        this.c(3.0F);
        this.a(CreativeModeTab.f);

        this.c("beacon");
        this.a(1.0f);
        this.d("beacon");
    }

    @Override
    public TileEntity a(World world, int i)
    {
        return new CustomBeaconTileEntity();
    }

    @Override
    public boolean interact(World world, int i, int j, int k, EntityHuman entityhuman, int l, float f, float f1, float f2)
    {
        if (world.isStatic) {
            return true;
        } else {
            CustomBeaconTileEntity tileentitybeacon = (CustomBeaconTileEntity) world.getTileEntity(i, j, k);

            if (tileentitybeacon != null) {
                tileentitybeacon.clicked(entityhuman);
            }

            return true;
        }
    }

    @Override
    public boolean c()
    {
        return false;
    }

    @Override
    public boolean d()
    {
        return false;
    }

    @Override
    public int b()
    {
        return 34;
    }

    @Override
    public void postPlace(World world, int i, int i2, int i3, EntityLiving entityLiving, ItemStack itemStack)
    {
        super.postPlace(world, i, i2, i3, entityLiving, itemStack);
    }
}

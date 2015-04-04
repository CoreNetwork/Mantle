package us.corenetwork.mantle.beacons;

import java.lang.reflect.Field;
import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockContainer;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.CreativeModeTab;
import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.EnumDirection;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.Material;
import net.minecraft.server.v1_8_R2.TileEntity;
import net.minecraft.server.v1_8_R2.World;

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
        this.a(1.0f);

        try
        {
            Object blockData = Blocks.BEACON.getBlockData();

            Class blockDataClass = Class.forName("net.minecraft.server.v1_8_R2.BlockData");
            Field blockField = blockDataClass.getDeclaredField("a");
            blockField.setAccessible(true);
            blockField.set(blockData, this);
            this.j((IBlockData) blockData);
        } catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    @Override
    public TileEntity a(World world, int i)
    {
        return new CustomBeaconTileEntity();
    }

    @Override
    public boolean interact(World world, BlockPosition position, IBlockData blockData, EntityHuman entityhuman, EnumDirection direction, float f, float f1, float f2)
    {
        CustomBeaconTileEntity tileentitybeacon = (CustomBeaconTileEntity) world.getTileEntity(position);

        if (tileentitybeacon != null) {
            tileentitybeacon.clicked(entityhuman);
        }

        return true;
    }

    @Override
    public void doPhysics(World world, BlockPosition position, IBlockData iblockdata, Block block)
    {
        CustomBeaconTileEntity tileentitybeacon = (CustomBeaconTileEntity) world.getTileEntity(new BlockPosition(position));

        if (tileentitybeacon != null) {
            tileentitybeacon.physics();
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
        return 3;
    }
}

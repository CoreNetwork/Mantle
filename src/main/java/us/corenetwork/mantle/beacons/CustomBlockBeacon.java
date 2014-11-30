package us.corenetwork.mantle.beacons;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Random;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockBeacon;
import net.minecraft.server.v1_8_R1.BlockContainer;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.BlockWallBanner;
import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.CreativeModeTab;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityLiving;
import net.minecraft.server.v1_8_R1.EnumDirection;
import net.minecraft.server.v1_8_R1.IBlockData;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.Material;
import net.minecraft.server.v1_8_R1.TileEntity;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.Bukkit;

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

            Class blockDataClass = Class.forName("net.minecraft.server.v1_8_R1.BlockData");
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
        if (world.isStatic) {
            return true;
        } else {
            CustomBeaconTileEntity tileentitybeacon = (CustomBeaconTileEntity) world.getTileEntity(position);

            if (tileentitybeacon != null) {
                tileentitybeacon.clicked(entityhuman);
            }

            return true;
        }
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

package us.corenetwork.mantle.beacons;

import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.TileEntity;
import net.minecraft.server.v1_7_R4.TileEntityBeacon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.ParticleLibrary;

/**
 * Created by Matej on 28.10.2014.
 */
public class CustomBeaconTileEntity extends TileEntityBeacon
{
    private int pyramidSize = 0;

    private BeaconEffect activeEffect;

    /*
        Tile entity tick
     */
    @Override
    public void h()
    {
        //Check pyramid size every 80 ticks
        if (this.world.getTime() % 80L == 0L) {

            updatePyramidSize();
            Bukkit.broadcastMessage("pyramid size: " + pyramidSize);
        }

        if (this.world.getTime() % 20L == 0L)
        {
            ParticleLibrary.HAPPY_VILLAGER.broadcastParticle(getCenterLocation(), 0.5f, 0.5f, 0.5f, 0, 10);
        }
    }

    public void clicked(EntityHuman human)
    {
        if (activeEffect == null)
            human.getBukkitEntity().openInventory(new GUIEffectPicker(this));
        else
            human.getBukkitEntity().openInventory(new GUIBeaconStatus(this));
    }

    public BeaconEffect getActiveEffect()
    {
        return activeEffect;
    }

    public void setActiveEffect(BeaconEffect activeEffect)
    {
        this.activeEffect = activeEffect;
    }

    public int getPyramidSize()
    {
        return pyramidSize;
    }

    private Location getLocation()
    {
        return new Location(this.world.getWorld(), x, y, z);
    }

    private Block getBlock()
    {
        return this.world.getWorld().getBlockAt(x, y, z);
    }


    private Location getCenterLocation()
    {
        return new Location(this.world.getWorld(), x + 0.5, y + 0.5, z + 0.5);
    }

    private void updatePyramidSize()
    {
        int size = 0;
        boolean fail = false;

        Block startBlock = getBlock();
        for (int layer = 1; layer < 5; layer++)
        {
            for (int x = -layer; x <= layer; x++)
            {
                for (int z = -layer; z <= layer; z++)
                {
                    Block block = startBlock.getRelative(x, -layer, z);
                    if (block.getType() != Material.DIAMOND_BLOCK && block.getType() != Material.IRON_BLOCK && block.getType() != Material.GOLD_BLOCK)
                    {
                        fail = true;
                        break;
                    }
                }

                if (fail)
                    break;
            }

            if (fail)
                break;

            size++;
        }

        pyramidSize = size;
    }

    public static void inject()
    {
        try
        {
            Field tileEntityNameMapField = TileEntity.class.getDeclaredField("i");
            Field tileEntityClassMapField = TileEntity.class.getDeclaredField("j");

            tileEntityClassMapField.setAccessible(true);
            tileEntityNameMapField.setAccessible(true);

            Map tileEntityClassMap = (Map) tileEntityClassMapField.get(null);
            tileEntityClassMap.remove(TileEntityBeacon.class);
            tileEntityClassMap.put(CustomBeaconTileEntity.class, "Beacon");


            Map tileEntityNameMap = (Map) tileEntityNameMapField.get(null);
            tileEntityNameMap.put("Beacon", CustomBeaconTileEntity.class);

            net.minecraft.server.v1_7_R4.Block.REGISTRY.a(138, "beacon", new CustomBlockBeacon());
        }
        catch (Exception e)
        {
            MLog.severe("Error while loading beacons. Go bug matejdro!");
            e.printStackTrace();
        }
    }
}

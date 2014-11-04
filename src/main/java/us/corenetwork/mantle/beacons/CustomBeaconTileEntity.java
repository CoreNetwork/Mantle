package us.corenetwork.mantle.beacons;

import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.TileEntity;
import net.minecraft.server.v1_7_R4.TileEntityBeacon;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.ParticleLibrary;

/**
 * Created by Matej on 28.10.2014.
 */
public class CustomBeaconTileEntity extends TileEntityBeacon
{
    private int pyramidLevel = 0;
    private int range = 0;
    private int rangeSquared = 0;
    private int fuelLeftTicks = 33 * 20;
    private boolean goldPyramid;
    private boolean redstoneBlocked = false;

    private BeaconEffect activeEffect;

    public CustomBeaconTileEntity()
    {
        super();

        //Delay initialization for 1 tick to make sure stuff around has loaded
        Bukkit.getScheduler().runTask(MantlePlugin.instance, new Runnable()
        {
            @Override
            public void run()
            {
                redstoneBlocked = getBlock().isBlockPowered();
            }
        });
    }

    /*
        Tile entity tick
     */
    @Override
    public void h()
    {
        //Check pyramid size every 80 ticks
        if (this.world.getTime() % 80L == 0L) {

            updatePyramidSize();
        }


        if (isActive())
        {
            if (activeEffect.getEffectType() == BeaconEffect.EffectType.POTION && this.world.getTime() % 80L == 0L)
            {
                applyPotionEffect();
            }

            if (this.world.getTime() % 20L == 0L)
            {
                ParticleLibrary.HAPPY_VILLAGER.broadcastParticle(getCenterLocation(), 0.5f, 0.5f, 0.5f, 0, 10);
            }
        }
    }

    public void clicked(EntityHuman human)
    {
        if (activeEffect == null)
            human.getBukkitEntity().openInventory(new GUIEffectPicker(this));
        else
            human.getBukkitEntity().openInventory(new GUIBeaconStatus(this));
    }

    public void physics()
    {
        redstoneBlocked = getBlock().isBlockPowered();
    }

    public BeaconEffect getActiveEffect()
    {
        return activeEffect;
    }

    public void setActiveEffect(BeaconEffect activeEffect)
    {
        this.activeEffect = activeEffect;
    }

    public int getPyramidLevel()
    {
        return pyramidLevel;
    }

    public boolean isActive()
    {
        return activeEffect != null && fuelLeftTicks > 0 && pyramidLevel > 0 && !redstoneBlocked;
    }

    public int getRange()
    {
        return range;
    }

    public int getFuelLeftTicks()
    {
        return fuelLeftTicks;
    }

    public int getFuelDuration()
    {
        return activeEffect.getFuelDuration();
    }

    public boolean shouldUseStrongerEffect()
    {
        return pyramidLevel > 3 || (goldPyramid && pyramidLevel > 2);
    }

    public String getEffectName()
    {
        return shouldUseStrongerEffect() ? activeEffect.getEffectNameStrong() : activeEffect.getEffectNameWeak();
    }

    public boolean isPyramidSolidGold()
    {
        return goldPyramid;
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

    private void applyPotionEffect()
    {
        PotionEffect parameters = activeEffect.getPotionEffectParameters();

        int amplifier = parameters.getAmplifier();
        if (shouldUseStrongerEffect())
            amplifier = activeEffect.getStrongEffectModifier();

        PotionEffect actualEffect = new PotionEffect(parameters.getType(), 160, amplifier, parameters.isAmbient());

        int rangeChunks = (int) Math.ceil(range / 16.0);
        Chunk beaconChunk = getBlock().getChunk();
        Location beaconLocation = getCenterLocation();

        for (int x = -rangeChunks; x <= rangeChunks; x++)
        {
            for (int z = -rangeChunks; z <= rangeChunks; z++)
            {
                Chunk neighbour = beaconChunk.getWorld().getChunkAt(beaconChunk.getX() + x, beaconChunk.getZ() + z);
                Entity[] entities = neighbour.getEntities();
                for (Entity entity : entities)
                {

                    if (entity instanceof Player && entity.getLocation().distanceSquared(beaconLocation) <= rangeSquared)
                    {
                        LivingEntity livingEntity = (LivingEntity) entity;
                        livingEntity.addPotionEffect(actualEffect, true);
                    }
                }
            }
        }
    }

    private void updatePyramidSize()
    {
        long start = System.nanoTime();

        int size = 0;
        boolean fail = false;

        int totalBlocks = 0;
        int ironBlocks = 0;
        int emeraldBlocks = 0;
        int goldBlocks = 0;
        int diamondBlocks = 0;

        Block startBlock = getBlock();
        for (int layer = 1; layer < 5; layer++)
        {
            //If layer is not complete, those blocks will not be added to total
            int layerTotalBlocks = 0;
            int layerIronBlocks = 0;
            int layerGoldBlocks = 0;
            int layerEmeraldBlocks = 0;
            int layerDiamondBlocks = 0;

            for (int x = -layer; x <= layer; x++)
            {
                for (int z = -layer; z <= layer; z++)
                {
                    Block block = startBlock.getRelative(x, -layer, z);

                    switch (block.getType())
                    {
                        case IRON_BLOCK:
                            layerIronBlocks++;
                            break;
                        case EMERALD_BLOCK:
                            layerEmeraldBlocks++;
                            break;
                        case GOLD_BLOCK:
                            layerGoldBlocks++;
                            break;
                        case DIAMOND_BLOCK:
                            layerDiamondBlocks++;
                            break;
                        default:
                            fail = true;
                            break;
                    }

                    if (fail)
                        break;

                    layerTotalBlocks++;
                }

                if (fail)
                    break;
            }

            if (fail)
                break;

            totalBlocks += layerTotalBlocks;
            ironBlocks += layerIronBlocks;
            emeraldBlocks += layerEmeraldBlocks;
            goldBlocks += layerGoldBlocks;
            diamondBlocks += layerDiamondBlocks;

            size++;
        }

        pyramidLevel = size;

        double rangeIron = 0;
        double rangeGold = 0;
        double rangeEmerald = 0;
        double rangeDiamond = 0;

        switch (pyramidLevel)
        {
            case 1:
                rangeIron = ironBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_IRON_L1.doubleNumber();
                rangeGold = goldBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_GOLD_L1.doubleNumber();
                rangeEmerald = emeraldBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_EMERALD_L1.doubleNumber();
                rangeDiamond = diamondBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_DIAMOND_L1.doubleNumber();
                break;
            case 2:
                rangeIron = ironBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_IRON_L2.doubleNumber();
                rangeGold = goldBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_GOLD_L2.doubleNumber();
                rangeEmerald = emeraldBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_EMERALD_L2.doubleNumber();
                rangeDiamond = diamondBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_DIAMOND_L2.doubleNumber();
                break;
            case 3:
                rangeIron = ironBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_IRON_L3.doubleNumber();
                rangeGold = goldBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_GOLD_L3.doubleNumber();
                rangeEmerald = emeraldBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_EMERALD_L3.doubleNumber();
                rangeDiamond = diamondBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_DIAMOND_L3.doubleNumber();
                break;
            case 4:
                rangeIron = ironBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_IRON_L4.doubleNumber();
                rangeGold = goldBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_GOLD_L4.doubleNumber();
                rangeEmerald = emeraldBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_EMERALD_L4.doubleNumber();
                rangeDiamond = diamondBlocks * BeaconsSettings.PYRAMID_RANGE_PER_BLOCK_DIAMOND_L4.doubleNumber();
                break;
        }

        range = (int) Math.round(rangeIron + rangeGold + rangeEmerald + rangeDiamond);
        rangeSquared = range * range;

        this.goldPyramid = goldBlocks == totalBlocks;

        long end = System.nanoTime();
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

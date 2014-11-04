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
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.Util;

/**
 * Created by Matej on 28.10.2014.
 */
public class CustomBeaconTileEntity extends TileEntityBeacon
{
    private int pyramidLevel = 0;
    private int range = 0;
    private int rangeSquared = 0;
    private int fuelLeftTicks = 0;
    private boolean goldPyramid;
    private boolean redstoneBlocked = false;
    private boolean firstCheck;

    private Block lastFuelContainer;

    private BeaconEffect activeEffect;

    public CustomBeaconTileEntity()
    {
        super();

        firstCheck = true;

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

            fuelLeftTicks--;
        }
        else if (isReady())
        {
            refuel();
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
        fuelLeftTicks = 0;
    }

    public int getPyramidLevel()
    {
        return pyramidLevel;
    }

    public boolean isActive()
    {
        return activeEffect != null && fuelLeftTicks > 0 && pyramidLevel > 0 && !redstoneBlocked;
    }

    /*
        When beacon is ready to be activated, it just needs fuel
     */
    public boolean isReady()
    {
        return activeEffect != null && pyramidLevel > 0 && !redstoneBlocked;
    }

    public int getRange()
    {
        return range;
    }

    public int getFuelLeftTicks()
    {
        return fuelLeftTicks;
    }

    public int getFuelDurationMinutes()
    {
        int duration = shouldUseStrongerEffect() ? activeEffect.getFuelDurationStrongEffect() : activeEffect.getFuelDuration();
        if (isPyramidSolidGold())
            duration *= 1.1;

        return duration;
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

    private void refuel()
    {
        if (lastFuelContainer == null)
        {
            findNewFuelContainer();
            return;
        }

        BlockState state = lastFuelContainer.getState();
        if (!(state instanceof InventoryHolder))
        {
            lastFuelContainer = null;
            return;
        }

        InventoryHolder holder = (InventoryHolder) state;
        Inventory inventory = holder.getInventory();

        boolean foundItem = false;
        ItemStack fuel = activeEffect.getFuelIcon();
        for (int i = 0; i < inventory.getSize(); i++)
        {
            ItemStack item = inventory.getItem(i);
            if (item != null && Util.isItemTypeSame(fuel, item))
            {
                if (item.getAmount() > 1)
                {
                    item.setAmount(item.getAmount() - 1);
                    inventory.setItem(i, item);
                }
                else
                {
                    inventory.setItem(i, null);
                }

                foundItem = true;
                fuelLeftTicks += getFuelDurationMinutes() * 1200; //1200 ticks in minute

                break;
            }
        }

        if (!foundItem)
            lastFuelContainer = null;

    }

    private void findNewFuelContainer()
    {
        if (this.world.getTime() % 20L != 0L) //Scan for new chests every 20 ticks to prevent constant scanning if there is no chest placed around
            return;

        Block beaconBlock = getBlock();
        ItemStack fuel = activeEffect.getFuelIcon();

        for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST})
        {
            Block neighbour = beaconBlock.getRelative(face);
            BlockState state = neighbour.getState();

            if (state instanceof InventoryHolder)
            {
                InventoryHolder holder = (InventoryHolder) state;
                Inventory inventory = holder.getInventory();

                for (ItemStack stack : inventory)
                {
                    if (stack != null && Util.isItemTypeSame(fuel, stack))
                    {
                        lastFuelContainer = neighbour;
                        break;
                    }
                }

                if (lastFuelContainer != null)
                    break;
            }
        }
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

        boolean strongEffectBefore = shouldUseStrongerEffect();
        pyramidLevel = size;
        range = (int) Math.round(rangeIron + rangeGold + rangeEmerald + rangeDiamond);
        rangeSquared = range * range;
        this.goldPyramid = goldBlocks == totalBlocks;

        //When switching pyramid from L3 to L4 (or to L3 in gold case), deplete all fuel
        if (!firstCheck && !strongEffectBefore && shouldUseStrongerEffect())
            fuelLeftTicks = 0;

        firstCheck = false;
    }

    @Override
    public int getSize()
    {
        return 0;
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

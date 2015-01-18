package us.corenetwork.mantle.netherspawning;

import java.lang.reflect.Field;

import java.util.Random;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.EntitySkeleton;
import net.minecraft.server.v1_8_R1.GenericAttributes;
import net.minecraft.server.v1_8_R1.PathfinderGoal;
import net.minecraft.server.v1_8_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R1.World;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftSkeleton;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimespawning.IgnoredSlimeChunks;
import us.corenetwork.mantle.slimespawning.SlimeSpawner;

public class NetherSpawner {

	public static void startSpawning(Block block, EntityType type)
	{
		spawn(block, type);

		int	maxAdditionalPackMobs = NetherSpawningSettings.MAX_ADDITIONAL_PACK_MOBS.integer();
		int	minAdditionalPackMobs = NetherSpawningSettings.MIN_ADDITIONAL_PACK_MOBS.integer();
		int diff = maxAdditionalPackMobs - minAdditionalPackMobs;

		if (diff <= 0)
			return;

		int additionalMobs = MantlePlugin.random.nextInt(diff) + minAdditionalPackMobs;
		for (int i = 0; i < additionalMobs; i++)
		{
			int xDiff = MantlePlugin.random.nextInt(10) - 5;
			int zDiff = MantlePlugin.random.nextInt(10) - 5;

			Block newBlock = block.getRelative(xDiff, 0, zDiff);
			if (!newBlock.isEmpty())
				continue;

			Block aboveBlock = newBlock.getRelative(BlockFace.UP);
			if (aboveBlock == null || aboveBlock.getY() < newBlock.getY())
				continue;
			if (!(aboveBlock.getType().isTransparent() && aboveBlock.getType() != Material.CARPET))
				continue;

			Block belowBlock = newBlock.getRelative(BlockFace.DOWN);
			if (belowBlock == null || belowBlock.getY() > newBlock.getY())
				continue;
			if (!canSpawnOnThisBlock(belowBlock))
				continue;

			spawn(block, type);
		}		
	}

    public static void spawn(Block block, EntityType type)
    {
        switch (type)
        {
            case BLAZE:
                spawnBlaze(block);
                break;
            case SKELETON:
                spawnWitherSkeleton(block, SpawnReason.NATURAL);
                break;
            case MAGMA_CUBE:
                spawnMagmaCube(block);
                break;

        }
    }

	private static void spawnBlaze(Block block)
	{
        boolean foundFire = false;

        // Scan for nearby fire in + pattern
        for (int x = -4; x <= 4; x++)
        {
            for (int z = -4; z <= 4; z++)
            {
                Block neighbour = block.getRelative(x, 0, z);
                if (neighbour.getType() == Material.FIRE)
                {
                    foundFire = true;
                    break;
                }
            }

            if (foundFire)
                break;
        }

        if (!foundFire)
            return;

        //Search for nearby Blazes
        int range = 25; //5*5
        for (int x = -1; x <= 1; x++)
        {
            for (int z = -1; z <= 1; z++)
            {
                Chunk neighbourChunk = block.getWorld().getChunkAt(block.getChunk().getX() + x, block.getChunk().getZ() + z);

                for (Entity entity : neighbourChunk.getEntities())
                {
                    if (entity instanceof Blaze && entity.getLocation().distanceSquared(Util.getLocationInBlockCenter(block)) < range)
                        return;
                }
            }
        }

		NetherSpawningHelper.spawningMob = true;
		block.getWorld().spawnEntity(Util.getLocationInBlockCenter(block), EntityType.BLAZE);
	}

	public static Skeleton spawnWitherSkeleton(Block block, SpawnReason reason)
	{
        if (reason == SpawnReason.NATURAL)
        {
            if (block.getY() > NetherSpawningSettings.WITHER_SKELETON_MAX_Y.integer() || block.getY() < NetherSpawningSettings.WITHER_SKELETON_MIN_Y.integer())
                return null;

            if (MantlePlugin.random.nextDouble() > NetherSpawningSettings.WITHER_SKELETON_SPAWN_CHANCE.doubleNumber())
                return null;
        }

        Block thirdBlock = block.getRelative(BlockFace.UP, 2);
		if (thirdBlock == null || thirdBlock.getY() < block.getY() || !(thirdBlock.getType().isTransparent() && thirdBlock.getType() != Material.CARPET))
			return null;

		boolean bowSkeleton = MantlePlugin.random.nextDouble() < NetherSpawningSettings.WITHER_SKELETON_RARE_BOW_CHANCE.doubleNumber();
		
		World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
		EntitySkeleton nmsSkeleton = new EntitySkeleton(nmsWorld);
		nmsSkeleton.setSkeletonType(1);

		try
		{
			Field goalSelectorField = EntityInsentient.class.getDeclaredField("goalSelector");
			goalSelectorField.setAccessible(true);
			PathfinderGoalSelector goalSelector = (PathfinderGoalSelector) goalSelectorField.get(nmsSkeleton);

			if (bowSkeleton)
			{
				Field bowPathFinder = EntitySkeleton.class.getDeclaredField("b");
				bowPathFinder.setAccessible(true);
				goalSelector.a(4, (PathfinderGoal) bowPathFinder.get(nmsSkeleton));

			}
			else
			{
				Field meleePathfinder = EntitySkeleton.class.getDeclaredField("c");
				meleePathfinder.setAccessible(true);
				goalSelector.a(4, (PathfinderGoal) meleePathfinder.get(nmsSkeleton));
			}
		}
		catch (Exception e)
		{
			MLog.severe("Error while spawning wither skeleton! Go bug matejdro!");
			e.printStackTrace();
		}

		nmsSkeleton.getAttributeInstance(GenericAttributes.e).setValue(NetherSpawningSettings.WITHER_SKELETON_STRENGTH.doubleNumber());
		nmsSkeleton.setLocation(block.getX() + 0.5, block.getY(), block.getZ() + 0.5, 0f, 0f);
		
		if (reason == SpawnReason.NATURAL)
			NetherSpawningHelper.spawningMob = true;
		if (!nmsWorld.addEntity(nmsSkeleton, reason))
			return null;

		Skeleton skeleton = (CraftSkeleton) nmsSkeleton.getBukkitEntity();

        if (bowSkeleton)
            skeleton.getEquipment().setItemInHand(new ItemStack(Material.BOW));

        nmsSkeleton.getAttributeInstance(GenericAttributes.d).setValue(NetherSpawningSettings.WITHER_SKELETON_NORMAL_SPEED.doubleNumber());

        return skeleton;
	}

    public static void spawnMagmaCube(Block block)
    {
        if (block.getY() < NetherSpawningSettings.MAGMA_CUBE_MIN_Y.integer())
            return;

        if (block.getY() > NetherSpawningSettings.MAGMA_CUBE_MAX_Y.integer())
            return;

        if (block.getLightLevel() < NetherSpawningSettings.MAGMA_CUBE_MIN_LIGHT.integer())
            return;

        Chunk chunk = block.getChunk();
        if (IgnoredSlimeChunks.isIgnored(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()) || !isMagmaCubeChunk(chunk))
            return;

        int size = MantlePlugin.random.nextInt(3);
        size = 1 << size;

        if (!SlimeSpawner.isThereEnoughSpace(block, size))
            return;

        NetherSpawningHelper.spawningMob = true;

        Location location = Util.getLocationInBlockCenter(block);
        location.setYaw(MantlePlugin.random.nextFloat() * 360);

        MagmaCube magmaCube = block.getWorld().spawn(location, MagmaCube.class);
        if (magmaCube.isValid())
        {
            magmaCube.setSize(size);
        }
    }


    public static boolean isMagmaCubeChunk(Chunk chunk)
    {
        long seed = chunk.getWorld().getSeed();

        Random rnd = new Random(seed +
                (long) (chunk.getX() * chunk.getX() * 0x6091c4) +
                (long) (chunk.getX() * 0xbd0ca) +
                (long) (chunk.getZ() * chunk.getZ()) * 0x7a703L +
                (long) (chunk.getZ() * 0x5f24f) ^ 0x5208daf);
        return rnd.nextInt(10) == 0;
    }

    public static void spawnGhast(Block block)
    {
        //Check if there is enough space for Ghast to spawn
        for (int x = -2; x <= 2; x++)
        {
            for (int y = -2; y <= 2; y++)
            {
                for (int z = -2; z <= 2; z++)
                {
                    Block neighbour = block.getRelative(x, y, z);
                    if (!neighbour.isEmpty())
                        return;

                }
            }
        }
        NetherSpawningHelper.spawningMob = true;
        block.getWorld().spawnEntity(Util.getLocationInBlockCenter(block), EntityType.GHAST);
    }

    public static boolean canSpawnOnThisBlock(Block block)
	{
		//Mobs can't spawn on top of bedrock
		if (block.getType() == Material.BEDROCK)
			return false;
		
		//Solid blocks automatically qualify.
		if (block.getType().isOccluding())
			return true;
		
		//Mobs can spawn on top of upside down half blocks
		if (block.getType().getData() == Step.class)
		{
			return new Step(block.getType(), block.getData()).isInverted();
		}
		
		if (block.getType().getData() == Stairs.class)
		{
			return new Stairs(block.getType(), block.getData()).isInverted();

		}

		if (block.getType() == Material.HOPPER)
		{
			return true;
		}
		
		return false;
	}



}

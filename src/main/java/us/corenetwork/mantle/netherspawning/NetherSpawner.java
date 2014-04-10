package us.corenetwork.mantle.netherspawning;

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R2.EntityInsentient;
import net.minecraft.server.v1_7_R2.EntitySkeleton;
import net.minecraft.server.v1_7_R2.GenericAttributes;
import net.minecraft.server.v1_7_R2.PathfinderGoal;
import net.minecraft.server.v1_7_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R2.World;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftSkeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.animalspawning.AnimalSpawningSettings;

public class NetherSpawner {

	public static void spawnMob(Block block)
	{
		boolean blaze = false;
		if (block.getY() < NetherSpawningSettings.BLAZE_MAX_Y.integer())
		{
			blaze = MantlePlugin.random.nextDouble() < NetherSpawningSettings.BLAZE_CHANCE.doubleNumber();
		}

		if (blaze)
			spawnBlaze(block);
		else
			spawnWitherSkeleton(block, SpawnReason.NATURAL);

		int	maxAdditionalPackMobs = AnimalSpawningSettings.MAX_ADDITIONAL_PACK_MOBS.integer();		
		int	minAdditionalPackMobs = AnimalSpawningSettings.MIN_ADDITIONAL_PACK_MOBS.integer();
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
			if (!aboveBlock.getType().isTransparent())
				continue;

			Block belowBlock = newBlock.getRelative(BlockFace.DOWN);
			if (belowBlock == null || belowBlock.getY() > newBlock.getY())
				continue;
			if (!canSpawnOnThisBlock(belowBlock))
				continue;

			if (blaze)
				spawnBlaze(newBlock);
			else
				spawnWitherSkeleton(newBlock, SpawnReason.NATURAL);
		}		
	}

	private static void spawnBlaze(Block block)
	{
		NetherSpawningHelper.spawningMob = true;
		block.getWorld().spawnEntity(getLocation(block), EntityType.BLAZE);
	}

	public static Skeleton spawnWitherSkeleton(Block block, SpawnReason reason)
	{
		Block thirdBlock = block.getRelative(BlockFace.UP, 2);
		if (thirdBlock == null || thirdBlock.getY() < block.getY() || !thirdBlock.getType().isTransparent())
			return null;

		boolean rareSpawn = MantlePlugin.random.nextDouble() < NetherSpawningSettings.WITHER_SKELETON_RARE_SPAWN_CHANCE.doubleNumber() && block.getY() <= NetherSpawningSettings.WITHER_SKELETON_RARE_MAX_SPAWN_Y.integer();
		boolean bowSkeleton = !rareSpawn && MantlePlugin.random.nextDouble() < NetherSpawningSettings.WITHER_SKELETON_RARE_BOW_CHANCE.doubleNumber();
		
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
				Field bowPathFinder = EntitySkeleton.class.getDeclaredField("bp");
				bowPathFinder.setAccessible(true);
				goalSelector.a(4, (PathfinderGoal) bowPathFinder.get(nmsSkeleton));

			}
			else
			{
				Field meleePathfinder = EntitySkeleton.class.getDeclaredField("bq");
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
		if (rareSpawn)
		{
				skeleton.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD, 1));
		}
		else
		{
			if (bowSkeleton)
				skeleton.getEquipment().setItemInHand(new ItemStack(Material.BOW));

			nmsSkeleton.getAttributeInstance(GenericAttributes.d).setValue(NetherSpawningSettings.WITHER_SKELETON_NORMAL_SPEED.doubleNumber());
		}
		
		return skeleton;
	}

	public static Location getLocation(Block block)
	{
		return new Location(block.getWorld(), block.getX() + 0.5, block.getY(), block.getZ() + 0.5);
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
		
		return false;
	}

}

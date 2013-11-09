package us.corenetwork.mantle.netherspawning;

import java.lang.reflect.Field;

import net.minecraft.server.v1_6_R3.EntityInsentient;
import net.minecraft.server.v1_6_R3.EntitySkeleton;
import net.minecraft.server.v1_6_R3.GenericAttributes;
import net.minecraft.server.v1_6_R3.PathfinderGoal;
import net.minecraft.server.v1_6_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftSkeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.animalspawning.AnimalSpawningSettings;
import us.corenetwork.mantle.hardmode.HardmodeModule;

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
			spawnWitherSkeleton(block);

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
			if (!aboveBlock.isEmpty())
				continue;

			Block belowBlock = newBlock.getRelative(BlockFace.DOWN);
			if (belowBlock == null || belowBlock.getY() > newBlock.getY())
				continue;
			if (!belowBlock.getType().isOccluding())
				continue;

			if (blaze)
				spawnBlaze(newBlock);
			else
				spawnWitherSkeleton(newBlock);
		}		
	}

	private static void spawnBlaze(Block block)
	{
		NetherSpawningHelper.spawningMob = true;
		block.getWorld().spawnEntity(getLocation(block), EntityType.BLAZE);
	}

	private static void spawnWitherSkeleton(Block block)
	{
		Block thirdBlock = block.getRelative(BlockFace.UP, 2);
		if (thirdBlock == null || thirdBlock.getY() < block.getY() || !thirdBlock.isEmpty())
			return;

		World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
		EntitySkeleton nmsSkeleton = new EntitySkeleton(nmsWorld);
		nmsSkeleton.setSkeletonType(1);

		try
		{
			Field goalSelectorField = EntityInsentient.class.getDeclaredField("goalSelector");
			goalSelectorField.setAccessible(true);
			PathfinderGoalSelector goalSelector = (PathfinderGoalSelector) goalSelectorField.get(nmsSkeleton);

			Field meleePathfinder = EntitySkeleton.class.getDeclaredField("bq");
			meleePathfinder.setAccessible(true);
			goalSelector.a(4, (PathfinderGoal) meleePathfinder.get(nmsSkeleton));
		}
		catch (Exception e)
		{
			MLog.severe("Error while spawning wither skeleton! Go bug matejdro!");
			e.printStackTrace();
		}

		nmsSkeleton.getAttributeInstance(GenericAttributes.e).setValue(NetherSpawningSettings.WITHER_SKELETON_STRENGTH.doubleNumber());
		nmsSkeleton.setLocation(block.getX() + 0.5, block.getY(), block.getZ() + 0.5, 0f, 0f);
		NetherSpawningHelper.spawningMob = true;
		nmsWorld.addEntity(nmsSkeleton);

		Skeleton skeleton = (CraftSkeleton) nmsSkeleton.getBukkitEntity();
		if (MantlePlugin.random.nextDouble() < NetherSpawningSettings.WITHER_SWORD_CHANCE.doubleNumber() && block.getY() <= NetherSpawningSettings.WITHER_SWORD_MAX_Y.integer())
		{
			skeleton.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD, 1));
		}
		else
		{
			HardmodeModule.applyDamageNode(skeleton, NetherSpawningSettings.WITHER_APPLY_DAMAGE_NODE_ON_SPAWN.string());
			nmsSkeleton.getAttributeInstance(GenericAttributes.d).setValue(NetherSpawningSettings.WITHER_NOSWORD_SPEED.doubleNumber());
		}
	}

	private static Location getLocation(Block block)
	{
		return new Location(block.getWorld(), block.getX() + 0.5, block.getY(), block.getZ() + 0.5);
	}

}

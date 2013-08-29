package us.corenetwork.mantle.netherspawning;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.animalspawning.AnimalSpawningSettings;

public class NetherSpawner {
		
	public static void spawnMob(Block block)
	{
		boolean blaze = false;
		if (block.getY() < 60)
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
			if (newBlock.getType() != Material.AIR)
				continue;

			Block belowBlock = newBlock.getRelative(BlockFace.DOWN);
			
			if (belowBlock == null)
				continue;
			if (!belowBlock.getType().isSolid())
				continue;
		
			if (blaze)
				spawnBlaze(newBlock);
			else
				spawnWitherSkeleton(newBlock);
		}
		
		if (blaze)
			spawnBlaze(block);
		else
			spawnWitherSkeleton(block);
		
	}
	
	private static void spawnBlaze(Block block)
	{
		NetherSpawningHelper.spawningMob = true;
		block.getWorld().spawnEntity(block.getLocation(), EntityType.BLAZE);
	}
	
	private static void spawnWitherSkeleton(Block block)
	{
		Block thirdBlock = block.getRelative(BlockFace.UP, 2);
		if (thirdBlock == null || !thirdBlock.isEmpty())
			return;
			
		NetherSpawningHelper.spawningMob = true;
		Skeleton skeleton = (Skeleton) block.getWorld().spawnEntity(block.getLocation(), EntityType.SKELETON);
		
		skeleton.setSkeletonType(SkeletonType.WITHER);
		
		
		if (MantlePlugin.random.nextDouble() < NetherSpawningSettings.WITHER_SWORD_CHANCE.doubleNumber())
		{
			skeleton.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD, 1));
		}
		else
		{
			PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0);
			skeleton.addPotionEffect(slowness);
		}
	}

}

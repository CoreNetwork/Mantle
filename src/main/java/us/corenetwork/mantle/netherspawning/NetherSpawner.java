package us.corenetwork.mantle.netherspawning;

import java.lang.reflect.Field;

import net.minecraft.server.v1_6_R3.EntitySkeleton;
import net.minecraft.server.v1_6_R3.Item;
import net.minecraft.server.v1_6_R3.PathfinderGoal;
import net.minecraft.server.v1_6_R3.PathfinderGoalSelector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftSkeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.animalspawning.AnimalSpawningSettings;
import us.corenetwork.mantle.hardmode.HardmodeModule;

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
		
		EntitySkeleton ent = ((CraftSkeleton)skeleton).getHandle();
        try {
            ent.setSkeletonType(1);
            Field selector = EntitySkeleton.class.getDeclaredField("goalSelector");
            selector.setAccessible(true);
            Field e = EntitySkeleton.class.getDeclaredField("e");
            e.setAccessible(true);
            PathfinderGoalSelector goals = (PathfinderGoalSelector) selector.get(ent);
            goals.a(4, (PathfinderGoal) e.get(ent));
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
		
		
		if (MantlePlugin.random.nextDouble() < NetherSpawningSettings.WITHER_SWORD_CHANCE.doubleNumber() && block.getY() <= NetherSpawningSettings.WITHER_SWORD_MAX_Y.integer())
		{
			skeleton.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD, 1));
		}
		else
		{
			HardmodeModule.applyDamageNode(skeleton, NetherSpawningSettings.WITHER_APPLY_DAMAGE_NODE_ON_SPAWN.string());
		}
	}

}

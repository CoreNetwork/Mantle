package us.corenetwork.mantle.slimespawning;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.netherspawning.NetherSpawner;

public class SlimeSpawner {

	public static void spawn(Block block)
	{		
		if (block.getLightLevel() < 8)
			return;
		
		if (!isSlimeChunk(block.getChunk()))
			return;
		
		int size = MantlePlugin.random.nextInt(3);
		size = 1 << size;

		if (!isThereEnoughSpace(block, size))
			return;
		
        if (getDistanceToNearestPlayer(block.getLocation()) < SlimeSpawningSettings.NEAREST_PLAYER_MINIMUM_DISTANCE_SQUARED.integer())
        	return;
		
		SlimeSpawningHelper.spawningMob = true;
		
		Location location = NetherSpawner.getLocation(block);
		//Bug in bukkit swaps yaw and pitch when spawning
		location.setPitch(MantlePlugin.random.nextFloat() * 360);
		
		Slime slime = block.getWorld().spawn(location, Slime.class);
		if (slime.isValid())
		{
			slime.setSize(size);
		}
	
	}

	private static boolean isSlimeChunk(Chunk chunk)
	{
		long seed = chunk.getWorld().getSeed();
		
		Random rnd = new Random(seed + 
                (long) (chunk.getX() * chunk.getX() * 0x4c1906) + 
                (long) (chunk.getX() * 0x5ac0db) + 
                (long) (chunk.getZ() * chunk.getZ()) * 0x4307a7L + 
                (long) (chunk.getZ() * 0x5f24f) ^ 0x3ad8025f);
		return rnd.nextInt(10) == 0;
	}
	
	private static boolean isThereEnoughSpace(Block block, int size)
	{
		size = (int) Math.ceil(size * 0.6);
		
		int hSize = (int) (Math.floor(size / 2.0));
		
		//Ground
		for (int x = -hSize; x <= hSize; x++)
		{
			for (int z = -hSize; z <= hSize; z++)
			{
				Block neighbour = block.getRelative(x, -1, z);
				
				if (!NetherSpawner.canSpawnOnThisBlock(neighbour))
					return false;
			}
		}
		
		//Air
		for (int x = -hSize; x <= hSize; x++)
		{
			for (int z = -hSize; z <= hSize; z++)
			{
				for (int y = 0; y < size; y++)
				{
					Block neighbour = block.getRelative(x, y, z);
					
					if (!neighbour.isEmpty())
						return false;

				}
			}
		}
		
		return true;
	}
	
    public static int getDistanceToNearestPlayer(Location location)
    {
    	int minDistance = Integer.MAX_VALUE;
    	for (Player player : Bukkit.getOnlinePlayers())
    	{
    		if (player.getWorld() != location.getWorld())
    			continue;
    		
    		int distance = (int) player.getLocation().distanceSquared(location);
    		if (distance < minDistance)
    			minDistance = distance;
    	}
    	
    	return minDistance;
    }    
}

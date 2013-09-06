package us.corenetwork.mantle.spellbooks;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import us.corenetwork.mantle.Util;

public class CircleIterator {
	public static void iterateCircleBlocks(CircleIterator.BlockReceiver blockReceiver, Location center, int radius)
	{
		center = center.getBlock().getLocation();

		int radiusSquared = radius * radius;
		for (int x = -radius; x <= radius; x++)
		{
			for (int z = -radius; z <= radius; z++)
			{
				Location location = center.clone().add(x, 0, z);
				if (Util.flatDistanceSquared(center, location) <= radiusSquared)
				{
					blockReceiver.onCircleColumnFound(center.getWorld(), location.getBlockX(), location.getBlockZ());
				}
			}
		}
	}
	
	public static void iterateCircleEntities(CircleIterator.EntityReceiver entityReceiver, Location center, int radius)
	{
		center = center.getBlock().getLocation();

		World world = center.getWorld();
		Chunk centerChunk = center.getChunk();
		int centerX = centerChunk.getX();
		int centerZ = centerChunk.getZ();

		int chunks = (int) Math.ceil(radius / 16.0);
		
		int radiusSquared = radius * radius;
		
		for (int x = -chunks; x <= chunks; x++)
		{
			for (int z = -chunks; z <= chunks; z++)
			{
				Chunk chunk = world.getChunkAt(x + centerX, z + centerZ);
				for (Entity e : chunk.getEntities())
				{
					if (Util.flatDistanceSquared(center, e.getLocation()) <= radiusSquared)
					{
						entityReceiver.onCircleEntity(e);
					}
				}
			}
		}
	}

	public static interface BlockReceiver
	{
		public void onCircleColumnFound(World world, int x, int z);	
	}
	
	public static interface EntityReceiver
	{
		public void onCircleEntity(Entity entity);	
	}
}

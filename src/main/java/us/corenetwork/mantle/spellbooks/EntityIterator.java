package us.corenetwork.mantle.spellbooks;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class EntityIterator {
	
	public static void iterateEntitiesInCube(EntityIterator.EntityReceiver entityReceiver, Location center, int radius)
	{
		center = center.getBlock().getLocation();

		World world = center.getWorld();
		Chunk centerChunk = center.getChunk();
		int centerX = centerChunk.getX();
		int centerZ = centerChunk.getZ();

		int chunks = (int) Math.ceil(radius / 16.0);
				
		for (int x = -chunks; x <= chunks; x++)
		{
			for (int z = -chunks; z <= chunks; z++)
			{
				Chunk chunk = world.getChunkAt(x + centerX, z + centerZ);
				for (Entity e : chunk.getEntities())
				{
					if (Math.abs(e.getLocation().getX() - center.getX()) > radius ||
						Math.abs(e.getLocation().getY() - center.getY()) > radius ||
						Math.abs(e.getLocation().getZ() - center.getZ()) > radius)
					{
						continue;
					}
					
					
					entityReceiver.onEntityFound(e);
				}
			}
		}
	}
	
	public static interface EntityReceiver
	{
		public void onEntityFound(Entity entity);	
	}
}

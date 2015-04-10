package us.corenetwork.mantle.spellbooks;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class EntityIterator {
	
	public static List<Entity> getEntitiesInCube(Location center, int radius)
	{
        LinkedList<Entity> list = new LinkedList<>();

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
					
					
					list.add(e);
				}
			}
		}

        return list;
	}
}

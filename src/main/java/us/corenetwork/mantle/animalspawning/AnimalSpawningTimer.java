package us.corenetwork.mantle.animalspawning;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;

import java.util.ArrayDeque;

public class AnimalSpawningTimer implements Runnable {
	public static AnimalSpawningTimer timerSingleton;
	
	private static World overworld;
	
	public AnimalSpawningTimer()
	{
		overworld = Bukkit.getWorld(AnimalSpawningSettings.SPAWNING_WORLD.string());
	}
	
	@Override
	public void run() {		
		MLog.debug("Starting animal spawning PREPARE!");

		if (overworld.getTime() > 13000 && overworld.getTime() < 23000)
		{
			MLog.debug("Night! Cancelling...");
			Bukkit.getScheduler().runTaskLaterAsynchronously(MantlePlugin.instance, AnimalSpawningTimer.timerSingleton, AnimalSpawningSettings.SPAWNING_INTERVAL_TICKS.integer());
			return;
		}

		long start = System.nanoTime();
		
		int amount = AnimalSpawningSettings.CHUNKS_SPAWNING_AMOUNT.integer();
		
		ArrayDeque<ChunkCoordinates> chunks = new ArrayDeque<ChunkCoordinates>(amount);
		
		MLog.debug("[ANIMAL] Randomizing chunks - start!");

		
		for(int i = 0; i<amount;i++)
		{
			 chunks.add(AnimalRange.getRandomChunk());
		}
		
		MLog.debug("[ANIMAL] Randomizing chunks - finish!");
		
				
		long end = System.nanoTime();
		MLog.debug("Spawning PREPARE ended!");
		MLog.debug("PREPARE Counter: " + chunks.size());
		MLog.debug("PREPARE Time: " + (end - start) / 1000000.0);
		
		SyncSpawner spawner = new SyncSpawner(chunks);
		Bukkit.getScheduler().runTask(MantlePlugin.instance, spawner);
	}
	
	public static class SyncSpawner implements Runnable
	{
		Iterable<ChunkCoordinates> chunks;
		
		public SyncSpawner(Iterable<ChunkCoordinates> chunks)
		{
			this.chunks = chunks;
		}
		
		@Override
		public void run() {
			MLog.debug("Starting animal spawning SYNC!");

			int unloaded = 0;
			long start = System.nanoTime();
			
			long chunkLoad = 0;
			
			for (ChunkCoordinates coordinates : chunks)
			{
				if (!overworld.isChunkLoaded(coordinates.x, coordinates.z))
					unloaded++;
				
				long chunkLoadStart = System.nanoTime();
				Chunk c = overworld.getChunkAt(coordinates.x, coordinates.z);
				
				if (!c.isLoaded())
				{
					c.load();
				}
				long chunkLoadEnd = System.nanoTime();
				chunkLoad += chunkLoadEnd - chunkLoadStart;
				
				//Don't spawn if there is already animal inside that chunk
				boolean animalExist = false;
				Entity[] entities = c.getEntities();
				for (Entity e : entities)
				{
					if (e instanceof Animals)
					{
						animalExist = true;
						break;
					}
				}
				if (animalExist)
					continue;
				
				int randomX = MantlePlugin.random.nextInt(16);
				int randomZ = MantlePlugin.random.nextInt(16);
				
				randomX += c.getX() * 16;
				randomZ += c.getZ() * 16;
				
				Block block = overworld.getHighestBlockAt(randomX, randomZ);
				if (block.getType() != Material.AIR)
					continue;

				Block belowBlock = block.getRelative(BlockFace.DOWN);
								
				if (belowBlock == null)
					continue;
				if (block.getLightLevel() < 9)
					continue;
				if (belowBlock.getType() != Material.GRASS)
					continue;
			
				AnimalSpawner.spawnAnimal(block);
			}
			
			long end = System.nanoTime();
			
			MLog.debug("Spawning SYNC ended!");
			MLog.debug("SYNC Unloaded: " + unloaded);
			MLog.debug("SYNC Time: " + (end - start) / 1000000.0);
			MLog.debug("SYNC Chunk loading only time: " + chunkLoad / 1000000.0);

			Bukkit.getScheduler().runTaskLaterAsynchronously(MantlePlugin.instance, AnimalSpawningTimer.timerSingleton, AnimalSpawningSettings.SPAWNING_INTERVAL_TICKS.integer());
		}
		

	}
}

package us.corenetwork.mantle.animalspawning;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MantlePlugin;

public class AnimalSpawningTimer implements Runnable {
	public static AnimalSpawningTimer timerSingleton;
	
	private static World overworld;
	
	public AnimalSpawningTimer()
	{
		overworld = Bukkit.getWorlds().get(0);
	}
	
	@Override
	public void run() {		
		MLog.debug("Starting animal spawning PREPARE!");

		long start = System.nanoTime();
		
		int amount = AnimalSpawningSettings.CHUNKS_SPAWNING_AMOUNT.integer();
		
		ArrayDeque<ChunkCoordinates> chunks = new ArrayDeque<ChunkCoordinates>(amount);
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM animal_chunks WHERE Spawned <> 0 ORDER BY Spawned LIMIT " + amount);
			ResultSet set = statement.executeQuery();
			
			while (set.next())
			{
				ChunkCoordinates coordinates = new ChunkCoordinates();
				coordinates.x = set.getInt("X");
				coordinates.z = set.getInt("Z");
				
				chunks.add(coordinates);
				
				PreparedStatement updateStatement = IO.getConnection().prepareStatement("UPDATE animal_chunks SET Spawned = 0 WHERE X = ? AND Z = ?");
				updateStatement.setInt(1, coordinates.x);
				updateStatement.setInt(2, coordinates.z);

				updateStatement.executeUpdate();
				updateStatement.close();
			}
			
			statement.close();
			if (chunks.size() < amount)
			{
				MLog.debug("All chunks spawned! Resetting...");
				statement = IO.getConnection().prepareStatement("UPDATE animal_chunks SET Spawned = random()");
				statement.executeUpdate();
				statement.close();
			}
			
			IO.getConnection().commit();				
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
				
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

			if (overworld.getTime() > 13000 && overworld.getTime() < 23000)
			{
				MLog.debug("Night! Cancelling...");
				Bukkit.getScheduler().runTaskLaterAsynchronously(MantlePlugin.instance, AnimalSpawningTimer.timerSingleton, AnimalSpawningSettings.SPAWNING_INTERVAL_TICKS.integer());
				return;
			}
			
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
	
	public static class ChunkCoordinates
	{
		int x;
		int z;
	}
}

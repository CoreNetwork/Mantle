package com.mcnsa.flatcore.flatcorecommands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.mcnsa.flatcore.CachedSchematic;
import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class GenerateMoreVillagesCommand extends BaseAdminCommand {
	
	public GenerateMoreVillagesCommand()
	{
		desc = "Generate more villages";
		needPlayer = false;
	}


	public Boolean run(CommandSender sender, String[] args) {
		if (args.length < 1 || !Util.isInteger(args[0]))
		{
			sender.sendMessage("Enter number of villages!");
			return true;
		}
		
		Util.Message(Settings.getString(Setting.MESSAGE_SERVER_FROZEN), sender);
		
		int numberOfVillages = Integer.parseInt(args[0]);
		
		World overworld = Bukkit.getServer().getWorlds().get(0);
		long start = System.currentTimeMillis();
		
		Deque<Location> checkedLocations = GriefPreventionHandler.getAllClaims();
		
		getAllVillages(checkedLocations);
				
		int minX = Settings.getInt(Setting.GENERATION_MIN_X);
		int minZ = Settings.getInt(Setting.GENERATION_MIN_Z);
		int maxX = Settings.getInt(Setting.GENERATION_MAX_X);
		int maxZ = Settings.getInt(Setting.GENERATION_MAX_Z);
		
		FCLog.info("Initializing village generation...");
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO villages (Type, CenterX, CenterZ, SizeX, SizeZ) VALUES (?,?,?,?,?)");

			int y = Settings.getInt(Setting.VILLAGE_PASTING_Y);
			int numTypes = Settings.getInt(Setting.NUMBER_OF_VILLAGES);
			CachedSchematic[] villages = new CachedSchematic[numTypes];
			for (int i = 0; i < numTypes; i++)
			{				
				String villageIdString = i >= 9 ? Integer.toString(i + 1) : ("0" + (i + 1));
				String fileName = "village-" + villageIdString + ".schematic";
				
				villages[i] = new CachedSchematic(fileName);
				villages[i].findVillagers();
				
				FCLog.info("Loading schematic for village #" + (i + 1) + " (Size: " + villages[i].xSize + " " + villages[i].zSize + ") ...");
			}
			
			for (int vNum = 0; vNum < numberOfVillages; vNum++)
			{
				Location bestLocation = null;
				int bestDistance = 0;
				for (int i = 0; i < 5000; i++)
				{
					Location loc = new Location(overworld, MCNSAFlatcore.random.nextInt(maxX) + minX, y, MCNSAFlatcore.random.nextInt(maxZ) + minZ);
					int dist = getSmallestDistance(checkedLocations, loc);
					
					if (dist > bestDistance)
					{
						bestDistance = dist;
						bestLocation = loc;
					}
				}
				
				int realDistance = (int) Math.sqrt(bestDistance);
				int type = MCNSAFlatcore.random.nextInt(numTypes);

				if (GriefPreventionHandler.containsClaim(bestLocation.getBlockX() - villages[type].xSize / 2, bestLocation.getBlockZ() - villages[type].zSize / 2, villages[type].xSize, villages[type].zSize, false) || isVillageHere(bestLocation.getBlockX(), bestLocation.getBlockZ()))
				{
					FCLog.severe("Village at " + bestLocation.getBlockX() + " " + bestLocation.getBlockZ() + " (" + realDistance + " blocks away from civilization) would overlap existing structure!");
					FCLog.severe("Something went terribly wrong. Either map is overcrowded or plugin is bugged. Aborting generation.");
					break;
				}
				
				int progress = (int) (vNum * 100.0 / numberOfVillages);
				
				Location spawnLocation;
				spawnLocation = villages[type].place(bestLocation.getBlockX(), y, bestLocation.getBlockZ(), 0);
				villages[type].spawnVillagers(spawnLocation);
				
				statement.setInt(1, type);
				statement.setInt(2, spawnLocation.getBlockX());
				statement.setInt(3, spawnLocation.getBlockZ());
				statement.setInt(4, villages[type].xSize);
				statement.setInt(5, villages[type].zSize);
				statement.addBatch();
						
				checkedLocations.addLast(bestLocation);
				
				if (progress % 5 == 0)
					for (Chunk c : Bukkit.getServer().getWorlds().get(0).getLoadedChunks())
						c.unload(true, true);
				

				
				FCLog.info("Placing new village type #" + type + " at " + bestLocation.getBlockX() + " " + bestLocation.getBlockZ() + " (" + realDistance + " blocks away from civilization) - " + progress + "% done");
			}
			
			statement.executeBatch();
			statement.close();
			IO.getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		
		long end = System.currentTimeMillis();
		FCLog.info("Finished! Total time taken: " + ((end - start) / 1000.0) + "s ");
		return true;
	}
	
	public static int getSmallestDistance(Deque<Location> checkLocations, Location point)
	{
		int smallestDist = Integer.MAX_VALUE;
		
		Iterator<Location> i = checkLocations.iterator();
		while (i.hasNext())
		{
			Location check = i.next();
			int dist = Util.flatDistance(check, point);
			if (dist < smallestDist)
				smallestDist = dist;
		}
		
		return smallestDist;
	}
	
	private static boolean isVillageHere(int x, int z)
	{
		boolean villageExists = false;
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT SizeX,SizeZ,((centerX - ? + sizeX / 2) * (centerX - ? + sizeX / 2) + (centerZ - ? + sizeZ / 2) * (centerZ - ? + sizeZ / 2)) as dist FROM villages ORDER BY dist ASC LIMIT 1");
			statement.setInt(1, x);
			statement.setInt(2, x);
			statement.setInt(3, z);
			statement.setInt(4, z);

			ResultSet set = statement.executeQuery();
			if (set.next())
			{
				int distanceSquared = set.getInt("dist");
				int distance = (int) Math.sqrt(distanceSquared);
				
				int sizeX = set.getInt("SizeX");
				int sizeZ = set.getInt("SizeZ");
						
				if (distance < sizeX || distance < sizeZ)
				{
					villageExists = true;
				}
			}
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return villageExists;
	}
	
	public static void getAllVillages(Collection<Location> list)
	{
		World overworld = Bukkit.getServer().getWorlds().get(0);

		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT CenterX, CenterZ FROM villages");

			ResultSet set = statement.executeQuery();
			while (set.next())
			{
				final int villageX = set.getInt("centerX");
				final int villageZ = set.getInt("centerZ");
							
				Location center = new Location(overworld, villageX, 0, villageZ);
				list.add(center);
			}
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}

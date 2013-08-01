package com.mcnsa.flatcore.generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;

import com.mcnsa.flatcore.CachedSchematic;
import com.mcnsa.flatcore.CachedSchematic.ChestInfo;
import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.generation.StructureData.Protection;
import com.mcnsa.flatcore.restockablechests.RestockableChest;

public class StructureGenerator {
	public static void generateAllWorlds()
	{
		for (World world : Bukkit.getWorlds())
			generateWorld(world);
	}
	
	public static void generateWorld(World world)
	{
		Runtime runtime = Runtime.getRuntime();
		String worldName = world.getName();
		
		MemorySection worldConfig = (MemorySection) GenerationModule.instance.config.get("Worlds." + worldName);
		
		if (worldConfig == null)
		{
			FCLog.info("World " + worldName + " does not have generation config node. Skipping...");
			return;
		}
		
		FCLog.info("Starting generation for world " + worldName);
		
		
		String[] minDimensions = ((String) worldConfig.get("Dimensions.Min")).split(" ");
		String[] maxDimensions = ((String) worldConfig.get("Dimensions.Max")).split(" ");

		int minX = Integer.parseInt(minDimensions[0]);
		int maxX = Integer.parseInt(maxDimensions[0]);
		
		int minZ = Integer.parseInt(minDimensions[1]);
		int maxZ = Integer.parseInt(maxDimensions[1]);
		
		int sizeX = maxX - minX;
		int sizeZ = maxZ - minZ;
		
		FCLog.info("Preparing structures...");

		
		HashMap<Character, StructureData> structures = new HashMap<Character, StructureData>();
		
		MemorySection structuresConfig = (MemorySection) worldConfig.get("Structures");
		for (Entry<String,Object> e : structuresConfig.getValues(false).entrySet())
		{
			StructureData structure = new StructureData(e.getKey(), (MemorySection) e.getValue());
			structures.put(structure.getTextAlias(), structure);
		}
	
		FCLog.info("Preparing textmap...");
		
		File textMapFile = new File(MCNSAFlatcore.instance.getDataFolder(), (String) worldConfig.get("TextmapFileName"));
		
		if (!textMapFile.exists())
		{
			FCLog.info("Text map file for world " + worldName + " does not exist. Skipping...");
			return;
		}
		
		List<String> textMap = new ArrayList<String>();
		
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textMapFile)));
			while (true)
			{
				String line = reader.readLine();
				if (line == null)
					break;
				
				line = line.replace(" ", "{SPACE}");
				line = line.trim();
				line = line.replace("{SPACE}", " ");
				
				textMap.add(line);
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if (textMap.size() == 0)
		{
			FCLog.info("Text map file for world " + worldName + " is empty. Skipping...");
			return;
		}
		
		int rows = textMap.size();
		int columns = textMap.get(0).length();

		for (String row : textMap)
		{
			if (row.length() != columns)
			{
				FCLog.info("Text map file for world " + worldName + " is not properly aligned. Skipping...");
				return;
			}
		}
		
		int tileSizeX = sizeX / columns;
		int tileSizeZ = sizeZ / rows;
		int tiles = columns * rows;
		
		FCLog.info("Size of every letter's tile on the map is " + tileSizeX + "x" + tileSizeZ + ". Structures will be placed into center of each area.");
		
		VillagerSpawner villagerSpawner = new VillagerSpawner();
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO villages (Schematic, World, CornerX, CornerZ, SizeX, SizeZ) VALUES (?,?,?,?,?,?)");

			
			for (int rowNum = 0; rowNum < rows; rowNum++)
			{
				int z = rowNum * tileSizeZ + tileSizeZ / 2 - sizeZ / 2;
				String row = textMap.get(rowNum);
				for (int colNum = 0; colNum < columns; colNum++)
				{
					int x = colNum * tileSizeX + tileSizeX / 2 - sizeX / 2;
					char column = row.charAt(colNum);
					StructureData structure = structures.get(column);
					if (structure != null)
					{
						
						FCLog.info("Pasting " + structure.getName() + " to " + x + "," + z);
						
						int percentage = rowNum * columns + colNum;
						percentage *= 100;
						percentage /= tiles;
						FCLog.info("Row " + (rowNum + 1) + "/" + rows + ", column " + (colNum + 1) + "/" + columns + " [" + percentage + "% total]");

						CachedSchematic schematic = structure.getRandomSchematic();
						
						Location schematicCorner = schematic.place(world, x, structure.getPasteHeight(), z, 0);
						
						if (structure.shouldStoreAsVillage())
						{
							statement.setString(1, schematic.name);
							statement.setString(2, world.getName());
							statement.setInt(3, schematicCorner.getBlockX());
							statement.setInt(4, schematicCorner.getBlockZ());
							statement.setInt(5, schematic.xSize);
							statement.setInt(6, schematic.zSize);
							statement.addBatch();
						}
						
						if (structure.shouldSpawnVillagers())
						{
							schematic.spawnVillagers(schematicCorner, villagerSpawner);
						}
						
						Protection protection = structure.getProtectionData();
						
						List<Location> chestLocations = null;
						if (protection != null && protection.createChestSubclaims)
							chestLocations = new ArrayList<Location>();
						
						if (structure.shouldCreateRestockableChests())
						{
							ChestInfo[] chests = schematic.getChests(schematicCorner);
							for (ChestInfo chest : chests)
							{
								if (chest.restockable)
									RestockableChest.createChest(chest.loc.getBlock(), chest.lootTable, chest.interval, chest.perPlayer);
								
								if (chestLocations != null)
									chestLocations.add(chest.loc);
							}
						}
						
						if (protection != null)
						{
							int protectionSizeX = schematic.xSize + protection.padding * 2;
							int protectionSizeZ = schematic.zSize + protection.padding * 2;
							
							GriefPreventionHandler.secure(schematicCorner, chestLocations, protectionSizeX, protectionSizeZ, protection.claimPermission);
						}
						
						float memLeft = (float) ((float) runtime.freeMemory() / runtime.totalMemory());
						FCLog.debug("MemLeft: " + memLeft);
						if (memLeft < 0.3)
						{
							for (Chunk c : Bukkit.getServer().getWorlds().get(0).getLoadedChunks())
								c.unload(true, true);
							
							System.gc();
						}
						
						

					}
				}
			}
			
			statement.executeBatch();
			statement.close();
			IO.getConnection().commit();			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			villagerSpawner.close();
		}
		
		FCLog.info("Generation finished");
		
	}
	
	
}

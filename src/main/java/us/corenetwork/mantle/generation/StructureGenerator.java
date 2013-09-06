package us.corenetwork.mantle.generation;

import java.awt.image.BufferedImage;
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

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;

import us.corenetwork.mantle.CachedSchematic;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.CachedSchematic.ChestInfo;
import us.corenetwork.mantle.generation.StructureData.Protection;
import us.corenetwork.mantle.restockablechests.RestockableChest;


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
			MLog.info("World " + worldName + " does not have generation config node. Skipping...");
			return;
		}
		
		MLog.info("Starting generation for world " + worldName);
		
		
		String[] minDimensions = ((String) worldConfig.get("Dimensions.Min")).split(" ");
		String[] maxDimensions = ((String) worldConfig.get("Dimensions.Max")).split(" ");

		int minX = Integer.parseInt(minDimensions[0]);
		int maxX = Integer.parseInt(maxDimensions[0]);
		
		int minZ = Integer.parseInt(minDimensions[1]);
		int maxZ = Integer.parseInt(maxDimensions[1]);
		
		int sizeX = maxX - minX;
		int sizeZ = maxZ - minZ;
		
		BufferedImage worldImage = new BufferedImage(sizeX, sizeZ, BufferedImage.TYPE_INT_RGB);
				
		for (int x = 0; x < sizeX; x++)
		{
			for (int z = 0; z < sizeZ; z++)
			{
				worldImage.setRGB(x, z, 0xffffff);
			}
		}
		
		MLog.info("Preparing structures...");

		
		HashMap<Character, StructureData> structures = new HashMap<Character, StructureData>();
		
		MemorySection structuresConfig = (MemorySection) worldConfig.get("Structures");
		for (Entry<String,Object> e : structuresConfig.getValues(false).entrySet())
		{
			StructureData structure = new StructureData(e.getKey(), (MemorySection) e.getValue());
			structures.put(structure.getTextAlias(), structure);
		}
	
		MLog.info("Preparing textmap...");
		
		File textMapFile = new File(MantlePlugin.instance.getDataFolder(), (String) worldConfig.get("TextmapFileName"));
		
		if (!textMapFile.exists())
		{
			MLog.info("Text map file for world " + worldName + " does not exist. Skipping...");
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
			MLog.info("Text map file for world " + worldName + " is empty. Skipping...");
			return;
		}
		
		int rows = textMap.size();
		int columns = textMap.get(0).length();

		for (String row : textMap)
		{
			if (row.length() != columns)
			{
				MLog.info("Text map file for world " + worldName + " is not properly aligned. Skipping...");
				return;
			}
		}
		
		int tileSizeX = sizeX / columns;
		int tileSizeZ = sizeZ / rows;
		int tiles = columns * rows;
		
		MLog.info("Size of every letter's tile on the map is " + tileSizeX + "x" + tileSizeZ + ". Structures will be placed into center of each area.");
		
		VillagerSpawner villagerSpawner = new VillagerSpawner();
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO regeneration_structures (Schematic, StructureName, World, CornerX, CornerZ, PastingY, SizeX, SizeZ) VALUES (?,?,?,?,?,?,?,?)");

			
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
						
						MLog.info("Pasting " + structure.getName() + " to " + x + "," + z);
						
						int percentage = rowNum * columns + colNum;
						percentage *= 100;
						percentage /= tiles;
						MLog.info("Row " + (rowNum + 1) + "/" + rows + ", column " + (colNum + 1) + "/" + columns + " [" + percentage + "% total]");

						CachedSchematic schematic = structure.getRandomSchematic();
						
						Location schematicCorner = schematic.place(world, x, structure.getPasteHeight(), z, 0, structure.shouldIgnoreAir());
						
						schematic.drawBitmap(worldImage, x - minX, z - minZ);
						
						if (structure.shouldStoreAsRespawnable())
						{
							statement.setString(1, schematic.name);
							statement.setString(2, structure.getRespawnableStructureName());
							statement.setString(3, world.getName());
							statement.setInt(4, schematicCorner.getBlockX());
							statement.setInt(5, schematicCorner.getBlockZ());
							statement.setInt(6, structure.getPasteHeight());
							statement.setInt(7, schematic.xSize);
							statement.setInt(8, schematic.zSize);
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
						
						StructureData.WorldGuard region = structure.getWorldGuardData();
						if (region != null)
						{
							Location firstBlock = schematicCorner.clone().add(region.firstBlock.getX(), region.firstBlock.getY(), region.firstBlock.getZ());
							Location secondBlock = schematicCorner.clone().add(region.secondBlock.getX(), region.secondBlock.getY(), region.secondBlock.getZ());

							String name = structure.getName() + "-" + schematicCorner.getX() + "x" + schematicCorner.getZ();
							WorldGuardManager.createRegion(firstBlock, secondBlock, name, region.exampleRegion);
						}
						
						float memLeft = (float) ((float) runtime.freeMemory() / runtime.totalMemory());
						MLog.debug("MemLeft: " + memLeft);
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
		
		File imageFile = new File(MantlePlugin.instance.getDataFolder(), world.getName() + ".png");
		
		try {
			ImageIO.write(worldImage, "png", imageFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		MLog.info("Generation finished");
		
	}
	
	
}
package com.mcnsa.flatcore.generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;

import com.mcnsa.flatcore.CachedSchematic;
import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.MCNSAFlatcore;

public class PathGenerator {
	private static Runtime runtime = Runtime.getRuntime();

	private int startZ;
	private int startX;
	public HashSet<PathTile> visitedTiles = new HashSet<PathTile>();
	private World world;
	private HashMap<Character, StructureData> structures;
	
	public void generatePath(String path)
	{
		
		MemorySection pathConfig = (MemorySection) GenerationModule.instance.config.get("Paths." + path);
		
		if (pathConfig == null)
		{
			FCLog.info("Path " + path + " does not have generation config node. Aborting...");
			return;
		}
		
		FCLog.info("Starting generation for path " + path);
		
		world = Bukkit.getWorld((String) pathConfig.get("World"));
		
		if (world == null)
		{
			FCLog.info("Path " + path + " has invalid world set. Aborting...");
			return;
		}
		
		String[] startPos = ((String) pathConfig.get("Start")).split(" ");

		startX = Integer.parseInt(startPos[0]);
		startZ = Integer.parseInt(startPos[1]);
				
		FCLog.info("Preparing structures...");

		structures = new HashMap<Character, StructureData>();
		
		MemorySection structuresConfig = (MemorySection) pathConfig.get("Structures");
		for (Entry<String,Object> e : structuresConfig.getValues(false).entrySet())
		{
			StructureData structure = new StructureData(e.getKey(), (MemorySection) e.getValue());
			structures.put(structure.getTextAlias(), structure);
		}
	
		FCLog.info("Preparing textmap...");
		
		File textMapFile = new File(MCNSAFlatcore.instance.getDataFolder(), (String) pathConfig.get("TextmapFileName"));
		
		if (!textMapFile.exists())
		{
			FCLog.info("Text map file for path " + path + " does not exist. Aborting...");
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
			FCLog.info("Text map file for path " + path + " is empty. Aborting...");
			return;
		}
		
		int rows = textMap.size();
		int columns = textMap.get(0).length();

		for (String row : textMap)
		{
			if (row.length() != columns)
			{
				FCLog.info("Text map file for path " + path + " is not properly aligned. Aborting...");
				return;
			}
		}
		
		PathTileMap tileMap = new PathTileMap(textMap, rows, columns);
		
		PathTile firstTile = tileMap.tileMap[0][0];
		
		processTile(firstTile, startX, startZ, 1);
		
		FCLog.info("Generation finished");
	}
	
	private void processTile(PathTile tile, int x, int z, int coordCorner)
	{
		if (visitedTiles.contains(tile))
			return;
		
		visitedTiles.add(tile);
		
		StructureData structure = structures.get(tile.structure);
		if (structure == null)
			return;
		
		tile.printTile();

		
		CachedSchematic schematic;
		if (tile.schematic == 0)
			schematic = structure.getRandomSchematic();
		else
			schematic = structure.getSchematic(tile.schematic);

		schematic.rotateTo(tile.rotation);
		
		switch (coordCorner)
		{
		case 3:
			x-= schematic.xSize + 1;
			break;
		case 0:
			z-= schematic.zSize + 1;
			break;
		}
		
		schematic.place(new Location(world, x, structure.getPasteHeight(), z));
		
		float memLeft = (float) ((float) runtime.freeMemory() / runtime.totalMemory());
		FCLog.debug("MemLeft: " + memLeft);
		if (memLeft < 0.3)
		{
			for (Chunk c : Bukkit.getServer().getWorlds().get(0).getLoadedChunks())
				c.unload(true, true);
			
			System.gc();
		}
		
		for (int i = 0; i < 4; i++)
		{
			PathTile neighbour = tile.getNeighbour(i);
			if (neighbour == null)
				continue;
			
			int newX = x;
			int newZ = z;
			
			switch (i)
			{
			case 1:
				newX+= schematic.xSize + 1;
				break;
			case 2:
				newZ+= schematic.zSize + 1;
				break;
			}
			
			processTile(neighbour, newX, newZ, i);
		}
	}
	
	
}

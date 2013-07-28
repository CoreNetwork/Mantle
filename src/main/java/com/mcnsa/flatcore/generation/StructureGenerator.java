package com.mcnsa.flatcore.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.mcnsa.flatcore.FCLog;

public class StructureGenerator {
	public static void generateAllWorlds()
	{
		for (World world : Bukkit.getWorlds())
			generateWorld(world);
	}
	
	public static void generateWorld(World world)
	{
		String worldName = world.getName();
		
		Map<?,?> worldConfig = (Map<?, ?>) GenerationModule.instance.config.get("Worlds." + worldName);
		
		if (worldConfig == null)
		{
			FCLog.info("World " + worldName + " does not have generation config node. Skipping...");
			return;
		}
		
		FCLog.info("Starting generation for world " + worldName);
		
		
		Map<?,?> dimensions = (Map<?,?>) worldConfig.get("Dimensions");
		String[] minDimensions = ((String) dimensions.get("Min")).split(" ");
		String[] maxDimensions = ((String) dimensions.get("Max")).split(" ");

		int minX = Integer.parseInt(minDimensions[0]);
		int maxX = Integer.parseInt(maxDimensions[0]);
		
		int minZ = Integer.parseInt(minDimensions[1]);
		int maxZ = Integer.parseInt(maxDimensions[1]);
		
		FCLog.info("Preparing structures...");

		
		HashMap<Character, StructureData> structures = new HashMap<Character, StructureData>();
		
		Map<?,?> structuresConfig = (Map<?, ?>) worldConfig.get("Structures");
		for (Entry<?,?> e : structuresConfig.entrySet())
		{
			StructureData structure = new StructureData((String) e.getKey(), (Map<?,?>) e.getValue());
			structures.put(structure.getTextAlias(), structure);
		}
		
	}
	
	
}

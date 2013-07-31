package com.mcnsa.flatcore.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.MemorySection;

import com.mcnsa.flatcore.CachedSchematic;
import com.mcnsa.flatcore.FCLog;


public class StructureData {
	private Map<String, CachedSchematic> schematicsMap = new HashMap<String, CachedSchematic>();
	
	private MemorySection configNode;
	private String name;
	
	public StructureData(String name, MemorySection configNode)
	{
		this.name = name;
		this.configNode = configNode;
		
		List<String> schematics = (List<String>) configNode.get("Schematics");
		if (schematics == null)
		{
			FCLog.severe("Structure " + name + " is missing Schematics!");
		}
		
		for (String schematic : schematics)
		{
			if (schematic.startsWith("weights"))
				continue;
		
			FCLog.info("Loading schematic " + schematic + " for structure " + name + "...");
			
			CachedSchematic cs = new CachedSchematic(schematic);
			
			if (shouldSpawnVillagers())
				cs.findVillagers();
			if (shouldCreateRestockableChests())
				cs.findChests();
			
			schematicsMap.put(schematic, cs);
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public Character getTextAlias()
	{
		String textAlias = (String) configNode.get("TextmapAlias");
		if (textAlias == null)
		{
			FCLog.severe("Structure " + name + " is missing TextmapAlias!");
			return null;
		}
		return textAlias.charAt(0);
	}
	
	public boolean shouldStoreAsVillage()
	{
		Boolean result = (Boolean) configNode.get("StoreAsVillage");
		if (result == null)
		{
			return false;
		}
		return result;
	}
	
	public boolean shouldSpawnVillagers()
	{
		Boolean result = (Boolean) configNode.get("SpawnVillagers");
		if (result == null)
		{
			return false;
		}
		return result;
	}
	
	public boolean shouldCreateRestockableChests()
	{
		Boolean result = (Boolean) configNode.get("CreateRestockableChests");
		if (result == null)
		{
			return false;
		}
		return result;
	}
	
	public CachedSchematic getSchematic()
	{
		List<?> schematics = (List<?>) configNode.get("Schematics");
		if (schematics == null)
		{
			FCLog.severe("Structure " + name + " is missing Schematics!");
		}
		
		String pickedSchematic = SchematicNodeParser.pickSchematic(schematics);
		return schematicsMap.get(pickedSchematic);
	}
	
	public int getPasteHeight()
	{
		Integer height = (Integer) configNode.get("PasteHeight");
		if (height == null)
			return 0;
		return height;
	}
	
	public Protection getProtectionData()
	{
		MemorySection protectionNode = (MemorySection) configNode.get("Protection");
		if (protectionNode == null)
			return null;
		
		Protection protection = new Protection();
		
		protection.padding = (Integer) protectionNode.get("Padding");
		if (protection.padding == null)
			protection.padding = 0;
		
		protection.createChestSubclaims = (Boolean) protectionNode.get("ContainerAccessSubclaims");
		if (protection.createChestSubclaims == null)
			protection.createChestSubclaims = false;

		protection.claimPermission = (Integer) protectionNode.get("ClaimPermission");
		
		return protection;
	}
	
	public static class Protection
	{
		Integer claimPermission;
		Integer padding;
		Boolean createChestSubclaims;
		
	}

}

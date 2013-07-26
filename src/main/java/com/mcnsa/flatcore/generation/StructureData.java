package com.mcnsa.flatcore.generation;

import java.util.Map;

import com.mcnsa.flatcore.CachedSchematic;
import com.mcnsa.flatcore.FCLog;


public class StructureData {
	private Map<?,?> configNode;
	private String name;
	
	public StructureData(String name, Map<?,?> configNode)
	{
		this.name = name;
		this.configNode = configNode;
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
		Boolean result = (Boolean) configNode.get("CreateRestockableChests ");
		if (result == null)
		{
			return false;
		}
		return result;
	}
	
	public CachedSchematic getSchematic()
	{
		//TODO
		
		return null;
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
		Map<?,?> protectionNode = (Map<?, ?>) configNode.get("Protection");
		if (protectionNode == null)
			return null;
		
		Protection protection = new Protection();
		
		protection.padding = (Integer) protectionNode.get("Padding");
		if (protection.padding == null)
			protection.padding = 0;
		
		protection.createChestSubclaims = (Boolean) protectionNode.get("ContainerAccessSubclaims");
		if (protection.createChestSubclaims == null)
			protection.createChestSubclaims = false;

		return protection;
	}
	
	public static class Protection
	{
		Integer padding;
		Boolean createChestSubclaims;
		
	}

}

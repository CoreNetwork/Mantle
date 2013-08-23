package us.corenetwork.mantle.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;

import us.corenetwork.mantle.CachedSchematic;
import us.corenetwork.mantle.FCLog;



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
	
	public boolean shouldStoreAsRespawnable()
	{
		return getRespawnableStructureName() != null;
	}
	
	public String getRespawnableStructureName()
	{
		String result = (String) configNode.get("StoreAsRespawnable");
		if (result == null)
		{
			return null;
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
		
	public boolean shouldIgnoreAir()
	{
		Boolean result = (Boolean) configNode.get("IgnoreAir");
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
	
	public CachedSchematic getRandomSchematic()
	{
		List<?> schematics = (List<?>) configNode.get("Schematics");
		if (schematics == null)
		{
			FCLog.severe("Structure " + name + " is missing Schematics!");
		}
		
		String pickedSchematic = SchematicNodeParser.pickSchematic(schematics);
		return schematicsMap.get(pickedSchematic);
	}
	
	public CachedSchematic getSchematic(int selection)
	{
		
		List<?> schematics = (List<?>) configNode.get("Schematics");
		if (schematics == null)
		{
			FCLog.severe("Structure " + name + " is missing Schematics!");
		}

		int counter = 0;
		for (int i = 0; i < schematics.size(); i++)
		{
			Object o = schematics.get(i);
			if (!((String) o).startsWith("weights ")) 
				counter++;
			else
				continue;

			if (counter == selection)
			{
				return schematicsMap.get((String) o);
			}
		}
		
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
	
	public WorldGuard getWorldGuardData()
	{
		MemorySection worldGuardNode = (MemorySection) configNode.get("WorldGuardRegion");
		if (worldGuardNode == null)
			return null;		
		
		WorldGuard wgRegion = new WorldGuard();
		
		String firstBlockString[] = ((String) worldGuardNode.get("FirstBlock")).split(" ");
		String secondBlockString[] = ((String) worldGuardNode.get("SecondBlock")).split(" ");

		wgRegion.exampleRegion = (String) worldGuardNode.get("ExampleRegion");
		
		wgRegion.firstBlock = new Location(null, Integer.parseInt(firstBlockString[0]), Integer.parseInt(firstBlockString[1]), Integer.parseInt(firstBlockString[2]));
		wgRegion.secondBlock = new Location(null, Integer.parseInt(secondBlockString[0]), Integer.parseInt(secondBlockString[1]), Integer.parseInt(secondBlockString[2]));
		
		return wgRegion;
	}
	
	public static class Protection
	{
		Integer claimPermission;
		Integer padding;
		Boolean createChestSubclaims;
	}
	
	public static class WorldGuard
	{
		Location firstBlock;
		Location secondBlock;
		String exampleRegion;
	}


}

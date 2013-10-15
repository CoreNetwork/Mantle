package us.corenetwork.mantle.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;

import us.corenetwork.mantle.CachedSchematic;
import us.corenetwork.mantle.MLog;



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
			MLog.severe("Structure " + name + " is missing Schematics!");
		}
		
		for (String schematic : schematics)
		{
			if (schematic.startsWith("weights"))
				continue;
		
			MLog.info("Loading schematic " + schematic + " for structure " + name + "...");
			
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
			MLog.severe("Structure " + name + " is missing TextmapAlias!");
			return null;
		}
		return textAlias.charAt(0);
	}
	
	public Integer getImageColor()
	{
		String imageColor = (String) configNode.get("ImageMapColor");
		if (imageColor == null)
		{
			MLog.severe("Structure " + name + " is missing ImageMapColor!");
			return null;
		}
		return Integer.parseInt(imageColor, 16);
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
			MLog.severe("Structure " + name + " is missing Schematics!");
		}
		
		String pickedSchematic = SchematicNodeParser.pickSchematic(schematics);
		return schematicsMap.get(pickedSchematic);
	}
	
	public CachedSchematic getSchematic(int selection)
	{
		
		List<?> schematics = (List<?>) configNode.get("Schematics");
		if (schematics == null)
		{
			MLog.severe("Structure " + name + " is missing Schematics!");
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
		
		String firstBlockString = ((String) worldGuardNode.get("FirstBlock"));
		if (firstBlockString == null)
		{
			MLog.severe("Structure " + name + " is missing WorldGuardRegion.FirstBlock! Reverting to auto...");
		}
		else if (!firstBlockString.equalsIgnoreCase("auto"))
		{
			String firstBlockStringSplit[] = firstBlockString.split(" ");
			wgRegion.firstBlock = new Location(null, Integer.parseInt(firstBlockStringSplit[0]), Integer.parseInt(firstBlockStringSplit[1]), Integer.parseInt(firstBlockStringSplit[2]));
		}
		
		String secondBlockString = ((String) worldGuardNode.get("SecondBlock"));
		if (secondBlockString == null)
		{
			MLog.severe("Structure " + name + " is missing WorldGuardRegion.SecondBlock! Reverting to auto...");
		}
		else if (!secondBlockString.equalsIgnoreCase("auto"))
		{
			String secondBlockStringSplit[] = secondBlockString.split(" ");
			wgRegion.secondBlock = new Location(null, Integer.parseInt(secondBlockStringSplit[0]), Integer.parseInt(secondBlockStringSplit[1]), Integer.parseInt(secondBlockStringSplit[2]));
		}
		
		wgRegion.padding = worldGuardNode.getInt("Padding", 0);
		
		wgRegion.exampleRegion = (String) worldGuardNode.get("ExampleRegion");		
		
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
		Integer padding;
	}


}

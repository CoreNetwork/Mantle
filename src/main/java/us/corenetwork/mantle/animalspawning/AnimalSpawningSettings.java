package us.corenetwork.mantle.animalspawning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public enum AnimalSpawningSettings {
	CHUNK_MIN_X("Chunks.MinX", -500),
	CHUNK_MIN_Z("Chunks.MinZ", -500),
	CHUNK_MAX_X("Chunks.MaxX", 500),
	CHUNK_MAX_Z("Chunks.MaxZ", 500),

    SPAWNING_WORLD("AnimalSpawningWorld", "world"),

	PREVENT_SPAWNING_ANIMALS("PreventSpawningAnimals", Arrays.asList("Cow", "Horse", "Pig", "Sheep")),

	SPAWNING_INTERVAL_TICKS("SpawningIntervalTicks", 100),
	CHUNKS_SPAWNING_AMOUNT("ChunksSpawningAmount", 10),
	
	MIN_ADDITIONAL_PACK_MOBS("AdditionalPackMobs.Min", 1),
	MAX_ADDITIONAL_PACK_MOBS("AdditionalPackMobs.Max", 3),
	
	RANGES("Ranges", new ArrayList<Map<String, Integer>>(){{
		add(new HashMap<String, Integer>(){{
			put("StartChunk", 1);
			put("EndChunk", 100);
			put("Weight", 3);
		}});
		add(new HashMap<String, Integer>(){{
			put("StartChunk", 101);
			put("EndChunk", 300);
			put("Weight", 2);
		}});
		add(new HashMap<String, Integer>(){{
			put("StartChunk", 301);
			put("EndChunk", 500);
			put("Weight", 1);
		}});
	}});
	protected String string;
	protected Object def;
	
	private AnimalSpawningSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) AnimalSpawningModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) AnimalSpawningModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) AnimalSpawningModule.instance.config.get(string, def);
	}
	
	public List<String> stringList()
	{
		return (List<String>) AnimalSpawningModule.instance.config.get(string, def);
	}
	
	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;
		
		Object descO = AnimalSpawningModule.instance.config.get(path);
		if (descO == null)
		{
			AnimalSpawningModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			AnimalSpawningModule.instance.saveConfig();
			descO = AnimalSpawningModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

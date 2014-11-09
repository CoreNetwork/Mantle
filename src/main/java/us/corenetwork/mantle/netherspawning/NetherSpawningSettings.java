package us.corenetwork.mantle.netherspawning;

import java.util.Arrays;
import java.util.List;

public enum NetherSpawningSettings {
    NETHER_WORLD("NetherWorld", "world_nether"),
	PREVENT_SPAWNING_NETHER("PreventSpawningNether", Arrays.asList("Skeleton", "Blaze")),

	SPAWNING_INTERVAL_TICKS("SpawningIntervalTicks", 100),

	NEAREST_PLAYER_MINIMUM_DISTANCE_SQUARED("MinimumDistanceToPlayerSquared", 625),
    FARTHEST_PLAYER_MAXIMUM_DISTANCE_SQUARED("MaximumDistanceToPlayerSquared", 9216),


    BLAZE_CHANCE("Blaze.SpawningChance", 0.6),
	BLAZE_MAX_Y("Blaze.MaxY", 60),

    WITHER_SKELETON_MAX_Y("WitherSkeleton.MaxY", 56),
	WITHER_SKELETON_STRENGTH("WitherSkeleton.Strength", 4.0),
	WITHER_SKELETON_RARE_BOW_CHANCE("WitherSkeleton.Normal.BowChance", 0.03),
	WITHER_SKELETON_RARE_MAX_SPAWN_Y("WitherSkeleton.Rare.MaxSpawnY", 60),
	WITHER_SKELETON_NORMAL_SPEED("WitherSkeleton.Normal.Speed", 0.25),	
	
	MIN_ADDITIONAL_PACK_MOBS("AdditionalPackMobs.Min", 1),
	MAX_ADDITIONAL_PACK_MOBS("AdditionalPackMobs.Max", 3);

    
	protected String string;
	protected Object def;
	
	private NetherSpawningSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) NetherSpawningModule.instance.config.get(string, def)).doubleValue();
	}

	public Integer integer()
	{
		return (Integer) NetherSpawningModule.instance.config.get(string, def);
	}

	public String string()
	{
		return (String) NetherSpawningModule.instance.config.get(string, def);
	}
	
	public List<String> stringList()
	{
		return (List<String>) NetherSpawningModule.instance.config.get(string, def);
	}

	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;

		Object descO = NetherSpawningModule.instance.config.get(path);
		if (descO == null)
		{
			NetherSpawningModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			NetherSpawningModule.instance.saveConfig();
			descO = NetherSpawningModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

package us.corenetwork.mantle.netherspawning;

import java.util.Arrays;
import java.util.List;

public enum NetherSpawningSettings {
    NETHER_WORLD("NetherWorld", "world_nether"),
	PREVENT_SPAWNING_NETHER("PreventSpawningNether", Arrays.asList("Skeleton", "Blaze")),

	NEAREST_PLAYER_MINIMUM_DISTANCE_SQUARED("MinimumDistanceToPlayerSquared", 625),
    FARTHEST_PLAYER_MAXIMUM_DISTANCE_SQUARED("MaximumDistanceToPlayerSquared", 9216),

    BLAZE_SPAWNING_INTERVAL_TICKS("Blaze.SpawningIntervalTicks", 10),

    MAGMA_CUBE_SPAWNING_INTERVAL_TICKS("MagmaCube.SpawningIntervalTicks", 10),
    MAGMA_CUBE_MAX_Y("MagmaCube.MaxY", 256),
    MAGMA_CUBE_MIN_Y("MagmaCube.MinY", 0),

    WITHER_SKELETON_SPAWNING_INTERVAL_TICKS("WitherSkeleton.SpawningIntervalTicks", 10),
	WITHER_SKELETON_SPAWN_CHANCE("WitherSkeleton.SpawnChance", 1),
	WITHER_SKELETON_MIN_Y("WitherSkeleton.MinY", 1),
	WITHER_SKELETON_MAX_Y("WitherSkeleton.MaxY", 56),
	WITHER_SKELETON_STRENGTH("WitherSkeleton.Strength", 4.0),
	WITHER_SKELETON_RARE_BOW_CHANCE("WitherSkeleton.BowChance", 0.03),
	WITHER_SKELETON_NORMAL_SPEED("WitherSkeleton.Speed", 0.25),

    GHAST_SPAWNING_INTERVAL_TICKS("Ghast.SpawningIntervalTicks", 10),
	GHAST_SPAWN_CHANCE("Ghast.SpawnChance", 1),
    GHAST_LIGHT_CHECK_Y("Ghast.LightCheckY", 183),
    GHAST_MIN_SPAWN_Y("Ghast.MinSpawnY", 163),
	GHAST_MAX_SPAWN_Y("Ghast.MaxSpawnY", 179),
	GHAST_MIN_MOVE_DOWN("Ghast.MinMoveDown", 4),
	GHAST_MAX_MOVE_DOWN("Ghast.MaxMoveDown", 6),

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

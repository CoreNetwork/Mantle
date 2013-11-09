package us.corenetwork.mantle.netherspawning;


public enum NetherSpawningSettings {
    NETHER_WORLD("NetherWorld", "world_nether"),
    	
	SPAWNING_INTERVAL_TICKS("SpawningIntervalTicks", 100),

	NEAREST_PLAYER_MINIMUM_DISTANCE_SQUARED("MinimumDistanceToPlayerSquared", 625),
	
	BLAZE_CHANCE("Blaze.SpawningChance", 0.6),
	BLAZE_MAX_Y("Blaze.MaxY", 60),

	WITHER_SKELETON_STRENGTH("WitherSkeleton.Strength", 4.0),
	WITHER_SWORD_CHANCE("WitherSkeleton.Sword.Chance", 0.03),
	WITHER_SWORD_MAX_Y("WitherSkeleton.Sword.MaxY", 60),
	WITHER_NOSWORD_SPEED("WitherSkeleton.NoSword.Speed", 0.25),	
	
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

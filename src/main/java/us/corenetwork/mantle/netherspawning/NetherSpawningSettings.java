package us.corenetwork.mantle.netherspawning;


public enum NetherSpawningSettings {
    NETHER_WORLD("NetherWorld", "world_nether"),

    MIN_DISTANCE_PLAYER("DistanceToPlayerSquared.Min", 100),
    MAX_DISTANCE_PLAYER("DistanceToPlayerSquared.Max", 900),

	SPAWNING_INTERVAL_TICKS("SpawningIntervalTicks", 100),

	BLAZE_CHANCE("BlazeSpawningChance", 0.6),
	WITHER_SWORD_CHANCE("WitherSwordChance", 0.03),
		
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

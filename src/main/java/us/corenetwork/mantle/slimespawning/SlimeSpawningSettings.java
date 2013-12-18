package us.corenetwork.mantle.slimespawning;

import java.util.List;

public enum SlimeSpawningSettings {
    OVERWORLD_NAME("SpawningWorld", "world"),
	SPAWNING_INTERVAL_TICKS("SpawningIntervalTicks", 100),
	SLIME_KILL_TIME("SlimeKillTime", 13000),

	NEAREST_PLAYER_MINIMUM_DISTANCE_SQUARED("MinimumDistanceToPlayerSquared", 625),
		
	MIN_ADDITIONAL_PACK_MOBS("AdditionalPackMobs.Min", 1),
	MAX_ADDITIONAL_PACK_MOBS("AdditionalPackMobs.Max", 3);
    
	protected String string;
	protected Object def;
	
	private SlimeSpawningSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) SlimeSpawningModule.instance.config.get(string, def)).doubleValue();
	}

	public Integer integer()
	{
		return (Integer) SlimeSpawningModule.instance.config.get(string, def);
	}

	public String string()
	{
		return (String) SlimeSpawningModule.instance.config.get(string, def);
	}
	
	public List<String> stringList()
	{
		return (List<String>) SlimeSpawningModule.instance.config.get(string, def);
	}

	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;

		Object descO = SlimeSpawningModule.instance.config.get(path);
		if (descO == null)
		{
			SlimeSpawningModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			SlimeSpawningModule.instance.saveConfig();
			descO = SlimeSpawningModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

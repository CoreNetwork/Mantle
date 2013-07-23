package com.mcnsa.flatcore.hardmode;


public enum HardmodeSettings {

	ENDERMAN_TELEPORT_CHANCE("EndermanTeleport.Chance", 0.25),
	ENDERMAN_TELEPORT_RANGE("EndermanTeleport.Range", 30),
	ENDERMAN_TELEPORT_EXCLUDING_ZONE("EndermanTeleport.ExcludingZone", 10),
	ENDERMAN_TELEPORT_MAX_TRIES("EndermanTeleport.MaxTries", 3),
	ENDERMAN_TELEPORT_APPLY_DAMAGE_EFFECT("EndermanTeleport.ApplyDamageEffect", "EndermanTeleport"),
	
	NETHERRACK_FIRE_CHANCE("HardMode.NetherrackFireChance", 20),
	PLAYER_PUNCH_FIRE_DURATION("HardMode.PlayerPunchFireDuration", 35),
	ZOMBIE_RESPAWN_CHANCE("HardMode.ZombieRespawnChance", 20);

	
	protected String string;
	protected Object def;
	
	private HardmodeSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) HardmodeModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) HardmodeModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) HardmodeModule.instance.config.get(string, def);
	}
	
	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;
		
		Object descO = HardmodeModule.instance.config.get(path);
		if (descO == null)
		{
			HardmodeModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			HardmodeModule.instance.saveConfig();
			descO = HardmodeModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

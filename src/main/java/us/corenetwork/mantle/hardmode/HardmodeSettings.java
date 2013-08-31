package us.corenetwork.mantle.hardmode;

import java.util.Arrays;
import java.util.List;


public enum HardmodeSettings {

	ENDERMAN_TELEPORT_CHANCE("EndermanTeleport.Chance", 0.25),
	ENDERMAN_TELEPORT_RANGE("EndermanTeleport.Range", 30),
	ENDERMAN_TELEPORT_EXCLUDING_ZONE("EndermanTeleport.ExcludingZone", 10),
	ENDERMAN_TELEPORT_MAX_TRIES("EndermanTeleport.MaxTries", 3),
	ENDERMAN_TELEPORT_APPLY_DAMAGE_EFFECT("EndermanTeleport.ApplyDamageEffect", "EndermanTeleport"),
	
	ZOMBIE_RESPAWN_CHANCE("ZombieRespawnChance", 20),
		
	GHAST_SPAWNING_CHANCE("GhastSpawningChance", 0.2),
	
	WITHER_MIN_SPAWNING_Y("Wither.MinSpawningY", 20),
	WITHER_DESPAWNING_Y("Wither.DespawningY", 60),
	WITHER_TIMEOUT("Wither.DespawningTimeoutSeconds", 120),
	
	NAMED_MOBS_NO_DROP("NoDropMobNames", Arrays.asList(new String[] {"Guard" })),
	
	MESSAGE_NO_WITHER_SURFACE("Messages.NoWitherSurface", "Sorry, you can't build wither on the surface!"),
	MESSAGE_NO_MILKING_NETHER("Messages.NoMilkingNether", "Sorry, you can't milk cows in nether!");
	
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
		
	public List<String> stringList()
	{
		return (List<String>) HardmodeModule.instance.config.get(string, def);
	}
}

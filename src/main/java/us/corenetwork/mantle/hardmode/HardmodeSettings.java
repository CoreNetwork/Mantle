package us.corenetwork.mantle.hardmode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public enum HardmodeSettings {

	ENDERMAN_TELEPORT_CHANCE("EndermanTeleport.Chance", 0.25),
	ENDERMAN_TELEPORT_RANGE("EndermanTeleport.Range", 30),
	ENDERMAN_TELEPORT_EXCLUDING_ZONE("EndermanTeleport.ExcludingZone", 10),
	ENDERMAN_TELEPORT_MAX_TRIES("EndermanTeleport.MaxTries", 3),
	ENDERMAN_TELEPORT_APPLY_DAMAGE_EFFECT("EndermanTeleport.ApplyDamageEffect", "EndermanTeleport"),
	
	GHAST_SPAWNING_CHANCE("Ghast.SpawningChance", 0.2),
	GHAST_MINIMUM_SPAWNING_Y("Ghast.MinimumSpawningY", 80),
	GHAST_MAXIMUM_ATTACK_RANGE("Ghast.MaxmimumHorizontalTargetRangeSquared", 70 * 70),
	GHAST_FIREBALL_DAMAGE_MULTIPLIER("Ghast.Fireball.DamageMultiplier", 4.0),
	GHAST_FIREBALL_BLAST_RADIUS_MULTIPLIER("Ghast.Fireball.BlastRadiusMultiplier", 2.0),

	WITHER_MIN_SPAWNING_Y("Wither.MinSpawningY", 20),
	WITHER_DESPAWNING_Y("Wither.DespawningY", 60),
	WITHER_TIMEOUT("Wither.DespawningTimeoutSeconds", 120),
	WITHER_MINION_HEALTH("Wither.MinionHealth", 4),
	WITHER_EXPLOSION_RADIUS("Wither.ExplosionRadius", 20),
	WITHER_SPAWNING_PHASE_DURATION("Wither.SpawningPhaseLength", 220),

	WITHER_DEBUG("Wither.Debug", false),

	WITHER_DELAY_BETWEEN_SKILLS_AMOUNTS("Wither.DelayBetweenSkillsAmounts", new Integer[]{80,70,60,50,40,30,20,10}),

	WITHER_MANA_REGEN("Wither.Mana.Regen", 0.01),
	WITHER_MANA_MAX_AMOUNTS("Wither.Mana.MaxAmounts", new Integer[]{1500, 2000, 2200, 2400, 2600, 2700}),

	WITHER_SHIELD_REGEN("Wither.Shield.Regen", 0.01),
	WITHER_SHIELD_MAX_AMOUNTS("Wither.Shield.MaxAmounts", new Integer[]{1500, 2000, 2200, 2400, 2600, 2700}),

	WITHER_HEALTH_REGEN("Wither.Health.Regen", 0.03),
	WITHER_HEALTH_MAX_AMOUNTS("Wither.Health.MaxAmounts", new Integer[]{300, 400, 500, 550, 600, 700}),

	WITHER_BASE_DMG("Wither.BaseDmg", 4.5),

	//-- normal black skull attack --
	WITHER_BS_SEARCH_HORIZ("Wither.BlackSkull.SearchHoriz", 30),
	WITHER_BS_SEARCH_VERT("Wither.BlackSkull.SearchVert", 10),
	WITHER_BS_SHOOT_MAX_DISTANCE("Wither.BlackSkull.ShootMaxDistance", 30),
	WITHER_BS_SHOOT_BASIC_TIME("Wither.BlackSkull.ShootBasicTime", 50),
	WITHER_BS_SHOOT_TIME_VARIANCE("Wither.BlackSkull.ShootTimeVariance", 10),
	WITHER_BS_RE_SEARCH_TIME("Wither.BlackSkull.ReSearchTime", 50),


	//-- basic moves --
	WITHER_PH_SA_MOVE_BASIC_TIME("Wither.Phases.StationaryArtillery.MoveBasicTime", 100),
	WITHER_PH_SA_MOVE_TIME_VARIANCE("Wither.Phases.StationaryArtillery.MoveTimeVariance", 30),
	WITHER_PH_SA_MIN_VERTICAL("Wither.Phases.StationaryArtillery.MinVertical", 3),
	WITHER_PH_SA_MAX_VERTICAL("Wither.Phases.StationaryArtillery.MaxVertical", 10),
	WITHER_PH_SA_MAX_HORIZONTAL("Wither.Phases.StationaryArtillery.MinHorizontal", 3),
	WITHER_PH_SA_MIN_HORIZONTAL("Wither.Phases.StationaryArtillery.MaxHorizontal", 5),
	WITHER_PH_SA_COOLDOWN("Wither.Phases.StationaryArtillery.Cooldown", 0),
	WITHER_PH_SA_MANACOST("Wither.Phases.StationaryArtillery.ManaCost", 0),
	WITHER_PH_SA_NORMALATTACK("Wither.Phases.StationaryArtillery.NormalAttack", true),

	//-- advanced moves --
	//--- wither aura ---
	WITHER_PH_WA_DISTANCE_FROM_WITHER("Wither.Phases.WitherAura.DistFromWither",4),
	WITHER_PH_WA_SEGMENTS_PER_SHOT("Wither.Phases.WitherAura.SegmentsPerShot", 10),
	WITHER_PH_WA_SKULLS_PER_SEGMENT("Wither.Phases.WitherAura.SkullsPerSegment", 3),
	WITHER_PH_WA_DELAY_BETWEEN("Wither.Phases.WitherAura.DelayBetweenShots", 20),
	WITHER_PH_WA_NUM_OF_SHOTS("Wither.Phases.WitherAura.NumberOfShots", 10),
	WITHER_PH_WA_CIRCLE_SEGMENTS("Wither.Phases.WitherAura.CircleSegments", 120),
	WITHER_PH_WA_MAX_ANGLE_FORWARD("Wither.Phases.WitherAura.MaxAngleForward", 60),
	WITHER_PH_WA_MAX_ANGLE_BACKWARDS("Wither.Phases.WitherAura.MaxAngleBackward", 30),

	WITHER_PH_WA_COOLDOWN("Wither.Phases.WitherAura.Cooldown", 600),

	WITHER_PH_WA_MANACOST("Wither.Phases.WitherAura.ManaCost", 150),
	WITHER_PH_WA_NORMALATTACK("Wither.Phases.WitherAura.NormalAttack", true),
	//WITHER_PH_WA_("Wither.Phases.WitherAura."),

	//--- acid cloud ---

	WITHER_PH_AC_RANGE("Wither.Phases.AcidCloud.Range", 5),

	WITHER_PH_AC_COOLDOWN("Wither.Phases.AcidCloud.Cooldown", 400),
	WITHER_PH_AC_MANACOST("Wither.Phases.AcidCloud.ManaCost", 400),
	WITHER_PH_AC_NORMALATTACK("Wither.Phases.AcidCloud.NormalAttack", false),
	//WITHER_PH_AC_("Wither.Phases.AcidCloud."),


	NAMED_MOBS_NO_DROP("NoDropMobNames", Arrays.asList(new String[] {"Guard" })),
	
	APPLY_DAMAGE_NODE_ON_PIGMEN_SPAWN("Pigmen.ApplyDamageNodeOnSpawn", "SlownessNode"),
	PIGMEN_SWORD_CHANCE("Pigmen.SwordChance", 0.05),
	
	NETHER_MAX_SPAWN_LIGHT_LEVEL("Nether.MaxSpawnLightLevel", 7),

    NO_DEATH_DROPS_EXPERIENCE("NoDeathDrops.Experience", new ArrayList<String>()),
    NO_DEATH_DROPS_ITEMS("NoDeathDrops.Items", new ArrayList<String>()),

    MESSAGE_NO_WITHER_SURFACE("Messages.NoWitherSurface", "Sorry, you can't build wither on the surface!"),
	MESSAGE_NO_MILKING_NETHER("Messages.NoMilkingNether", "Sorry, you can't milk cows in nether!"),

    REINFORCEMENTS_DISTANCE("Reinforcements.Distance", 10);
	
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
	public float floatNumber()
	{
		return ((Number) HardmodeModule.instance.config.get(string, def)).floatValue();
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

	public List<Integer> intList()
	{
		return HardmodeModule.instance.config.getIntegerList(string);
	}
	public Boolean bool()
	{
		return (Boolean) HardmodeModule.instance.config.get(string, def);
	}

}

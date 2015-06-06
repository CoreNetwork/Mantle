package us.corenetwork.mantle.hardmode;

import org.bukkit.entity.Wither;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public enum HardmodeSettings {

	ENDERMAN_TELEPORT_CHANCE("EndermanTeleport.Chance", 0.25),
	ENDERMAN_TELEPORT_RANGE("EndermanTeleport.Range", 30),
	ENDERMAN_TELEPORT_EXCLUDING_ZONE("EndermanTeleport.ExcludingZone", 10),
	ENDERMAN_TELEPORT_MAX_TRIES("EndermanTeleport.MaxTries", 3),
	ENDERMAN_TELEPORT_APPLY_DAMAGE_EFFECT("EndermanTeleport.ApplyDamageEffect", "EndermanTeleport"),
	
	GHAST_MAXIMUM_ATTACK_RANGE("Ghast.MaxmimumHorizontalTargetRangeSquared", 70 * 70),
	GHAST_FIREBALL_DAMAGE_MULTIPLIER("Ghast.Fireball.DamageMultiplier", 4.0),
	GHAST_FIREBALL_BLAST_RADIUS_MULTIPLIER("Ghast.Fireball.BlastRadiusMultiplier", 2.0),

    BABY_ZOMBIE_BURN("BabyZombies.Burn", false),
    BABY_ZOMBIE_CHECK_INTERVAL("BabyZombies.CheckInterval", 20),
    BABY_ZOMBIE_BURN_TICKS("BabyZombies.BurnTicks", 30),

	WITHER_BOX_UNDER_SIZE("Wither.BoxSizeUnderExplosion", 30),
	WITHER_MIN_SPAWNING_Y("Wither.MinSpawningY", 20),
	WITHER_MAX_SPAWNING_Y("Wither.MaxSpawningY", 20),
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

	WITHER_HEALTH_REGEN("Wither.Health.Regen", 0.01),
	WITHER_HEALTH_MAX_AMOUNTS("Wither.Health.MaxAmounts", new Integer[]{300, 400, 500, 550, 600, 700}),

	WITHER_SHIELD_COLOR("Wither.ShieldColor", "&6"),
	WITHER_NAMES("Wither.Names", new ArrayList<String>(){{add( "Wither");add("| Wihter |");add("|| Wihter ||");add("||| Wihter |||");add("|||| Wither ||||");add("||||| Wither |||||");}}),
	WITHER_BASE_DMG("Wither.BaseDmg", 4.5),
	WITHER_KNOCKBACK_POWER("Wither.KnockbackPower", 1),
	//-- normal black skull attack --
	WITHER_BS_SEARCH_HORIZ("Wither.BlackSkull.SearchHoriz", 60),
	WITHER_BS_SEARCH_VERT("Wither.BlackSkull.SearchVert", 30),
	WITHER_BS_SHOOT_MAX_DISTANCE("Wither.BlackSkull.ShootMaxDistance", 200),
	WITHER_BS_SHOOT_BASIC_TIME("Wither.BlackSkull.ShootBasicTime", 30),
	WITHER_BS_SHOOT_TIME_VARIANCE("Wither.BlackSkull.ShootTimeVariance", 10),
	WITHER_BS_RE_SEARCH_TIME("Wither.BlackSkull.ReSearchTime", 50),
	WITHER_BS_RADIUS("Wither.BlackSkull.Radius", 1.5),

	//-- basic move --
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
	WITHER_PH_WA_DAMAGE_MULTIPLIER("Wither.Phases.WitherAura.DamageMultiplier", 1.1),
	WITHER_PH_WA_RADIUS_MULTIPLIER("Wither.Phases.WitherAura.RadiusMultiplier", 0.5),

	WITHER_PH_WA_COOLDOWN("Wither.Phases.WitherAura.Cooldown", 600),
	WITHER_PH_WA_MANACOST("Wither.Phases.WitherAura.ManaCost", 150),
	WITHER_PH_WA_NORMALATTACK("Wither.Phases.WitherAura.NormalAttack", true),
	//WITHER_PH_WA_("Wither.Phases.WitherAura."),

	//--- acid cloud ---

	WITHER_PH_AC_RANGE("Wither.Phases.AcidCloud.Range", 5),
	WITHER_PH_AC_DURATION("Wither.Phases.AcidCloud.Duration", 400),

	WITHER_PH_AC_COOLDOWN("Wither.Phases.AcidCloud.Cooldown", 400),
	WITHER_PH_AC_MANACOST("Wither.Phases.AcidCloud.ManaCost", 400),
	WITHER_PH_AC_NORMALATTACK("Wither.Phases.AcidCloud.NormalAttack", false),
	WITHER_PH_AC_PARTICLE("Wither.Phases.AcidCloud.Particle", 29),
	WITHER_PH_AC_PARTICLE_REFRESH_RATE("Wither.Phases.AcidCloud.ParticleRefreshRate", 5),
	WITHER_PH_AC_PARTICLE_AMOUT("Wither.Phases.AcidCloud.ParticleAmout", 10),
	WITHER_PH_AC_DEBUFF_REFRESH_RATE("Wither.Phases.AcidCloud.DebuffRefreshRate", 20),
	WITHER_PH_AC_DEBUFF_DURA_REMOVED("Wither.Phases.AcidCloud.DebuffDurabilityRemoved",  new Integer[]{6, 5, 4, 3, 2}),
	WITHER_PH_AC_DEBUFF_HUNGER("Wither.Phases.AcidCloud.DebuffHunger", true),
	//WITHER_PH_AC_("Wither.Phases.AcidCloud."),


	//--- minions ---
	WITHER_PH_MI_COOLDOWN("Wither.Phases.Minions.Cooldown", 400),
	WITHER_PH_MI_MANACOST("Wither.Phases.Minions.ManaCost", 400),
	WITHER_PH_MI_NORMALATTACK("Wither.Phases.Minions.NormalAttack", true),

	WITHER_PH_MI_DURATION("Wither.Phases.Minions.Duration", 400),
	WITHER_PH_MI_MINION_SPAWN_RADIUS_MIN("Wither.Phases.Minions.SpawnRadiusMin", 3),
	WITHER_PH_MI_MINION_SPAWN_RADIUS_MAX("Wither.Phases.Minions.SpawnRadiusMax", 7),
	//WITHER_PH_MI_("Wither.Phase.Minions")

	//--- stomp ---
	WITHER_PH_ST_COOLDOWN("Wither.Phases.Stomp.Cooldown", 400),
	WITHER_PH_ST_MANACOST("Wither.Phases.Stomp.ManaCost", 400),
	WITHER_PH_ST_NORMALATTACK("Wither.Phases.Stomp.NormalAttack", false),

	WITHER_PH_ST_DAMAGE_MULTIPLIER("Wither.Phases.Stomp.DamageMultiplier", 2),

	WITHER_PH_ST_GROUND_TIME("Wither.Phases.Stomp.GroundTime",80),
	WITHER_PH_ST_MIN_FOLLOWING_TIME("Wither.Phases.Stomp.MinFollowingTime",80),
	WITHER_PH_ST_MAX_FOLLOWING_TIME("Wither.Phases.Stomp.MaxFollowingTime",800),

	WITHER_PH_ST_STOMP_MAX_DISTANCE("Wither.Phases.Stomp.StompMaxDistance",4),
	WITHER_PH_ST_DRILL_MAX_DISTANCE_FLAT("Wither.Phases.Stomp.DrillMaxDistanceFlat", 6),


	//WITHER_PH_ST_("Wither.Phase.Stomp.",),


	NAMED_MOBS_NO_DROP("NoDropMobNames", Arrays.asList(new String[] {"Guard" })),
	
	APPLY_DAMAGE_NODE_ON_PIGMEN_SPAWN("Pigmen.ApplyDamageNodeOnSpawn", "SlownessNode"),
	PIGMEN_SWORD_CHANCE("Pigmen.SwordChance", 0.05),
	
	NETHER_MAX_SPAWN_LIGHT_LEVEL("Nether.MaxSpawnLightLevel", 7),

    NO_DEATH_DROPS_EXPERIENCE("NoDeathDrops.Experience", new ArrayList<String>()),
    NO_DEATH_DROPS_ITEMS("NoDeathDrops.Items", new ArrayList<String>()),

	ZOMBIE_DOOR_BREAKING_CHANCE("ZombieDoorBreakingChance", 1),

    MESSAGE_NO_WITHER_SURFACE("Messages.NoWitherSurface", "Sorry, you can't build wither on the surface!"),
	MESSAGE_NO_MILKING_NETHER("Messages.NoMilkingNether", "Sorry, you can't milk cows in nether!"),

    SKELETON_FIRST_ARROW_DELAY_MULTIPLIER("Skeleton.FirstArrowDelayMultiplier", 0.3),

    WITCH_FIRST_POTION_DELAY_MULTIPLIER("Witch.FirstPotionDelayMultiplier", 0.3),

    ANIMALS_AI_MAXIMUM_RANGE_TO_PLAYER("Animals.AIMaximumPlayerDistance", 32),
    ANIMALS_ENABLE_AI_NERF("Animals.EnableAINerf", true),

    REINFORCEMENTS_DISTANCE("Reinforcements.Distance", 10),
    REINFORCEMENTS_ENABLED("Reinforcements.Enabled", false),
    VEHICLE_PLAYER_SYNC("VehiclePlayerSync", true);
	
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

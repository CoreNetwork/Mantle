package com.mcnsa.flatcore;

import org.bukkit.Material;




public enum Setting {
	MAP_MIN_X("Map.MinX", -10000),
	MAP_MIN_Z("Map.MinZ", -10000),
	MAP_MAX_X("Map.MaxX", 10000),
	MAP_MAX_Z("Map.MaxZ", 10000),
	GENERATION_MIN_X("Generation.MinX", -10000),
	GENERATION_MIN_Z("Generation.MinZ", -10000),
	GENERATION_MAX_X("Generation.MaxX", 10000),
	GENERATION_MAX_Z("Generation.MaxZ", 10000),
	MAP_PORTAL_MIN_Y("Map.PortalMinY", 10),
	MAP_PORTAL_MAX_Y("Map.PortalMaxY", 100),
	MAP_MOVE_PORTALS_WITH_LOWER_Y("Map.MovePortalsWithLowerY", 20),
	MAP_MOVE_PORTALS_WITH_HIGHER_Y("Map.MovePortalsWithHigherY", 300),
	NETHER_MAX_X("Map.NetherMaxX", 625),
	NETHER_MIN_X("Map.NetherMinX", -625),
	NETHER_MAX_Z("Map.NetherMaxZ", 625),
	NETHER_MIN_Z("Map.NetherMinZ", -625),
	NETHER_PORTAL_MIN_Y("Nether.PortalMinY", 10),
	NETHER_MOVE_PORTALS_WITH_LOWER_Y("Nether.MovePortalsWithLowerY", 20),
	NETHER_MOVE_PORTALS_WITH_HIGHER_Y("Nether.MovePortalsWithHigherY", 120),
	NETHER_PORTAL_MAX_Y("Nether.PortalMaxY", 100),
	
	TELEPORT_Y("TeleportY", 14),
	
	VILLAGE_GRID_SPACE("Village.Spacing", 500),
	VILLAGE_RANDOM_OFFSET("Village.RandomOffset", 150),
	NUMBER_OF_VILLAGES("Village.NumberOfSchematics", 6),
	VILLAGE_PASTING_Y("Village.PastingY", 1),
	VILLAGE_OFFSET_X("Village.OffsetX", 0),
	VILLAGE_OFFSET_Z("Village.OffsetZ", 0),
	
	CAMPFIRE_GRID_SPACE("Campfire.Spacing", 500),
	CAMPFIRE_PASTING_Y("Campfire.PastingY", 1),
	CAMPFIRE_RANDOM_OFFSET("Campfire.RandomOffset", 150),
	CAMPFIRE_OFFSET_X("Campfire.OffsetX", 250),
	CAMPFIRE_OFFSET_Z("Campfire.OffsetZ", 250),
	NUMBER_OF_NORMAL_CAMPFIRES("Campfire.NumberOfNormalSchematics", 2),
	NUMBER_OF_TRAPPED_CAMPFIRES("Campfire.NumberOfTrappedSchematics", 2),
	CAMPFIRE_TRAP_CHANCE("Campfire.TrapChance", 10),
	CAMPFIRE_PROTECTION_RADIUS("Campfire.ProtectionRadius", 40),
	
	OUTPOST_GRID_SPACE("Outpost.Spacing", 500),
	OUTPOST_PASTING_Y("Outpost.PastingY", 1),
	OUTPOST_RANDOM_OFFSET("Outpost.RandomOffset", 150),
	OUTPOST_OFFSET_X("Outpost.OffsetX", 350),
	OUTPOST_OFFSET_Z("Outpost.OffsetZ", 350),
	NUMBER_OF_OUTPOSTS("Outpost.NumberOfSchematics", 2),
	OUTPOST_PROTECITON_RADIUS("Outpost.ProtectionRadius", 50),
	
	RESORATION_VILLAGE_CHECK_PADDING("Restoration.VillageCheckPadding", 10),
	RESTORATION_VILLAGE_CHECK_PERIOD("Restoration.VillageCheckPeriod", 1800),
	RESTORATION_WARN_PERCENTAGE("Restoration.WarnPercentage", 60),

	INVESTIGATION_TOOL("InvestigationTool", Material.STICK.getId()),
	
	NETHERRACK_FIRE_CHANCE("HardMode.NetherrackFireChance", 20),
	PLAYER_PUNCH_FIRE_DURATION("HardMode.PlayerPunchFireDuration", 35),
	ZOMBIE_RESPAWN_CHANCE("HardMode.ZombieRespawnChance", 20),
	PIGMAN_ANGER_RANGE("HardMode.PigmanAngerRange", 1),
	
	DEBUG("Debug", false),
	
	MESSAGE_DELETE_DB_TO_IMPORT("Messages.DeleteDBToImport", "Villages are already created! Delete data.db file to reset plugin."),
	MESSAGE_NO_PERMISSION("Messages.NoPermission", "No permission!"),
	MESSAGE_SERVER_FROZEN("Messages.ServerFrozen", "Generating stuff... [NEWLINE] &cYour server is now &bfrozen&c and you will soon disconnect! [NEWLINE] &fMonitor progress in server console."),
	MESSAGE_GENERATION_COMPLETED("Messages.GenerationCompleted", "&aGeneration completed!"),
	MESSAGE_RIGHT_CLICK_CHEST_WITH_ARM("Messages.RightClickChestWithArm", "Right click chest with your arm to finish creating it!"),
	MESSAGE_LOOTING_TABLE_DOES_NOT_EXIST("Messages.LootingTableDoesNotExist", "That looting table does not exist!"),
	MESSAGE_CHEST_CREATED("Messages.ChestCreated", "Restockable chest created."),
	MESSAGE_CHEST_EXISTS("Messages.ChestExists", "Chest already exists! Break it to delete it."),
	MESSAGE_CHEST_DELETED("Messages.ChestDeleted", "Restockable chest deleted."),
	MESSAGE_CHESTS_RESTOCKED("Messages.ChestsRestocked", "Chests Restocked"),
	MESSAGE_ANALYZING("Messages.Analyzing", "Analyzing villages. Please wait..."),
	MESSAGE_VILLAGE_STATUS("Messages.VillageStatus", "Total number of Villages: <Total> [NEWLINE] Claimed Villages: <Claimed> (<ClaimedPercent>%) [NEWLINE] Empty villages: <Empty> (<EmptyPercent>%)"),
	MESSAGE_LOGIN_WARN("Messages.LoginWarn", "Warning! <Claimed> of <Total> villages (<Percentage>%) are already claimed!"),
	MESSAGE_CAN_MAKE_PORTAL("Messages.CanMakePortal", "&aYou can make a Nether portal here if you’d like"),
	MESSAGE_CANT_MAKE_PORTAL("Messages.CantMakePortal", "&cYou cannot make a Nether portal here because it would overlap a claim in <OtherDimension>."),
	MESSAGE_CONFIGURATION_RELOADED("Messages.ConfigurationReloaded", "Configuration reloaded successfully!"),
	MESSAGE_DELETE_NEARBY_VILLAGE("Messages.DeleteNearbyVillage", "You have chosen to delete village #<ID> (Center is <Distance> blocks away). Enter command again to confirm."),
	MESSAGE_NO_VILLAGES("Messages.NoVillages", "You have no villages on this server!"),
	MESSAGE_VILLAGE_DELETED("Messages.VillageDeleted", "Village deleted."),
	MESSAGE_TELEPORTED("Messages.Teleported", "Teleported."),
	MESSAGE_FOUND_ADMIN_VILLAGE("Messages.FoundAdminVillage", "Found village with admin claim at <X> <Z>! Type command again to teleport."),
	MESSAGE_NO_ADMIN_VILLAGE("Messages.NoAdminVillage", "Congratulations! You have no villages with admin claims!"),
	MESSAGE_VILLAGE_WILL_NOT_BE_RESTORED("Messages.VillageWillNotBeRestored", "Village #<ID> (Center is <Distance> blocks away) will not be restored (claim is in the way)."),
	MESSAGE_VILLAGE_WILL_BE_RESTORED("Messages.VillageWillBeRestored", "Village #<ID> (Center is <Distance> blocks away) will BE restored."),
	MESSAGE_SPAWN_IGNORED("Messages.SpawnIgnored", "Your home is now ignored when spawning"),
	MESSAGE_SPAWN_UNIGNORED("Message.SpawnUnignored", "Your home is now unignored when spawning"),
	
	SIGN_PORTAL_OUT_OF_BOUNDARIES("Signs.PortalOutOfBoundaries", "&cWarning![NEWLINE]Your portal[NEWLINE]Is out of[NEWLINE]world limit"),
	SIGN_OVERLAP_CLAIM("Signs.OverlapClaim", "&cWarning![NEWLINE]Foreign claim[NEWLINE]overlaps your[NEWLINE]destination");
	
	private String name;
	private Object def;
	
	private Setting(String Name, Object Def)
	{
		name = Name;
		def = Def;
	}
	
	public String getString()
	{
		return name;
	}
	
	public Object getDefault()
	{
		return def;
	}
}

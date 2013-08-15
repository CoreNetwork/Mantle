package com.mcnsa.flatcore;

import java.util.Arrays;

public enum Setting {	
	TELEPORT_Y("Respawn.TeleportY", 14),
	SPAWN_PROTECTION_LENGTH("Respawn.SpawnProtectionLength", 120),
	SPAWN_PROTECTION_NOTIFICATIONS("Respawn.SpawnProtectionNotifications", Arrays.asList(new Integer[] {90, 60, 30, 10})),
	MOB_REMOVAL_RADIUS_SQUARED("Respawn.MobRemovalRadiusSquared", 625),
	
	RESORATION_VILLAGE_CHECK_PADDING("Restoration.VillageCheckPadding", 10),
	RESTORATION_VILLAGE_CHECK_PERIOD("Restoration.VillageCheckPeriod", 1800),
	RESTORATION_WARN_PERCENTAGE("Restoration.WarnPercentage", 60),
	
	PIGMAN_ANGER_RANGE("HardMode.PigmanAngerRange", 1),
	
	DEBUG("Debug", false),
	
	MESSAGE_DELETE_DB_TO_IMPORT("Messages.DeleteDBToImport", "Villages are already created! Delete data.db file to reset plugin."),
	MESSAGE_NO_PERMISSION("Messages.NoPermission", "No permission!"),
	MESSAGE_GENERATION_COMPLETED("Messages.GenerationCompleted", "&aGeneration completed!"),
	MESSAGE_ANALYZING("Messages.Analyzing", "Analyzing villages. Please wait..."),
	MESSAGE_VILLAGE_STATUS("Messages.VillageStatus", "Total number of Villages: <Total> [NEWLINE] Claimed Villages: <Claimed> (<ClaimedPercent>%) [NEWLINE] Empty villages: <Empty> (<EmptyPercent>%)"),
	MESSAGE_LOGIN_WARN("Messages.LoginWarn", "Warning! <Claimed> of <Total> villages (<Percentage>%) are already claimed!"),
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
	MESSAGE_SPAWN_UNIGNORED("Messages.SpawnUnignored", "Your home is now unignored when spawning"),
	MESSAGE_SPAWN_PROTECTION_START("Messages.SpawnProtectionStart", "You are invincible for <Time> seconds!"),
	MESSAGE_SPAWN_PROTECTION_NOTIFICATION("Messages.SpawnProtectionNotification", "Invicibility will expire in <Time> seconds."),
	MESSAGE_SPAWN_PROTECTION_END_CLAIMS("Messages.SpawnProtectionEndClaims", "&eYou are no longer invincible! Find food and weapons before you try to reach your &6/base"),
	MESSAGE_SPAWN_PROTECTION_END_NO_CLAIMS("Messages.SpawnProtectionEndNoClaims", "&eYou are no longer invincible! Find food or shelter."),
	MESSAGE_SPAWN_UNPROTECT_NOT_PROTECTED("Messages.UnprotectNotProtected", "&eYou are already not protected."),
	MESSAGE_SPAWN_PROTECTION_DONT_ABUSE("Messages.SpawnProtectionDontAbuse", "Do not abuse spawn protection! Use /unprotect to disable it to start killing mobs.");
	
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

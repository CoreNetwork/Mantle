package com.mcnsa.flatcore;

import java.util.Arrays;

public enum Setting {	
	TELEPORT_Y("Respawn.TeleportY", 14),
	RESPAWN_MIN_X("Respawn.MinX", -10000),
	RESPAWN_MIN_Z("Respawn.MinZ", -10000),
	RESPAWN_MAX_X("Respawn.MaxX", 10000),
	RESPAWN_MAX_Z("Respawn.MinX", 10000),
	SPAWN_PROTECTION_LENGTH("Respawn.SpawnProtectionLength", 120),
	SPAWN_PROTECTION_NOTIFICATIONS("Respawn.SpawnProtectionNotifications", Arrays.asList(new Integer[] {90, 60, 30, 10})),
	MOB_REMOVAL_RADIUS_SQUARED("Respawn.MobRemovalRadiusSquared", 625),
	
	PIGMAN_ANGER_RANGE("HardMode.PigmanAngerRange", 1),
	
	DEBUG("Debug", false),
	
	MESSAGE_NO_PERMISSION("Messages.NoPermission", "No permission!"),
	MESSAGE_CONFIGURATION_RELOADED("Messages.ConfigurationReloaded", "Configuration reloaded successfully!"),
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

package us.corenetwork.mantle;

import java.util.Arrays;

public enum Setting {	
	
	PIGMAN_ANGER_RANGE("HardMode.PigmanAngerRange", 1),
	
	DEBUG("Debug", false),
	
	MESSAGE_NO_PERMISSION("Messages.NoPermission", "No permission!"),
	MESSAGE_CONFIGURATION_RELOADED("Messages.ConfigurationReloaded", "Configuration reloaded successfully!");
	
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

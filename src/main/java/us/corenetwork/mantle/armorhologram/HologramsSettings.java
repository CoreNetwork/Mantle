package us.corenetwork.mantle.armorhologram;


import us.corenetwork.mantle.hydration.HydrationModule;

public enum HologramsSettings
{
	MESSAGE_NO_HOLOGRAM_WITH_THAT_NAME("Messages.NoHologramWithThatName", "No hologram with such name exist!"),
    MESSAGE_HOLOGRAM_ADDED("Messages.HologramAdded", "Hologram Added"),
    MESSAGE_HOLOGRAM_UPDATED("Messages.HologramUpdated", "Hologram Updated"),
    MESSAGE_HOLOGRAM_REMOVED("Messages.HologramRemoved", "Hologram Removed"),
    MESSAGE_RELOADED("Messages.Reloaded", "Reloaded.");

	protected String string;
	protected Object def;
	
	private HologramsSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) HydrationModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) HydrationModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) HydrationModule.instance.config.get(string, def);
	}
	
	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;
		
		Object descO = HydrationModule.instance.config.get(path);
		if (descO == null)
		{
			HydrationModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			HydrationModule.instance.saveConfig();
			descO = HydrationModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

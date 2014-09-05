package us.corenetwork.mantle.armorhologram;


import us.corenetwork.mantle.hydration.HydrationModule;

public enum ArmorHologramSettings
{
	;

	protected String string;
	protected Object def;
	
	private ArmorHologramSettings(String string, Object def)
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

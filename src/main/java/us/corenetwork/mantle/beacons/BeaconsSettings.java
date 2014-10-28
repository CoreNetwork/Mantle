package us.corenetwork.mantle.beacons;

import us.corenetwork.mantle.portals.PortalsModule;


public enum BeaconsSettings
{
	;
	
	protected String string;
	protected Object def;
	
	private BeaconsSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) PortalsModule.instance.config.get(string, def)).doubleValue();
	}

	public Integer integer()
	{
		return (Integer) PortalsModule.instance.config.get(string, def);
	}

	public String string()
	{
		return (String) PortalsModule.instance.config.get(string, def);
	}

	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;

		Object descO = PortalsModule.instance.config.get(path);
		if (descO == null)
		{
			PortalsModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			PortalsModule.instance.saveConfig();
			descO = PortalsModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

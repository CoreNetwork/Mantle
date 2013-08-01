package com.mcnsa.flatcore.generation;


public enum GenerationSettings {

	MESSAGE_SERVER_FROZEN("Messages.ServerFrozen", "Generating stuff... [NEWLINE] &cYour server is now &bfrozen&c and you will soon disconnect! [NEWLINE] &fMonitor progress in server console.");
	
	protected String string;
	protected Object def;
	
	private GenerationSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) GenerationModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) GenerationModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) GenerationModule.instance.config.get(string, def);
	}
	
	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;
		
		Object descO = GenerationModule.instance.config.get(path);
		if (descO == null)
		{
			GenerationModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			GenerationModule.instance.saveConfig();
			descO = GenerationModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

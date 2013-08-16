package com.mcnsa.flatcore.regeneration;



public enum RegenerationSettings {
	RESORATION_VILLAGE_CHECK_PADDING("Restoration.VillageCheckPadding", 10),
	RESTORATION_WARN_PERCENTAGE("Restoration.WarnPercentage", 60),
	
	MESSAGE_ANALYZING("Messages.Analyzing", "Analyzing regenerative structures. Please wait..."),
	MESSAGE_ANALYZE_HEADER("Messages.AnalyzeHeader", "Status of regeneration:"),
	MESSAGE_ANALYZE_LINE("Messages.VillageStatus", "<Structure>: [NEWLINE] Total: <Total> [NEWLINE] Claimed: <Claimed> (<ClaimedPercent>%) [NEWLINE] Empty: <Empty> (<EmptyPercent>%) [NEWLINE] [NEWLINE]"),
	MESSAGE_RESPAWNED("Messages.Respawned", "Nearby structure (Center is <Distance> blocks away) has been respawned."),
	MESSAGE_NO_STRUCTURES("Messages.NoStructures", "You have no respawnable structures on this server!"),
	MESSAGE_LOGIN_WARN("Messages.LoginWarn", "Warning! <Claimed> of <Total> (<Percentage>%) positions of structure <Structure> are already claimed!"),
	MESSAGE_STRUCTURE_DELETED("Messages.StructureDeleted", "Structure deleted from DB."),
	MESSAGE_STRUCTURE_WILL_NOT_BE_RESTORED("Messages.StructureWillNotBeRestored", "Structure #<ID> (Center is <Distance> blocks away) will not be restored (claim is in the way)."),
	MESSAGE_DELETE_NEARBY_STRUCTURE("Messages.DeleteNearbyStructure", "You have chosen to delete structure #<ID> (Center is <Distance> blocks away). Enter command again to confirm."),
	MESSAGE_STRUCTURE_WILL_BE_RESTORED("Messages.VillageWillBeRestored", "Structure #<ID> (Center is <Distance> blocks away) will BE restored.");

	protected String string;
	protected Object def;
	
	private RegenerationSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) RegenerationModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) RegenerationModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) RegenerationModule.instance.config.get(string, def);
	}
	
	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;
		
		Object descO = RegenerationModule.instance.config.get(path);
		if (descO == null)
		{
			RegenerationModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			RegenerationModule.instance.saveConfig();
			descO = RegenerationModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

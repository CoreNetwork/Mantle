package us.corenetwork.mantle.inspector;



public enum InspectorSettings {
	POSTPONE_TIME("PostponeTimeSeconds", 3600 * 24 * 14),
	
	MESSAGE_COMMAND_SYNTAX("Messages.CommandSyntax", "Usage: /inspect start|stop|approve|postpone|reject|skip"),
	MESSAGE_SESSION_ALREADY_ACTIVE("Messages.SessionAlreadyActive", "Inspection is already started!"),
	MESSAGE_SESSION_NOT_ACTIVE("Messages.SessionNotActive", "You must first start inspection using /inspect start"),
	MESSAGE_NO_STRUCTURE_FOUND("Messages.NoStructureFound", "There are no uninspected structures left! Congratulations!"),
	MESSAGE_TELEPORTED("Messages.Teleported", "Teleporting you to a structure #<ID>."),
	MESSAGE_APPROVE("Messages.Approve", "Structure was approved and will never be refreshed again."),
	MESSAGE_REJECT("Messages.Reject", "Claims were deleted and structure was now restored, waiting for new better owner."),
	MESSAGE_POSTPONE("Messages.Postpone", "Inspection was postponed for two weeks."),
	MESSAGE_SKIP("Messages.Skip", "Skipped. You won't see this structure until you use /inspect start again."),
	MESSAGE_STOP("Messages.Stop", "Inspection stopped");
	
	protected String string;
	protected Object def;
	
	private InspectorSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) InspectorModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) InspectorModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) InspectorModule.instance.config.get(string, def);
	}
	
	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;
		
		Object descO = InspectorModule.instance.config.get(path);
		if (descO == null)
		{
			InspectorModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			InspectorModule.instance.saveConfig();
			descO = InspectorModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

package com.mcnsa.flatcore.restockablechests;


public enum RChestsSettings {
	ENABLED("Enabled", true),
	TELEPORT_DELAY("TeleportDelay", 5),
	
	MESSAGE_CHECKPOINT_CREATED("Messages.PointCreated", "Checkpoint <Position> in list <List> created!"),
	MESSAGE_LIST_NOT_EXIST("Messages.ListNotExist", "List <List> does not exist!"),
	MESSAGE_LIST_DELETED("Messages.ListDeleted", "List <List> deleted."),
	MESSAGE_CHECKPOINT_NOT_EXIST("Messages.CheckpointNotExist", "Checkpoint <Position> in <List> does not exist!"),
	MESSAGE_CHECKPOINT_MOVED("Messages.CheckpointMoved", "Checkpoint <Position> in <List> moved to your location."),
	MESSAGE_CHECKPOINT_SAVED("Messages.CheckpointSaved", "<Player>, your progress is now saved at checkpoint <Position> in <List>!"),
	MESSAGE_CHECKPOINT_NO_BACKWARDS("Messages.CheckpointNoBackwards", "<Player>, Checkpoint <Position> at <List> is behind your latest checkpoint! Nothing has been saved."),
	MESSAGE_NOTHING_SAVED("Messages.NothingSaved", "<Player>, You need to save checkpoint first before teleporting!"),
	MESSAGE_TELEPORT_SCHEDULED("Messages.TeleportScheduled", "<Player>, You will be teleported in few seconds to your checkpoint! Be completely still until then."),
	MESSAGE_TELEPORT_CANCELLED("Messages.TeleportCancelled", "<Player>, Your teleport was cancelled! Try again and don't move.");

	protected String string;
	protected Object def;
	
	private RChestsSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public Integer integer()
	{
		return (Integer) RChestsModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) RChestsModule.instance.config.get(string, def);
	}
	
	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;
		
		Object descO = RChestsModule.instance.config.get(path);
		if (descO == null)
		{
			RChestsModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			RChestsModule.instance.saveConfig();
			descO = RChestsModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

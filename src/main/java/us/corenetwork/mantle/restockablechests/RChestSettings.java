package us.corenetwork.mantle.restockablechests;

public enum RChestSettings {	
	
	USE_ONLY_CHEST_GUI("UseOnlyChestGUI", true),
	CHEST_COOLDOWN_EXCLUSION_RANGE("ChestCooldownExclusionRangeSquared", 900),
	
	MESSAGE_RIGHT_CLICK_CHEST_WITH_ARM("Messages.RightClickChestWithArm", "Right click chest with your arm to finish creating it!"),
	MESSAGE_LOOTING_TABLE_DOES_NOT_EXIST("Messages.LootingTableDoesNotExist", "That looting table does not exist!"),
	MESSAGE_CHEST_CREATED("Messages.ChestCreated", "Restockable chest created."),
	MESSAGE_CHEST_EXISTS("Messages.ChestExists", "Chest already exists! Break it to delete it."),
	MESSAGE_CHEST_DELETED("Messages.ChestDeleted", "Restockable chest deleted."),
	MESSAGE_CHESTS_RESTOCKED("Messages.ChestsRestocked", "Chests Restocked");

	
	protected String string;
	protected Object def;
	
	private RChestSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) RChestsModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) RChestsModule.instance.config.get(string, def);
	}
	
	public Boolean bool()
	{
		return (Boolean) RChestsModule.instance.config.get(string, def);
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

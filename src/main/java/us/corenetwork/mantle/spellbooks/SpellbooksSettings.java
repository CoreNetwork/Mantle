package us.corenetwork.mantle.spellbooks;

import java.util.List;

public enum SpellbooksSettings {	
	MESSAGE_RESLIME_SUCCESS("Messages.ReslimeSuccess", "No-slime spell was successfully lifted from this chunk!"),
	MESSAGE_RESLIME_FAIL("Messages.ReslimeFail", "This chunk is not under unsliming spell!"),

	MESSAGE_NO_PERMISSION("Messages.NoPermission", "Sorry, you can't use spellbook on territory owned by somebody else!");
	
	protected String string;
	protected Object def;
	
	private SpellbooksSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) SpellbooksModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) SpellbooksModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) SpellbooksModule.instance.config.get(string, def);
	}
		
	public List<String> stringList()
	{
		return (List<String>) SpellbooksModule.instance.config.get(string, def);
	}
}

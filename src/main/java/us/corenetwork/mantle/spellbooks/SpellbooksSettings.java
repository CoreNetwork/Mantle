package us.corenetwork.mantle.spellbooks;

import java.util.List;

public enum SpellbooksSettings {

	MESSAGE_NOT_AUTHORIZED("Messages.NotAuthorized", "Sorry, you can't use spellbook bound to someone else!"),
	MESSAGE_USED("Messages.Used", "Player <Player> used <Spellbook>.");
	
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

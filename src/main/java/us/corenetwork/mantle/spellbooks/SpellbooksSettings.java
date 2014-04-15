package us.corenetwork.mantle.spellbooks;

import java.util.List;

public enum SpellbooksSettings {

	MESSAGE_UNSLIMING_NOT_SLIME_CHUNK("Messages.Unsliming.NotSlimeChunk", "Your current chunk is not slimed!"),
	MESSAGE_UNSLIMING_SLIME_CHUNK("Messages.Unsliming.SlimeChunk", "That slime chunk has been purged! Those little bastards won't bugger you anymore."),

	MESSAGE_NOT_AUTHORIZED("Messages.NotAuthorized", "Sorry, you can't use spellbook bound to someone else!"),
	MESSAGE_USED("Messages.Used", "Player <Player> used <Spellbook>."),
	NO_BUILD_PERMISSION("Messages.NoBuildPermission", "Sorry, you can't use spellbook on territory owned by somebody else!");
	
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

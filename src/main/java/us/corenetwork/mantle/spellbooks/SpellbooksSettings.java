package us.corenetwork.mantle.spellbooks;

import java.util.List;

public enum SpellbooksSettings {

	USED_BROADCAST_MINIMUM_DELAY_SECONDS("UsedBroadcastMinimumDelaySeconds", 5),
	
	MESSAGE_UNSLIMING_NOT_SLIME_CHUNK("Messages.Unsliming.NotSlimeChunk", "Your current chunk is not slimed!"),
	MESSAGE_UNSLIMING_SLIME_CHUNK("Messages.Unsliming.SlimeChunk", "That slime chunk has been purged! Those little bastards won't bugger you anymore."),

	MESSAGE_RESLIME_SUCCESS("Messages.ReslimeSuccess", "No-slime spell was successfully lifted from this chunk!"),
	MESSAGE_RESLIME_FAIL("Messages.ReslimeFail", "This chunk is not under unsliming spell!"),

	MESSAGE_PEDDLING_NOTHING_TO_SELL("Messages.Peddling.NothingToSell", "You have nothing to sell to this villager!"),
	MESSAGE_PEDDLING_NOT_ENOUGH_TO_SELL("Messages.Peddling.NotEnoughToSell", "You don't have enough items to sell to this villager!"),

	MESSAGE_USED("Messages.Used", "Player <Player> used <Spellbook>."),
	MESSAGE_YOU_USED("Messages.YouUsed", "You used <Spellbook>."),

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

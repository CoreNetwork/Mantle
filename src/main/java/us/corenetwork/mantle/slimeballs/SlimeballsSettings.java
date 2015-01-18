package us.corenetwork.mantle.slimeballs;


public enum SlimeballsSettings
{
	RELEASE_COMMAND("ReleaseCommand", "limbo release <Player>"),

	MESSAGE_SLIMEBALLS_ACCOUNT_HEADER_EMPTY("Messages.Account.HeaderEmpty", "You don't have slimeballs!"),
	MESSAGE_SLIMEBALLS_ACCOUNT_HEADER_NOT_EMPTY("Messages.Account.HeaderNotEmpty", "You have <Amount> slimeball<PluralS>!"),
	MESSAGE_SLIMEBALLS_ACCOUNT_MOD("Messages.Account.Mod", "<Player>'s slimeballs: <Amount>"),
	MESSAGE_SLIMEBALLS_ACCOUNT_FOOTER("Messages.Account.Footer", "&7[NEWLINE]&bEnchanted Slimeballs are simply extra lives. Grab a slimeball and right click to get released from Limbo.[NEWLINE]&3Every player gets one when they join.[NEWLINE]&3Get more here: http://redd.it/1vuj5j"),
	MESSAGE_SLIMEBALLS_ACCOUNT_OTHER_PLAYER("Messages.AccountOtherPlayer", "<Player> currently has <Count> Slimeball<PluralS>!"),
	MESSAGE_SLIMEBALLS_PLAYER_NOT_EXISTS("Messages.PlayerDoesNotExist", "<Player> does not exist!"),

	MESSAGE_SLIMEBALL_CLICK_TEXT_PREFIX("Messages.SlimeballClickText.Prefix", "&3You are about to be released from Limbo. ["),
	MESSAGE_SLIMEBALL_CLICK_TEXT_BUTTON("Messages.SlimeballClickText.Button", "&bClick to confirm"),
	MESSAGE_SLIMEBALL_CLICK_TEXT_SUFFIX("Messages.SlimeballClickText.Suffix", "&3]"),

	MESSAGE_SLIMEBALLS_RELEASE("Messages.Release.Player", "You tripped over your Slimeball and passed out. Now you have woken up in different place...[NEWLINE]&3More: /slimeballs"),
	MESSAGE_SLIMEBALLS_RELEASE_NOTIFICATION("Messages.Release.Notification", "<Player> just slimed out of the Limbo! <Amount> balls remaining."),
	MESSAGE_SLIMEBALLS_RELEASE_EMPTY_ACCOUNT("Messages.Release.EmptyAccount", "&cYou don't have any slimeballs. &7/slimeballs"),
	MESSAGE_SLIMEBALLS_AWARDED_PLAYER("Messages.Awarded.Player", "&aYou received <Slimeballs> slimeball<PluralS>!"),
	MESSAGE_SLIMEBALLS_AWARDED_OTHER("Messages.Awarded.Other", "&a<Player> received <Slimeballs> slimeball<PluralS>! &2More: /slimeballs");

	protected String string;
	protected Object def;

	private SlimeballsSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) SlimeballsModule.instance.config.get(string, def)).doubleValue();
	}

	public Integer integer()
	{
		return (Integer) SlimeballsModule.instance.config.get(string, def);
	}

	public String string()
	{
		return (String) SlimeballsModule.instance.config.get(string, def);
	}

	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;

		Object descO = SlimeballsModule.instance.config.get(path);
		if (descO == null)
		{
			SlimeballsModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			SlimeballsModule.instance.saveConfig();
			descO = SlimeballsModule.instance.config.get(path);
		}

		return (String) descO;

	}
}

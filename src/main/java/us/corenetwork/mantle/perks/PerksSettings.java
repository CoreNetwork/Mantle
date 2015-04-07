package us.corenetwork.mantle.perks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.YamlUtils;


public enum PerksSettings
{

	SPECIAL_SKULL_NANOBOT_FILE("SpecialSkullNanobotFile", "special-skull"),
	SPECIAL_ARMOR_STAND_NANOBOT_FILE("SpecialArmorStandNanobotFile", "special-armorstand"),
	BANNER_LORE("BannerRole", Arrays.asList(new String[] {"&7Can be placed only by subscribers", "&7in land they claimed themselves.", "&8Will disappear when unclaimed."})),

	GROUP_TEAM_NAMES("GroupTeamNames", new HashMap<String, String>(){{
		put("Other", "0");
		put("Guardian", "A");
		put("Special", "B");
		put("SavantMaster", "C");
		put("SavantVeteran", "C");
		put("SavantAdept", "C");
		put("Savant", "C");
		put("ScribeMaster", "D");
		put("ScribeVeteran", "D");
		put("ScribeAdept", "D");
		put("Scribe", "D");
		put("MerchantMaster", "E");
		put("MerchantVeteran", "E");
		put("MerchantAdept", "E");
		put("Merchant", "E");
		put("MercenaryMaster", "F");
		put("MercenaryVeteran", "F");
		put("MercenaryAdept", "F");
		put("Mercenary", "F");
		put("RaiderMaster", "G");
		put("RaiderVeteran", "G");
		put("RaiderAdept", "G");
		put("Raider", "G");
		put("NomadMaster", "H");
		put("NomadVeteran", "H");
		put("NomadAdept", "H");
		put("Nomad", "H");
		put("FlatcorianMaster", "I");
		put("FlatcorianVeteran", "I");
		put("FlatcorianAdept", "I");
		put("Flatcorian", "I");
		put("Novice", "J");
	}}),


	MESSAGE_ARMOR_STAND_WRONG_PERMISSION("Messages.ArmorStandWrongPermission", "You need to be a subscriber to use this type of armor stand. Type /subscribe to learn more."),
	MESSAGE_ARMOR_STAND_WRONG_CLAIM("Messages.ArmorStandWrongClaim", "You can only place this type of armor stand in your own claim."),
	MESSAGE_BANNER_WRONG_PERMISSION("Messages.BannerWrongPermission", "You need to be a subscriber to use this type of banner. Type /subscribe to learn more."),
	MESSAGE_BANNER_WRONG_CLAIM("Messages.BannerWrongClaim", "You can only place this type of banner in your own claim."),
	MESSAGE_SKULL_WRONG_PERMISSION("Messages.SkullWrongPermission", "You need to be a subscriber to use this type of skull. Type /subscribe to learn more."),
	MESSAGE_SKULL_WRONG_CLAIM("Messages.SkullWrongClaim", "You can only place this type of skull in your own claim."),

	MESSAGE_SKULL_ONLY_ONE_IN_HAND("Messages.SkullOnlyOneInHand", "Please hold exactly one wither skull in your hand."),
	;

	protected String string;
	protected Object def;
	
	private PerksSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public String getString()
	{
		return string;
	}

	public double doubleNumber()
	{
		return ((Number) us.corenetwork.mantle.perks.PerksModule.instance.config.get(string, def)).doubleValue();
	}

	public Integer integer()
	{
		return (Integer) us.corenetwork.mantle.perks.PerksModule.instance.config.get(string, def);
	}

	public String string()
	{
		return (String) us.corenetwork.mantle.perks.PerksModule.instance.config.get(string, def);
	}

	public List<String> stringList()
	{
		return (List<String>) us.corenetwork.mantle.perks.PerksModule.instance.config.get(string, def);
	}

    public ItemStack itemStack()
    {
        if (!us.corenetwork.mantle.perks.PerksModule.instance.config.contains(string))
            return null;

        return YamlUtils.readItemStack(us.corenetwork.mantle.perks.PerksModule.instance.config.getConfigurationSection(string).getValues(false));
    }

	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;

		Object descO = us.corenetwork.mantle.perks.PerksModule.instance.config.get(path);
		if (descO == null)
		{
			us.corenetwork.mantle.perks.PerksModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			us.corenetwork.mantle.perks.PerksModule.instance.saveConfig();
			descO = PerksModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

package us.corenetwork.mantle.perks;

import java.util.Arrays;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.YamlUtils;
import us.corenetwork.mantle.beacons.BeaconsModule;


public enum PerksSettings
{

	SPECIAL_SKULL_NANOBOT_FILE("SpecialSkullNanobotFile", "special-skull"),
	SPECIAL_ARMOR_STAND_NANOBOT_FILE("SpecialArmorStandNanobotFile", "special-armorstand"),
	BANNER_LORE("BannerRole", Arrays.asList(new String[] {"&7Can be placed only by subscribers", "&7in land they claimed themselves.", "&8Will disappear when unclaimed."})),

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

	public double doubleNumber()
	{
		return ((Number) us.corenetwork.mantle.beacons.BeaconsModule.instance.config.get(string, def)).doubleValue();
	}

	public Integer integer()
	{
		return (Integer) us.corenetwork.mantle.beacons.BeaconsModule.instance.config.get(string, def);
	}

	public String string()
	{
		return (String) us.corenetwork.mantle.beacons.BeaconsModule.instance.config.get(string, def);
	}

	public List<String> stringList()
	{
		return (List<String>) us.corenetwork.mantle.beacons.BeaconsModule.instance.config.get(string, def);
	}

    public ItemStack itemStack()
    {
        if (!us.corenetwork.mantle.beacons.BeaconsModule.instance.config.contains(string))
            return null;

        return YamlUtils.readItemStack(us.corenetwork.mantle.beacons.BeaconsModule.instance.config.getConfigurationSection(string).getValues(false));
    }

	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;

		Object descO = us.corenetwork.mantle.beacons.BeaconsModule.instance.config.get(path);
		if (descO == null)
		{
			us.corenetwork.mantle.beacons.BeaconsModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			us.corenetwork.mantle.beacons.BeaconsModule.instance.saveConfig();
			descO = BeaconsModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

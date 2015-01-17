package us.corenetwork.mantle.perks;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.YamlUtils;
import us.corenetwork.mantle.beacons.BeaconsModule;


public enum PerksSettings
{
	SPECIAL_ARMOR_STAND_NANOBOT_FILE("SpecialArmorStandNanobotFile", "special-armorstand"),

	MESSAGE_ARMOR_STAND_WRONG_PERMISSION("Messages.ArmorStandWrongPermission", "You need to be a subscriber to use this type of armor stand. Type /subscribe to learn more."),
	MESSAGE_ARMOR_STAND_WRONG_CLAIM("Messages.ArmorStandWrongClaim", "You can only place this type of armor stand in your own claim.");


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

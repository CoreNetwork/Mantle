package us.corenetwork.mantle.beacons;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;
import us.corenetwork.mantle.YamlUtils;
import us.corenetwork.mantle.portals.PortalsModule;


public enum BeaconsSettings
{
	GUI_ITEM_HELP("GuiItems.Help", new ItemStack(Material.SIGN, 1)),
    GUI_ITEM_HELP_SELECTED("GuiItems.HelpSelected", new ItemStack(Material.SIGN, 1)),
    GUI_ITEM_CANCEL_EFFECT("GuiItems.CancelEffect", new ItemStack(Material.BEACON, 1)),
    GUI_ITEM_BEACON_ON("GuiItems.BeaconON", new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GREEN.getData())),
    GUI_ITEM_BEACON_OFF("GuiItems.BeaconOFF", new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData())),

    GUI_TITLE_PICK_EFFECT("Messages.GuiTitles.PickEffect", "Hover icons for options"),
    GUI_TITLE_BEACON_STATUS("Messages.GuiTitles.BeaconStatus", "[Return] ------------- [Help]");


    protected String string;
	protected Object def;
	
	private BeaconsSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) BeaconsModule.instance.config.get(string, def)).doubleValue();
	}

	public Integer integer()
	{
		return (Integer) BeaconsModule.instance.config.get(string, def);
	}

	public String string()
	{
		return (String) BeaconsModule.instance.config.get(string, def);
	}

    public ItemStack itemStack()
    {
        if (!BeaconsModule.instance.config.contains(string))
            return null;

        return YamlUtils.readItemStack(BeaconsModule.instance.config.getConfigurationSection(string).getValues(false));
    }

	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;

		Object descO = PortalsModule.instance.config.get(path);
		if (descO == null)
		{
			PortalsModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			PortalsModule.instance.saveConfig();
			descO = PortalsModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

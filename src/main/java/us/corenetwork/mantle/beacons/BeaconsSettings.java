package us.corenetwork.mantle.beacons;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.YamlUtils;


public enum BeaconsSettings
{
	GUI_ITEM_HELP("GuiItems.Help", new ItemStack(Material.SIGN, 1)),
    GUI_ITEM_HELP_SELECTED("GuiItems.HelpSelected", new ItemStack(Material.SIGN, 1)),
    GUI_ITEM_CANCEL_EFFECT("GuiItems.CancelEffect", new ItemStack(Material.BEACON, 1)),
    GUI_ITEM_BEACON_ON("GuiItems.BeaconON", new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GREEN.getData())),
    GUI_ITEM_BEACON_OFF("GuiItems.BeaconOFF", new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData())),

    PYRAMID_RANGE_PER_BLOCK_IRON_L1("RangePerBlock.Iron.L1", 1.333),
    PYRAMID_RANGE_PER_BLOCK_IRON_L2("RangePerBlock.Iron.L2", 0.765),
    PYRAMID_RANGE_PER_BLOCK_IRON_L3("RangePerBlock.Iron.L3", 0.458),
    PYRAMID_RANGE_PER_BLOCK_IRON_L4("RangePerBlock.Iron.L4", 0.305),

    PYRAMID_RANGE_PER_BLOCK_GOLD_L1("RangePerBlock.Gold.L1", 1.333),
    PYRAMID_RANGE_PER_BLOCK_GOLD_L2("RangePerBlock.Gold.L2", 0.765),
    PYRAMID_RANGE_PER_BLOCK_GOLD_L3("RangePerBlock.Gold.L3", 0.458),
    PYRAMID_RANGE_PER_BLOCK_GOLD_L4("RangePerBlock.Gold.L4", 0.305),

    PYRAMID_RANGE_PER_BLOCK_EMERALD_L1("RangePerBlock.Emerald.L1", 2.667),
    PYRAMID_RANGE_PER_BLOCK_EMERALD_L2("RangePerBlock.Emerald.L2", 1.529),
    PYRAMID_RANGE_PER_BLOCK_EMERALD_L3("RangePerBlock.Emerald.L3", 0.916),
    PYRAMID_RANGE_PER_BLOCK_EMERALD_L4("RangePerBlock.Emerald.L4", 0.610),

    PYRAMID_RANGE_PER_BLOCK_DIAMOND_L1("RangePerBlock.Diamond.L1", 6.667),
    PYRAMID_RANGE_PER_BLOCK_DIAMOND_L2("RangePerBlock.Diamond.L2", 3.882),
    PYRAMID_RANGE_PER_BLOCK_DIAMOND_L3("RangePerBlock.Diamond.L3", 2.313),
    PYRAMID_RANGE_PER_BLOCK_DIAMOND_L4("RangePerBlock.Diamond.L4", 1.543),

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

		Object descO = BeaconsModule.instance.config.get(path);
		if (descO == null)
		{
			BeaconsModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			BeaconsModule.instance.saveConfig();
			descO = BeaconsModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}

package us.corenetwork.mantle.beacons;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;
import us.corenetwork.mantle.YamlUtils;
import us.corenetwork.mantle.portals.PortalsModule;


public enum BeaconsSettings
{
	GUI_ITEM_HELP("GuiItems.Help", new ItemStack(Material.WRITTEN_BOOK, 1)),
    GUI_ITEM_CANCEL_EFFECT("GuiItems.CancelEffect", new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData())),
    GUI_ITEM_PYRAMID_LEVEL("GuiItems.PyramidLevel", new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData())),
    GUI_ITEM_FUEL_LEFT("GuiItems.FuelLeft", new ItemStack(Material.GOLD_INGOT, 1)),
    GUI_ITEM_EFFECT_HASTE("GuiItems.Effects.Haste", new ItemStack(Material.GOLD_PICKAXE, 1)),
    GUI_ITEM_EFFECT_REGENERATION("GuiItems.Effects.Regeneration", new ItemStack(Material.GOLDEN_APPLE, 1)),
    GUI_ITEM_EFFECT_STRENGTH("GuiItems.Effects.Strength", new ItemStack(Material.IRON_SWORD, 1)),
    GUI_ITEM_EFFECT_JUMP_BOOST("GuiItems.Effects.JumpBoost", new ItemStack(Material.BEDROCK, 1)),
    GUI_ITEM_EFFECT_WATER_BREATHING("GuiItems.Effects.WaterBreathing", new ItemStack(Material.RAW_FISH, 1)),
    GUI_ITEM_EFFECT_OVERCLOCK("GuiItems.Effects.Overclock", new ItemStack(Material.FURNACE, 1)),
    GUI_ITEM_EFFECT_ANIMAL_GROWTH("GuiItems.Effects.AnimalGrowth", new ItemStack(Material.WHEAT, 1)),

    GUI_TITLE_PICK_EFFECT("Messages.GuiTitles.PickEffect", "Hover icons for options");


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
